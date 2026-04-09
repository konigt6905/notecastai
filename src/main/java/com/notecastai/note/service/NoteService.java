package com.notecastai.note.service;

import com.notecastai.note.api.dto.*;
import com.notecastai.note.domain.ExportFormat;
import com.notecastai.note.service.command.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface NoteService {

    NoteDTO create(CreateNoteCommand command);

    NoteDTO updateManual(Long id, UpdateNoteManualCommand command);

    Page<NoteDTO> findAll(NotesQueryParam params, Pageable pageable);

    List<NoteFormatTypeDTO> listFormats();

    NoteDTO formatNoteKnowledgeBase(Long noteId, FormatNoteKnowledgeCommand command);

    NoteDTO formatNote(Long noteId, FormatNoteCommand command);

    NoteQuestionResponse askQuestion(Long id, AskNoteQuestionCommand command);

    NoteDTO getById(Long id);

    Page<NoteShortDTO> findAllShort(NotesQueryParam params, Pageable pageable);

    NoteDTO addTag(Long noteId, Long tagId);

    NoteDTO removeTag(Long noteId, Long tagId);

    NoteDTO combine(CombineNoteCommand command);

    byte[] exportNote(Long noteId, ExportFormat format);

    NoteDTO cloneNote(Long noteId, String newTitle, boolean includeFormattedNote);
}
