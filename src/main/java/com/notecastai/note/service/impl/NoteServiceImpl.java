package com.notecastai.note.service.impl;

import com.notecastai.common.exception.BusinessException;
import com.notecastai.common.util.OwnershipVerifier;
import com.notecastai.integration.ai.NoteAiChat;
import com.notecastai.integration.ai.NoteAiEditor;
import com.notecastai.integration.ai.dto.FormatNoteAiResponse;
import com.notecastai.integration.ai.dto.NewNoteAiResponse;
import com.notecastai.note.api.dto.*;
import com.notecastai.note.api.mapper.NoteMapper;
import com.notecastai.note.service.command.*;
import com.notecastai.note.domain.ExportFormat;
import com.notecastai.note.domain.FormatType;
import com.notecastai.note.domain.NoteEntity;
import com.notecastai.note.infrastructure.repo.NoteRepository;
import com.notecastai.note.service.NoteService;
import com.notecastai.note.service.factory.NoteEntityFactory;
import com.notecastai.tag.api.dto.TagDTO;
import com.notecastai.tag.domain.TagEntity;
import com.notecastai.tag.infrastructure.repo.TagRepository;
import com.notecastai.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class NoteServiceImpl implements NoteService {

    private final NoteRepository noteRepository;
    private final UserService userService;
    private final TagRepository tagRepository;
    private final NoteMapper mapper;
    private final NoteExportService noteExportService;
    private final NoteAiEditor noteAiEditor;
    private final NoteAiChat noteAiChat;
    private final NoteEntityFactory noteEntityFactory;

    @Override
    @Transactional
    public NoteDTO create(CreateNoteCommand command) {
        NewNoteAiResponse aiResponse = noteAiEditor.adjustNote(command);

        NoteEntity entity = noteEntityFactory.createFromAiResponse(command, aiResponse);

        log.info("Creating Note: title={}, tags={}, actions={}",
                entity.getTitle(), entity.getTags().size(), entity.getProposedAiActions().size());

        NoteEntity saved = noteRepository.save(entity);
        return mapper.toDto(saved);
    }

    @Override
    @Transactional
    public NoteDTO updateManual(Long id, UpdateNoteManualCommand command) {
        NoteEntity entity = noteRepository.getOrThrow(id);
        OwnershipVerifier.verify(entity.getUser().getId());

        if (command.getTitle() != null) {
            entity.setTitle(command.getTitle());
        }

        if (command.getKnowledgeBase() != null) {
            entity.setKnowledgeBase(command.getKnowledgeBase());
        }

        if (command.getTagIds() != null) {
            Long currentUserId = userService.getCurrentUserId();
            Set<TagEntity> tags = tagRepository.resolveAndValidateForUser(command.getTagIds(), currentUserId);
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
    @Transactional(readOnly = true)
    public Page<NoteShortDTO> findAllShort(NotesQueryParam params, Pageable pageable) {
        Page<NoteEntity> notes = noteRepository.findAll(params, pageable);
        return notes.map(this::toNoteShortDTO);
    }

    @Override
    @Transactional
    public NoteDTO formatNoteKnowledgeBase(Long noteId, FormatNoteKnowledgeCommand command) {
        NoteEntity entity = noteRepository.getOrThrow(noteId);
        OwnershipVerifier.verify(entity.getUser().getId());

        // Call AI with retry logic
        FormatNoteAiResponse aiResponse = noteAiEditor.formatNoteKnowledgeBase(noteId, command);

        entity.setTitle(aiResponse.getAdjustedTitle());
        entity.setKnowledgeBase(aiResponse.getKnowledgeBase());

        if (!aiResponse.getTagIds().isEmpty()) {
            entity.setTags(noteEntityFactory.resolveTagsByIds(aiResponse.getTagIds()));
        }
        entity.setProposedAiActions(noteEntityFactory.mapAiActions(aiResponse.getProposedAiActions()));

        NoteEntity saved = noteRepository.save(entity);
        log.info("Note formatted: id={}, title={}, tags={}, actions={}",
                saved.getId(), saved.getTitle(), saved.getTags().size(), saved.getProposedAiActions().size());

        return mapper.toDto(saved);
    }

    @Override
    @Transactional
    public NoteDTO formatNote(Long noteId, FormatNoteCommand command) {
        NoteEntity note = noteRepository.getOrThrow(noteId);
        OwnershipVerifier.verify(note.getUser().getId());

        var format = command.getFormatType() == null ? FormatType.DEFAULT : command.getFormatType();

        var cmd = CreateNoteCommand.builder()
                .title(note.getTitle())
                .tagIds(note.getTags().stream().map(TagEntity::getId).collect(Collectors.toList()))
                .knowledgeBase(note.getKnowledgeBase())
                .formatType(format)
                .instructions(command.getInstructions())
                .build();

        NewNoteAiResponse aiResponse = noteAiEditor.adjustNote(cmd);

        note.setFormattedNote(aiResponse.getFormattedNote());
        note.setCurrentFormat(format);
        note.setProposedAiActions(noteEntityFactory.mapAiActions(aiResponse.getProposedAiActions()));

        NoteEntity saved = noteRepository.save(note);
        log.info("Note adjusted: id={}, title={}, tags={}, actions={}",
                saved.getId(), saved.getTitle(), saved.getTags().size(), saved.getProposedAiActions().size());

        return mapper.toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public NoteQuestionResponse askQuestion(Long noteId, AskNoteQuestionCommand command) {
        NoteEntity entity = noteRepository.getOrThrow(noteId);
        OwnershipVerifier.verify(entity.getUser().getId());
        return noteAiChat.askQuestion(noteId, command);
    }

    @Override
    @Transactional(readOnly = true)
    public NoteDTO getById(Long id) {
        NoteEntity entity = noteRepository.getOrThrow(id);
        OwnershipVerifier.verify(entity.getUser().getId());
        return mapper.toDto(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<NoteFormatTypeDTO> listFormats() {
        return Arrays.stream(FormatType.values())
                .map(ft -> NoteFormatTypeDTO.builder()
                        .code(ft.name())
                        .label(ft.getLabel())
                        .promptText(ft.getPromptText())
                        .build())
                .collect(Collectors.toList());
    }


    @Override
    @Transactional
    public NoteDTO combine(CombineNoteCommand command) {
        // Verify ownership of every input note to prevent cross-user data leakage.
        List<NoteEntity> notes = command.getNoteIds().stream()
                .distinct()
                .map(id -> {
                    NoteEntity note = noteRepository.getOrThrow(id);
                    OwnershipVerifier.verify(note.getUser().getId());
                    return note;
                })
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

        // Create a new note command with combined knowledge base

        log.debug("combined knowledge base: {}", combinedKnowledgeBase);

        CreateNoteCommand createCommand = CreateNoteCommand.builder()
                .title(command.getTitle())
                .knowledgeBase(combinedKnowledgeBase)
                .tagIds(command.getTagIds())
                .type(com.notecastai.note.domain.NoteType.COMBINED)
                .formatType(command.getFormatType())
                .adjustTagsWithAi(command.isAdjustTagsWithAi())
                .adjustTitleWithAi(command.isAdjustTitleWithAi())
                .instructions(command.getInstructions())
                .build();

        log.info("Creating combined note from {} notes", notes.size());

        return create(createCommand);
    }

    @Override
    @Transactional
    public NoteDTO addTag(Long noteId, Long tagId) {
        NoteEntity note = noteRepository.getOrThrow(noteId);
        OwnershipVerifier.verify(note.getUser().getId());

        Long currentUserId = userService.getCurrentUserId();
        TagEntity tag = tagRepository.findByIdAndUserOrThrow(tagId, currentUserId);

        note.addTag(tag);

        NoteEntity savedNote = noteRepository.save(note);
        log.info("Tag {} added to note {}", tagId, noteId);

        return mapper.toDto(savedNote);
    }

    @Override
    @Transactional
    public NoteDTO removeTag(Long noteId, Long tagId) {
        NoteEntity note = noteRepository.getOrThrow(noteId);
        OwnershipVerifier.verify(note.getUser().getId());

        note.removeTagById(tagId);

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
                                .userId(tag.getUser().getId())
                                .name(tag.getName())
                                .build())
                        .toList())
                .createdDate(entity.getCreatedDate())
                .updatedDate(entity.getUpdatedDate())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] exportNote(Long noteId, ExportFormat format) {
        NoteEntity note = noteRepository.getOrThrow(noteId);
        OwnershipVerifier.verify(note.getUser().getId());

        String content = buildExportContent(note, format);

        if (format == ExportFormat.MD) {
            return noteExportService.exportAsMarkdown(mapper.toDto(note), content);
        } else if (format == ExportFormat.TXT) {
            return noteExportService.exportAsText(mapper.toDto(note), content);
        } else if (format == ExportFormat.HTML) {
            return noteExportService.exportAsHtml(mapper.toDto(note), content);
        } else if (format == ExportFormat.PDF) {
            return noteExportService.exportAsPdf(mapper.toDto(note), content);
        } else if (format == ExportFormat.DOCX) {
            return noteExportService.exportAsDocx(mapper.toDto(note), content);
        }

        // This should never happen due to enum validation
        throw BusinessException.of(BusinessException.BusinessCode.INVALID_REQUEST
                .append(" Unsupported export format: " + format)
        );
    }

    private String buildExportContent(NoteEntity note, ExportFormat format) {
        // Build content based on what's available in the note
        if (note.getFormattedNote() != null && !note.getFormattedNote().isEmpty()) {
            return note.getFormattedNote();
        } else if (note.getKnowledgeBase() != null && !note.getKnowledgeBase().isEmpty()) {
            return note.getKnowledgeBase();
        } else {
            return "# " + note.getTitle() + "\n\nNo content available.";
        }
    }

    @Override
    @Transactional
    public NoteDTO cloneNote(Long noteId, String newTitle, boolean includeFormattedNote) {
        NoteEntity originalNote = noteRepository.getOrThrow(noteId);
        OwnershipVerifier.verify(originalNote.getUser().getId());

        // Create the cloned note
        NoteEntity clonedNote = NoteEntity.builder()
                .user(userService.getCurrentUserReference())
                .title(newTitle != null ? newTitle : "Copy of " + originalNote.getTitle())
                .knowledgeBase(originalNote.getKnowledgeBase())
                .formattedNote(includeFormattedNote ? originalNote.getFormattedNote() : null)
                .currentFormat(originalNote.getCurrentFormat())
                .type(originalNote.getType())
                .tags(new HashSet<>(originalNote.getTags())) // Copy tags
                .build();

        clonedNote = noteRepository.save(clonedNote);

        log.info("Cloned note {} to new note {}", noteId, clonedNote.getId());

        return mapper.toDto(clonedNote);
    }

}