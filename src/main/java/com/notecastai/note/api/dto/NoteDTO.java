package com.notecastai.note.api.dto;

import com.notecastai.tag.api.dto.TagDTO;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class NoteDTO {

    @NotNull
    private Long id;
    @NotNull
    private Long userId;
    @NotNull
    private String title;
    @NotNull
    private String knowledgeBase;
    @NotNull
    private String formattedNote;
    @NotNull
    private List<TagDTO> tags;
    @NotNull
    private LocalDateTime createdDate;
    @NotNull
    private LocalDateTime updatedDate;
    private List<AiActionDto> proposedAiActions;
    @NotNull
    private Boolean inactive;
}