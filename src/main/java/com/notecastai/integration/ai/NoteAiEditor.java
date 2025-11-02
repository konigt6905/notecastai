package com.notecastai.integration.ai;

import com.notecastai.integration.ai.provider.openrouter.dto.FormatNoteAiResponse;
import com.notecastai.integration.ai.provider.openrouter.dto.NewNoteAiResponse;
import com.notecastai.note.api.dto.CreateNoteRequest;
import com.notecastai.note.api.dto.NoteKnowledgeFormatRequest;

public interface NoteAiEditor {

    NewNoteAiResponse adjustNote(CreateNoteRequest request);

    FormatNoteAiResponse formatNoteKnowledgeBase(Long noteId, NoteKnowledgeFormatRequest request);

}