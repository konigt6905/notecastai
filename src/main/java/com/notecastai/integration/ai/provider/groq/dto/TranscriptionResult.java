package com.notecastai.integration.ai.provider.groq.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TranscriptionResult {

    private String transcript;
    private String language;
    private Integer durationSeconds;
    private List<WordTimestamp> wordTimestamps;
    private List<SegmentTimestamp> segmentTimestamps;
    private TranscriptionMetadata metadata;

}
