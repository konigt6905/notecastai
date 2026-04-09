package com.notecastai.gamenote.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GameNoteStatisticsSummaryDTO {

    private Long gameNoteId;
    private String gameNoteTitle;

    // Attempt summary
    private Integer totalAttempts;
    private Integer completedAttempts;

    // Best performance
    private Integer bestScore;
    private Instant bestScoreDate;

    // Latest performance
    private Integer latestScore;
    private Instant latestAttemptDate;

    // Averages
    private BigDecimal averageScore;
    private BigDecimal averageCorrectness;
    private Integer averageTimeSeconds;

    // Progress indicators
    private String masteryLevel; // LEARNING, PRACTICING, MASTERED
    private BigDecimal improvementRate; // Percentage improvement from first to last attempt
}
