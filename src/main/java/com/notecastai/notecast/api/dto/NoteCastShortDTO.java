package com.notecastai.notecast.api.dto;

import com.notecastai.notecast.domain.NoteCastStatus;
import com.notecastai.notecast.domain.NoteCastStyle;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class NoteCastShortDTO {

    @NotNull
    private Long id;
    @NotNull
    private Long noteId;
    @NotNull
    private String noteTitle;
    @NotNull
    private NoteCastStatus status;
    @NotNull
    private NoteCastStyle style;
    @NotNull
    private Instant createdDate;
}
