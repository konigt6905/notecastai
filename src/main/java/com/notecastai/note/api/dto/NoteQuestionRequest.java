package com.notecastai.note.api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class NoteQuestionRequest {

    @NotBlank(message = "Question cannot be empty")
    @Size(max = 2000, message = "Question too long (maximum 2000 characters)")
    private String question;

    @Valid
    @Size(max = 50, message = "Chat history too long (maximum 50 messages)")
    private List<ChatMessage> history; // Optional chat history

}
