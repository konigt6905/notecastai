package com.notecastai.note.api.dto;


import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class NoteUpdateManualRequest {
    @NotNull
    private Long userId;

    private String title;
    private String knowledgeBase;
    private List<Long> tagIds;

    // Optional AI instructions to rewrite knowledgeBase.
    private String aiInstructions;
}