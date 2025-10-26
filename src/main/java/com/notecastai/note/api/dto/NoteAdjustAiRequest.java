package com.notecastai.note.api.dto;

import com.notecastai.note.domain.FormateType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public class NoteAdjustAiRequest {

    @NotNull
    private Long noteId;
    @NotNull
    private Long userId;
    @NotBlank
    private String instructions;
    // Optional
    private FormateType formateType;

}