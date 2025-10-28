package com.notecastai.notecast.api.dto;

import com.notecastai.notecast.domain.NoteCastStyle;
import com.notecastai.notecast.domain.TranscriptSize;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class NoteCastCreateRequest {

    @NotNull
    private Long noteId;
    @NotNull
    private NoteCastStyle style;
    @NotNull
    private TranscriptSize size;

}