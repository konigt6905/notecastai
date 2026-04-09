package com.notecastai.integration.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SegmentTimestamp {

    private Integer id;
    private String text;
    private Double startTime;
    private Double endTime;

    private Double averageLogProbability;
    private Double compressionRatio;
    private Double noSpeechProbability;
}
