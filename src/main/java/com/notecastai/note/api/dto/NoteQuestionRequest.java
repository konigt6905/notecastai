package com.notecastai.note.api.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class NoteQuestionRequest {

    @NotNull
    private String question;
    private List<ChatMessage> history; // Optional chat history

}
