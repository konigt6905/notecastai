package com.notecastai.integration.ai.provider.groq.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SegmentTimestamp {

    private Integer id;
    private String text;
    private Double startTime;
    private Double endTime;

    private Double averageLogProbability;
    private Double compressionRatio;
    private Double noSpeechProbability;
}