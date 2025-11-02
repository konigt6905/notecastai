package com.notecastai.integration.ai.provider.groq.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WordTimestamp {

    private String word;
    private Double startTime; // seconds
    private Double endTime;   // seconds
}
