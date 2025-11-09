package com.notecastai.integration.ai.dto;

import com.notecastai.notecast.domain.NoteCastStyle;
import com.notecastai.notecast.domain.TranscriptSize;
import com.notecastai.notecast.domain.TtsVoice;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TextToSpeechRequest {

    private Long referenceId;
    private String transcript;
    private TtsVoice voice;
    private NoteCastStyle style;
    private TranscriptSize size;

    @Builder.Default
    private TextToSpeechFormat format = TextToSpeechFormat.MP3;
}
