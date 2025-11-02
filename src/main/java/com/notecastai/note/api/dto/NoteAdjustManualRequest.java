package com.notecastai.note.api.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NoteAdjustManualRequest {

    @NotNull
    private Long noteId;

    @NotNull
    private Long userId;

    private String title;
    private String knowledgeBase;
    private List<Long> tagIds;
}
