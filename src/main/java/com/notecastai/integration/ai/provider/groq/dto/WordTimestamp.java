package com.notecastai.integration.ai.provider.groq.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class WordTimestamp {

    private String word;
    private Double startTime; // seconds
    private Double endTime;   // seconds
}
