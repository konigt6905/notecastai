package com.notecastai.integration.ai;

import com.notecastai.integration.ai.dto.AiAdjustedNote;
import com.notecastai.note.api.dto.NoteCreateRequest;
import com.notecastai.note.api.dto.NoteFormatRequest;

public interface NoteAiEditor {

    AiAdjustedNote adjustNewNote(NoteCreateRequest request);

    AiAdjustedNote formateNote(Long noteId, NoteFormatRequest request);

}