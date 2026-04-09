package com.notecastai.integration.ai;

import com.notecastai.integration.ai.dto.FormatNoteAiResponse;
import com.notecastai.integration.ai.dto.NewNoteAiResponse;
import com.notecastai.note.service.command.CreateNoteCommand;
import com.notecastai.note.service.command.FormatNoteKnowledgeCommand;

public interface NoteAiEditor {

    NewNoteAiResponse adjustNote(CreateNoteCommand command);

    FormatNoteAiResponse formatNoteKnowledgeBase(Long noteId, FormatNoteKnowledgeCommand command);

}
