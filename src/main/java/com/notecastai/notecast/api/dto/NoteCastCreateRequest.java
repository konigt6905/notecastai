package com.notecastai.notecast.api.dto;

import com.notecastai.notecast.domain.NoteCastStyle;
import com.notecastai.notecast.domain.TranscriptSize;
import com.notecastai.notecast.domain.TtsVoice;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NoteCastCreateRequest {

    @NotNull
    private Long noteId;
    @NotNull
    private NoteCastStyle style;
    @NotNull
    private TranscriptSize size;
    private String customInstructions; // Optional
    private TtsVoice voice; // Optional

}