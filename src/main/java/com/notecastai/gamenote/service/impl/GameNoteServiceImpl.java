package com.notecastai.gamenote.service.impl;

import com.notecastai.common.exeption.BusinessException;
import com.notecastai.common.util.SecurityUtils;
import com.notecastai.gamenote.api.dto.CreateGameNoteResponse;
import com.notecastai.gamenote.api.dto.GameNoteCreateRequest;
import com.notecastai.gamenote.api.dto.GameNoteDTO;
import com.notecastai.gamenote.api.dto.GameNoteQueryParam;
import com.notecastai.gamenote.api.dto.GameNoteShortDTO;
import com.notecastai.gamenote.api.mapper.GameNoteMapper;
import com.notecastai.gamenote.domain.GameNoteEntity;
import com.notecastai.gamenote.domain.GameNoteStatus;
import com.notecastai.gamenote.domain.event.GameNoteCreatedEvent;
import com.notecastai.gamenote.infrastructure.repo.GameNoteRepository;
import com.notecastai.gamenote.service.GameNoteService;
import com.notecastai.note.domain.NoteEntity;
import com.notecastai.note.infrastructure.repo.NoteRepository;
import com.notecastai.tag.domain.TagEntity;
import com.notecastai.tag.repo.TagRepository;
import com.notecastai.user.domain.UserEntity;
import com.notecastai.user.infrastructure.repo.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class GameNoteServiceImpl implements GameNoteService {

    private final GameNoteRepository gameNoteRepository;
    private final UserRepository userRepository;
    private final NoteRepository noteRepository;
    private final TagRepository tagRepository;
    private final GameNoteMapper mapper;
    private final ApplicationEventPublisher eventPublisher;
    private final com.notecastai.gamenote.infrastructure.repo.GameNoteStatisticsRepository statisticsRepository;

    @Override
    @Transactional
    public CreateGameNoteResponse create(GameNoteCreateRequest request) {
        UserEntity user = userRepository.getByClerkUserId(SecurityUtils.getCurrentClerkUserIdOrThrow());

        // Validate source note exists and belongs to user
        NoteEntity sourceNote = noteRepository.getOrThrow(request.getSourceNoteId());

        // Validate and resolve tags
        Set<TagEntity> tags = resolveTags(request.getTagIds(), user);

        // Generate title if not provided
        String title = request.getTitle();
        if (title == null || title.isBlank()) {
            title = generateTitle(request, sourceNote);
        }

        GameNoteEntity entity = GameNoteEntity.builder()
                .user(user)
                .sourceNote(sourceNote)
                .title(title)
                .status(GameNoteStatus.PENDING)
                .numberOfQuestions(request.getNumberOfQuestions())
                .questionLength(request.getQuestionLength())
                .answerLength(request.getAnswerLength())
                .difficulty(request.getDifficulty())
                .questionType(request.getQuestionType())
                .customInstructions(request.getCustomInstructions())
                .tags(tags)
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
        UserEntity user = userRepository.getByClerkUserId(SecurityUtils.getCurrentClerkUserIdOrThrow());

        // Fetch statistics for this game note
        List<com.notecastai.gamenote.domain.GameNoteStatisticsEntity> statistics =
                statisticsRepository.findByGameNoteAndUser(id, user.getId());

        return mapper.toDto(entity, statistics.isEmpty() ? null : statistics);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<GameNoteShortDTO> findAll(GameNoteQueryParam queryParam, Pageable pageable) {
        UserEntity user = userRepository.getByClerkUserId(SecurityUtils.getCurrentClerkUserIdOrThrow());
        queryParam.setUserId(user.getId());

        Page<GameNoteEntity> entities = gameNoteRepository.findAll(queryParam, pageable);

        // Fetch statistics for all game notes in the page
        List<Long> gameNoteIds = entities.getContent().stream()
                .map(GameNoteEntity::getId)
                .toList();

        // Batch fetch statistics for all game notes
        List<com.notecastai.gamenote.domain.GameNoteStatisticsEntity> allStatistics =
                statisticsRepository.findByUser(user.getId());

        // Group statistics by game note ID
        java.util.Map<Long, List<com.notecastai.gamenote.domain.GameNoteStatisticsEntity>> statisticsByGameNote =
                allStatistics.stream()
                        .filter(stat -> gameNoteIds.contains(stat.getGameNote().getId()))
                        .collect(java.util.stream.Collectors.groupingBy(stat -> stat.getGameNote().getId()));

        // Map entities to DTOs with statistics
        return entities.map(entity -> {
            List<com.notecastai.gamenote.domain.GameNoteStatisticsEntity> stats =
                    statisticsByGameNote.get(entity.getId());
            return mapper.toShortDto(entity, stats);
        });
    }

    @Override
    @Transactional
    public void delete(Long id) {
        GameNoteEntity entity = gameNoteRepository.getOrThrow(id);
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
        errorMessage = errorMessage != null ? errorMessage : "Unknown error occurred";
        if (errorMessage.length() > 1000) {
            errorMessage = errorMessage.substring(0, 1000) + "...";
        }

        GameNoteEntity entity = gameNoteRepository.getOrThrow(id);
        entity.setStatus(GameNoteStatus.FAILED);
        entity.setErrorMessage(errorMessage);
        gameNoteRepository.save(entity);
    }

    @Override
    @Transactional
    public GameNoteDTO addTag(Long id, Long tagId) {
        GameNoteEntity entity = gameNoteRepository.getOrThrow(id);

        TagEntity tag = tagRepository.findById(tagId)
                .orElseThrow(() -> BusinessException.of(BusinessException.BusinessCode.ENTITY_NOT_FOUND
                        .append(" Tag not found")));

        entity.getTags().add(tag);
        GameNoteEntity saved = gameNoteRepository.save(entity);

        log.info("Tag added to GameNote: gameNoteId={}, tagId={}", id, tagId);
        return mapper.toDto(saved);
    }

    @Override
    @Transactional
    public GameNoteDTO removeTag(Long id, Long tagId) {
        GameNoteEntity entity = gameNoteRepository.getOrThrow(id);

        entity.getTags().removeIf(tag -> tag.getId().equals(tagId));
        GameNoteEntity saved = gameNoteRepository.save(entity);

        log.info("Tag removed from GameNote: gameNoteId={}, tagId={}", id, tagId);
        return mapper.toDto(saved);
    }

    private Set<TagEntity> resolveTags(List<Long> tagIds, UserEntity user) {
        if (tagIds == null || tagIds.isEmpty()) {
            return new HashSet<>();
        }

        Set<TagEntity> tags = new HashSet<>();
        for (Long tagId : tagIds) {
            TagEntity tag = tagRepository.findById(tagId)
                    .orElseThrow(() -> BusinessException.of(BusinessException.BusinessCode.ENTITY_NOT_FOUND
                            .append(" Tag not found: " + tagId)));

            tags.add(tag);
        }

        return tags;
    }

    private String generateTitle(GameNoteCreateRequest request, NoteEntity sourceNote) {
        return String.format("%s - %s Quiz (%s)",
                sourceNote.getTitle(),
                request.getQuestionType().name().replace("_", " "),
                request.getDifficulty().getLabel()
        );
    }
}
