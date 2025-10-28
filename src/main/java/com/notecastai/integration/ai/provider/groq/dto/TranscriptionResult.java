package com.notecastai.integration.ai.provider.groq.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class TranscriptionResult {

    private String transcript;
    private String language;
    private Integer durationSeconds;
    private List<WordTimestamp> wordTimestamps;
    private List<SegmentTimestamp> segmentTimestamps;
    private TranscriptionMetadata metadata;

}
