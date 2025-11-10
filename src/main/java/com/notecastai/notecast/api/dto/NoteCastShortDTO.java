package com.notecastai.notecast.api.dto;

import com.notecastai.notecast.domain.NoteCastStatus;
import com.notecastai.notecast.domain.NoteCastStyle;
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
public class NoteCastShortDTO {

    @NotNull
    private Long id;
    @NotNull
    private Long noteId;
    @NotNull
    private String title;
    @NotNull
    private NoteCastStatus status;
    @NotNull
    private NoteCastStyle style;
    private List<TagDTO> tags;
    @NotNull
    private Instant createdDate;
}
