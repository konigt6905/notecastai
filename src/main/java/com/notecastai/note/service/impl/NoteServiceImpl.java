package com.notecastai.note.service.impl;

import com.notecastai.common.util.SecurityUtils;
import com.notecastai.integration.ai.NoteAiChat;
import com.notecastai.integration.ai.NoteAiEditor;
import com.notecastai.integration.ai.provider.openrouter.dto.FormatNoteAiResponse;
import com.notecastai.integration.ai.provider.openrouter.dto.NewNoteAiResponse;
import com.notecastai.note.api.dto.*;
import com.notecastai.note.api.mapper.NoteMapper;
import com.notecastai.note.domain.FormateType;
import com.notecastai.note.domain.NoteEntity;
import com.notecastai.note.infrastructure.repo.NoteRepository;
import com.notecastai.note.service.NoteService;
import com.notecastai.tag.api.dto.TagDTO;
import com.notecastai.tag.domain.TagEntity;
import com.notecastai.tag.repo.TagRepository;
import com.notecastai.user.api.mapper.UserMapper;
import com.notecastai.user.domain.UserEntity;
import com.notecastai.user.infrastructure.repo.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class NoteServiceImpl implements NoteService {

    private final NoteRepository noteRepository;
    private final UserRepository userRepository;
    private final TagRepository tagRepository;
    private final NoteMapper mapper;
    private final UserMapper userMapper;
    private final NoteAiEditor noteAiEditor;
    private final NoteAiChat noteAiChat;

    @Override
    @Transactional
    public NoteDTO create(NoteCreateRequest request) {
        UserEntity user = userRepository.getByClerkUserId(SecurityUtils.getCurrentClerkUserIdOrThrow());

        // Call AI with retry logic
        NewNoteAiResponse aiResponse = noteAiEditor.adjustNewNote(request);

        // Map AI actions
        List<NoteEntity.AiAction> aiActions = aiResponse.getProposedAiActions().stream()
                .map(action -> NoteEntity.AiAction.builder()
                        .name(action.getName())
                        .prompt(action.getPrompt())
                        .build())
                .collect(Collectors.toList());

        NoteEntity entity = NoteEntity.builder()
                .user(user)
                .title(aiResponse.getAdjustedTitle())
                .knowledgeBase(request.getKnowledgeBase())
                .formattedNote(aiResponse.getFormattedNote())
                .currentFormate(request.getFormateType())
                .type(request.getType())
                .tags(aiResponse.getTagIds().stream()
                        .map(tagRepository::getById)
                        .collect(Collectors.toSet()))
                .proposedAiActions(aiActions)
                .build();

        NoteEntity saved = noteRepository.save(entity);
        log.info("Note created: id={}, title={}, tags={}, actions={}",
                saved.getId(), saved.getTitle(), saved.getTags().size(), saved.getProposedAiActions().size());

        return mapper.toDto(saved);
    }

    @Override
    @Transactional
    public NoteDTO updateManual(Long id, NoteAdjustManualRequest request) {
        NoteEntity entity = noteRepository.getOrThrow(id);

        if (request.getTitle() != null) {
            entity.setTitle(request.getTitle());
        }

        if (request.getKnowledgeBase() != null) {
            entity.setKnowledgeBase(request.getKnowledgeBase());
        }

        if (request.getTagIds() != null) {
            Set<TagEntity> tags = resolveAndValidateTags(request.getUserId(), request.getTagIds());
            entity.setTags(tags);
        }

        return mapper.toDto(noteRepository.save(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<NoteDTO> findAll(NotesQueryParam params, Pageable pageable) {
        return noteRepository.findAll(params, pageable).map(mapper::toDto);
    }

    @Override
    public Page<NoteShortDTO> findAllShort(NotesQueryParam params, Pageable pageable) {
        Page<NoteEntity> notes = noteRepository.findAll(params, pageable);
        return notes.map(this::toNoteShortDTO);
    }

    @Override
    @Transactional
    public NoteDTO formateNoteKnowledgeBase(Long noteId, NoteFormatRequest request) {
        NoteEntity entity = noteRepository.getOrThrow(noteId);

        // Call AI with retry logic
        FormatNoteAiResponse aiResponse = noteAiEditor.formatNote(noteId, request);

        entity.setTitle(aiResponse.getAdjustedTitle());
        entity.setKnowledgeBase(aiResponse.getKnowledgeBase());

        // Update tags if any valid ones found
        if (!aiResponse.getTagIds().isEmpty()) {
            Set<TagEntity> tags = aiResponse.getTagIds().stream()
                    .map(tagRepository::getById)
                    .collect(Collectors.toSet());
            entity.setTags(tags);
        }

        // Update proposed actions
        List<NoteEntity.AiAction> aiActions = aiResponse.getProposedAiActions().stream()
                .map(action -> NoteEntity.AiAction.builder()
                        .name(action.getName())
                        .prompt(action.getPrompt())
                        .build())
                .collect(Collectors.toList());
        entity.setProposedAiActions(aiActions);

        NoteEntity saved = noteRepository.save(entity);
        log.info("Note formatted: id={}, title={}, tags={}, actions={}",
                saved.getId(), saved.getTitle(), saved.getTags().size(), saved.getProposedAiActions().size());

        return mapper.toDto(saved);
    }

    @Override
    public NoteQuestionResponse askQuestion(Long noteId, NoteQuestionRequest request) {
        return noteAiChat.askQuestion(noteId, request);
    }

    @Override
    public NoteDTO getById(Long id) {
        NoteEntity entity = noteRepository.getOrThrow(id);
        return mapper.toDto(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<NoteFormatTypeDTO> listFormats() {
        return Arrays.stream(FormateType.values())
                .map(ft -> NoteFormatTypeDTO.builder()
                        .code(ft.name())
                        .label(ft.getLabel())
                        .promptText(ft.getPromptText())
                        .build())
                .collect(Collectors.toList());
    }

    private Set<TagEntity> resolveAndValidateTags(Long userId, List<Long> tagIds) {
        if (tagIds == null || tagIds.isEmpty()) return Set.of();
        Set<TagEntity> result = new HashSet<>();
        for (Long tagId : tagIds) {
            if (tagId == null) continue;
            TagEntity tag = tagRepository.findByIdAndUserOrThrow(tagId, userId);
            result.add(tag);
        }
        return result;
    }

    @Override
    @Transactional
    public NoteDTO combine(NoteCombineRequest request) {
        List<NoteEntity> notes = request.getNoteIds().stream()
                .distinct()
                .map(noteRepository::getOrThrow)
                .toList();

        // Combine knowledge bases from all notes
        String combinedKnowledgeBase = notes.stream()
                .map(note -> {
                    StringBuilder sb = new StringBuilder();
                    sb.append("### ").append(note.getTitle()).append("\n\n");
                    sb.append(note.getKnowledgeBase()).append("\n\n");
                    sb.append("---\n\n");
                    return sb.toString();
                })
                .collect(Collectors.joining());

        // Create a new note request with combined knowledge base
        NoteCreateRequest createRequest = NoteCreateRequest.builder()
                .title(request.getTitle())
                .knowledgeBase(combinedKnowledgeBase)
                .tagIds(request.getTagIds())
                .type(com.notecastai.note.domain.NoteType.COMBINED)
                .formateType(request.getFormateType())
                .autoAiFormate(request.isAutoAiFormate())
                .instructions(request.getInstructions())
                .build();

        log.info("Creating combined note from {} notes", notes.size());

        return create(createRequest);
    }

    @Override
    @Transactional
    public NoteDTO addTag(Long noteId, Long tagId) {
        NoteEntity note = noteRepository.getOrThrow(noteId);
        TagEntity tag = tagRepository.getById(tagId);

        note.getTags().add(tag);

        NoteEntity savedNote = noteRepository.save(note);
        log.info("Tag {} added to note {}", tagId, noteId);

        return mapper.toDto(savedNote);
    }

    @Override
    @Transactional
    public NoteDTO removeTag(Long noteId, Long tagId) {
        NoteEntity note = noteRepository.getOrThrow(noteId);

        note.getTags().removeIf(tag -> tag.getId().equals(tagId));

        NoteEntity savedNote = noteRepository.save(note);
        log.info("Tag {} removed from note {}", tagId, noteId);

        return mapper.toDto(savedNote);
    }

    private NoteShortDTO toNoteShortDTO(NoteEntity entity) {
        return NoteShortDTO.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .tags(entity.getTags().stream()
                        .map(tag -> TagDTO.builder()
                                .user(userMapper.toDto(tag.getUser()))
                                .name(tag.getName())
                                .build())
                        .toList())
                .createdDate(entity.getCreatedDate())
                .updatedDate(entity.getUpdatedDate())
                .build();
    }

}