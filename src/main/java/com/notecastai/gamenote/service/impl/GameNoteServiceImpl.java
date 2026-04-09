package com.notecastai.gamenote.service.impl;

import com.notecastai.common.util.OwnershipVerifier;
import com.notecastai.gamenote.api.dto.*;
import com.notecastai.gamenote.api.mapper.GameNoteMapper;
import com.notecastai.gamenote.service.command.CreateGameNoteCommand;
import com.notecastai.gamenote.domain.GameNoteEntity;
import com.notecastai.gamenote.domain.GameNoteStatisticsEntity;
import com.notecastai.gamenote.domain.GameNoteStatus;
import com.notecastai.gamenote.domain.event.GameNoteCreatedEvent;
import com.notecastai.gamenote.infrastructure.repo.GameNoteRepository;
import com.notecastai.gamenote.infrastructure.repo.GameNoteStatisticsRepository;
import com.notecastai.gamenote.service.GameNoteService;
import com.notecastai.gamenote.service.factory.GameQuestionFactory;
import com.notecastai.integration.ai.dto.GameNoteAiRequest;
import com.notecastai.note.domain.NoteEntity;
import com.notecastai.note.infrastructure.repo.NoteRepository;
import com.notecastai.tag.domain.TagEntity;
import com.notecastai.tag.infrastructure.repo.TagRepository;
import com.notecastai.user.domain.UserEntity;
import com.notecastai.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class GameNoteServiceImpl implements GameNoteService {

    // Default values for simplified game creation
    private static final int DEFAULT_NUMBER_OF_QUESTIONS = 10;

    private final GameNoteRepository gameNoteRepository;
    private final UserService userService;
    private final NoteRepository noteRepository;
    private final TagRepository tagRepository;
    private final GameNoteMapper mapper;
    private final ApplicationEventPublisher eventPublisher;
    private final GameNoteStatisticsRepository statisticsRepository;
    private final GameQuestionFactory gameQuestionFactory;

    @Override
    @Transactional
    public CreateGameNoteResponse create(CreateGameNoteCommand command) {
        UserEntity user = userService.getCurrentUserReference();

        // Validate source note exists and belongs to user
        NoteEntity sourceNote = noteRepository.getOrThrow(command.getSourceNoteId());
        OwnershipVerifier.verify(sourceNote.getUser().getId());

        // Generate title if not provided
        String title = command.getTitle();
        if (title == null || title.isBlank()) {
            title = generateTitle(command, sourceNote);
        }

        // Questions/answers are now enforced SHORT via AI prompts
        GameNoteEntity entity = GameNoteEntity.builder()
                .user(user)
                .sourceNote(sourceNote)
                .title(title)
                .status(GameNoteStatus.PENDING)
                .numberOfQuestions(DEFAULT_NUMBER_OF_QUESTIONS)
                .difficulty(command.getDifficulty())
                .questionType(command.getQuestionType())
                .customInstructions(command.getCustomInstructions())
                .tags(new HashSet<>())  // Tags can be added later via separate endpoint
                .build();

        GameNoteEntity saved = gameNoteRepository.save(entity);

        log.info("GameNote created: id={}, type={}, questions={}, status={}",
                saved.getId(), saved.getQuestionType(), saved.getNumberOfQuestions(), saved.getStatus());

        eventPublisher.publishEvent(GameNoteCreatedEvent.builder().gameNoteId(saved.getId()).build());

        return CreateGameNoteResponse.builder()
                .id(saved.getId())
                .status(saved.getStatus())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public GameNoteDTO getById(Long id) {
        GameNoteEntity entity = gameNoteRepository.getOrThrow(id);
        OwnershipVerifier.verify(entity.getUser().getId());
        Long userId = userService.getCurrentUserId();

        // Fetch statistics for this game note
        List<GameNoteStatisticsEntity> statistics =
                statisticsRepository.findByGameNoteAndUser(id, userId);

        return mapper.toDto(entity, statistics.isEmpty() ? null : statistics);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<GameNoteShortDTO> findAll(GameNoteQueryParam queryParam, Pageable pageable) {
        Long userId = userService.getCurrentUserId();
        queryParam.setUserId(userId);

        Page<GameNoteEntity> entities = gameNoteRepository.findAll(queryParam, pageable);

        // Fetch statistics only for game notes on this page
        List<Long> gameNoteIds = entities.getContent().stream()
                .map(GameNoteEntity::getId)
                .toList();

        // Single query filtered by the page's game note IDs
        Map<Long, List<GameNoteStatisticsEntity>> statisticsByGameNote =
                statisticsRepository.findByGameNoteIdsAndUser(gameNoteIds, userId).stream()
                        .collect(Collectors.groupingBy(stat -> stat.getGameNote().getId()));

        // Map entities to DTOs with statistics
        return entities.map(entity -> {
            List<GameNoteStatisticsEntity> stats =
                    statisticsByGameNote.get(entity.getId());
            return mapper.toShortDto(entity, stats);
        });
    }

    @Override
    @Transactional
    public void delete(Long id) {
        GameNoteEntity entity = gameNoteRepository.getOrThrow(id);
        OwnershipVerifier.verify(entity.getUser().getId());
        gameNoteRepository.delete(entity);
        log.info("GameNote soft deleted: id={}", id);
    }

    @Override
    @Transactional
    public void updateStatus(Long id, GameNoteStatus status) {
        GameNoteEntity entity = gameNoteRepository.getOrThrow(id);
        entity.setStatus(status);
        gameNoteRepository.save(entity);

        log.debug("GameNote status updated: id={}, status={}", id, status);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateWithError(Long id, String errorMessage) {
        GameNoteEntity entity = gameNoteRepository.getOrThrow(id);
        entity.markAsFailed(errorMessage);
        gameNoteRepository.save(entity);
    }

    @Override
    @Transactional
    public void updateWithQuestions(Long id, List<GameQuestionDTO> questions) {
        GameNoteEntity entity = gameNoteRepository.getOrThrow(id);

        entity.getQuestions().clear();
        gameQuestionFactory.createFromDtos(entity, questions)
                .forEach(entity::addQuestion);

        entity.markAsProcessed();
        gameNoteRepository.save(entity);

        log.info("GameNote questions updated: id={}, questionsCount={}", id, questions.size());
    }

    @Override
    @Transactional(readOnly = true)
    public GameNoteAiRequest buildAiRequest(Long id) {
        GameNoteEntity entity = gameNoteRepository.getOrThrow(id);
        NoteEntity sourceNote = entity.getSourceNote();

        // Get note content (prefer knowledge base, fallback to formatted note)
        String noteContent = getNoteContent(sourceNote);

        return GameNoteAiRequest.builder()
                .noteTitle(sourceNote.getTitle())
                .noteContent(noteContent)
                .numberOfQuestions(entity.getNumberOfQuestions())
                .difficulty(entity.getDifficulty())
                .questionType(entity.getQuestionType())
                .customInstructions(entity.getCustomInstructions())
                .build();
    }

    @Override
    @Transactional
    public GameNoteDTO addTag(Long id, Long tagId) {
        GameNoteEntity entity = gameNoteRepository.getOrThrow(id);
        OwnershipVerifier.verify(entity.getUser().getId());

        Long currentUserId = userService.getCurrentUserId();
        TagEntity tag = tagRepository.findByIdAndUserOrThrow(tagId, currentUserId);

        entity.addTag(tag);
        GameNoteEntity saved = gameNoteRepository.save(entity);

        log.info("Tag added to GameNote: gameNoteId={}, tagId={}", id, tagId);
        return mapper.toDto(saved);
    }

    @Override
    @Transactional
    public GameNoteDTO removeTag(Long id, Long tagId) {
        GameNoteEntity entity = gameNoteRepository.getOrThrow(id);
        OwnershipVerifier.verify(entity.getUser().getId());

        entity.removeTagById(tagId);
        GameNoteEntity saved = gameNoteRepository.save(entity);

        log.info("Tag removed from GameNote: gameNoteId={}, tagId={}", id, tagId);
        return mapper.toDto(saved);
    }


    private String generateTitle(CreateGameNoteCommand command, NoteEntity sourceNote) {
        return String.format("%s - %s Quiz (%s)",
                sourceNote.getTitle(),
                command.getQuestionType().name().replace("_", " "),
                command.getDifficulty().getLabel()
        );
    }

    private String getNoteContent(NoteEntity sourceNote) {
        // Prefer knowledge base, fallback to formatted note
        String content = sourceNote.getKnowledgeBase();
        if (content == null || content.isBlank()) {
            content = sourceNote.getFormattedNote();
        }
        if (content == null || content.isBlank()) {
            throw new IllegalStateException("Source note has no content");
        }
        return content;
    }
}
