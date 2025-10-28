package com.notecastai.integration.ai;

import com.notecastai.note.api.dto.NoteQuestionRequest;
import com.notecastai.note.api.dto.NoteQuestionResponse;

public interface NoteAiChat {

    NoteQuestionResponse askQuestion(Long noteId, NoteQuestionRequest request);

}