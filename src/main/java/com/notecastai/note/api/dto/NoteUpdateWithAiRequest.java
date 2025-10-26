package com.notecastai.note.api.dto;


import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class NoteUpdateWithAiRequest {
    @NotNull
    private Long userId;
    @NotNull
    private String title;
    @NotNull
    private String knowledgeBase;
    @NotNull
    private String aiInstructions;
}