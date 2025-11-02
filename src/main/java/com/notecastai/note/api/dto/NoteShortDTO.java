package com.notecastai.note.api.dto;

import com.notecastai.tag.api.dto.TagDTO;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
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