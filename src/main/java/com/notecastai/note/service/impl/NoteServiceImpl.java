package com.notecastai.note.service.impl;

import com.notecastai.common.util.SecurityUtils;
import com.notecastai.integration.ai.NoteAiChat;
import com.notecastai.integration.ai.NoteAiEditor;
import com.notecastai.integration.ai.dto.AiAdjustedNote;
import com.notecastai.note.domain.FormateType;
import com.notecastai.note.api.mapper.NoteMapper;
import com.notecastai.note.api.dto.*;
import com.notecastai.note.domain.NoteEntity;
import com.notecastai.note.infrastructure.repo.NoteRepository;
import com.notecastai.note.service.NoteService;
import com.notecastai.tag.api.dto.TagDTO;
import com.notecastai.tag.domain.TagEntity;
import com.notecastai.tag.repo.TagRepository;
import com.notecastai.user.domain.UserEntity;
import com.notecastai.user.infrastructure.repo.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NoteServiceImpl implements NoteService {

    private final NoteRepository noteRepository;
    private final UserRepository userRepository;
    private final TagRepository tagRepository;
    private final NoteMapper mapper;
    private final NoteAiEditor noteAiEditor;
    private final NoteAiChat noteAiChat;

    @Override
    @Transactional
    public NoteDTO create(NoteCreateRequest request) {
        UserEntity user = userRepository.getByClerkUserId(SecurityUtils.getCurrentClerkUserIdOrThrow());

        AiAdjustedNote adjustedNote = noteAiEditor.adjustNewNote(request);

        NoteEntity entity = NoteEntity.builder()
                .user(user)
                .title(adjustedNote.getTitle())
                .knowledgeBase(adjustedNote.getKnowledgeBase())
                .formattedNote(adjustedNote.getFormattedNote())
                .currentFormate(request.getFormateType() == null ? FormateType.DEFAULT : request.getFormateType())
                .tags(adjustedNote.getTags().stream()
                        .map(tagDto -> tagRepository.getById(tagDto.getId()))
                        .collect(Collectors.toList())
                )
                .build();

        return mapper.toDto(noteRepository.save(entity));
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
            List<TagEntity> tags = resolveAndValidateTags(request.getUserId(), request.getTagIds());
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

        AiAdjustedNote adjustedNote = noteAiEditor.formateNote(noteId, request);
        entity.setKnowledgeBase(adjustedNote.getKnowledgeBase());

        return mapper.toDto(noteRepository.save(entity));
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

    private List<TagEntity> resolveAndValidateTags(Long userId, List<Long> tagIds) {
        if (tagIds == null || tagIds.isEmpty()) return List.of();
        List<TagEntity> result = new ArrayList<>();
        Set<Long> seen = new HashSet<>();
        for (Long tagId : tagIds) {
            if (tagId == null || seen.contains(tagId)) continue;
            TagEntity tag = tagRepository.findByIdAndUserOrThrow(tagId, userId);
            result.add(tag);
            seen.add(tagId);
        }
        return result;
    }

    private NoteShortDTO toNoteShortDTO(NoteEntity entity) {
        return NoteShortDTO.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .tags(entity.getTags().stream()
                        .map(tag -> TagDTO.builder()
                                .id(tag.getId())
                                .name(tag.getName())
                                .build())
                        .toList())
                .createdDate(entity.getCreatedDate())
                .updatedDate(entity.getUpdatedDate())
                .build();
    }

}