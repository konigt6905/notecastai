package com.notecastai.integration.ai.impl;

import com.notecastai.integration.ai.NoteAiChat;
import com.notecastai.note.api.dto.NoteQuestionRequest;
import com.notecastai.note.api.dto.NoteQuestionResponse;
import org.springframework.stereotype.Service;

@Service
public class NoteAiChatImpl implements NoteAiChat {

    @Override
    public NoteQuestionResponse askQuestion(Long noteId, NoteQuestionRequest request) {
        return null;
    }
}
