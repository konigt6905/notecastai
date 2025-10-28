package com.notecastai.integration.ai;

import com.notecastai.integration.ai.provider.openrouter.dto.FormatNoteAiResponse;
import com.notecastai.integration.ai.provider.openrouter.dto.NewNoteAiResponse;
import com.notecastai.note.api.dto.NoteCreateRequest;
import com.notecastai.note.api.dto.NoteFormatRequest;

public interface NoteAiEditor {

    NewNoteAiResponse adjustNewNote(NoteCreateRequest request);

    FormatNoteAiResponse formatNote(Long noteId, NoteFormatRequest request);
}