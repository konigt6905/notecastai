package com.notecastai.integration.ai;

import com.notecastai.note.api.dto.NoteQuestionResponse;
import com.notecastai.note.service.command.AskNoteQuestionCommand;

public interface NoteAiChat {

    NoteQuestionResponse askQuestion(Long noteId, AskNoteQuestionCommand command);

}
