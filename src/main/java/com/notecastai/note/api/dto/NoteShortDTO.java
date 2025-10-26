package com.notecastai.note.api.dto;

import com.notecastai.tag.api.dto.TagDTO;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.List;

@Data
@Builder
public class NoteShortDTO {

    @NotNull
    private Long id;
    @NotNull
    private String title;
    @NotNull
    private List<TagDTO> tags;
    @NotNull
    private Instant createdDate;
    @NotNull
    private Instant updatedDate;

}