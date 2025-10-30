package com.notecastai.note.service;

import com.notecastai.note.api.dto.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface NoteService {

    NoteDTO create(NoteCreateRequest request);

    NoteDTO updateManual(Long id, NoteAdjustManualRequest request);

    Page<NoteDTO> findAll(NotesQueryParam params, Pageable pageable);

    List<NoteFormatTypeDTO> listFormats();

    NoteDTO formateNoteKnowledgeBase(Long noteId, NoteFormatRequest  request);

    NoteQuestionResponse askQuestion(Long id, NoteQuestionRequest request);

    NoteDTO getById(Long id);

    Page<NoteShortDTO> findAllShort(NotesQueryParam params, Pageable pageable);

    NoteDTO addTag(Long noteId, Long tagId);

    NoteDTO removeTag(Long noteId, Long tagId);

    NoteDTO combine(NoteCombineRequest request);
}