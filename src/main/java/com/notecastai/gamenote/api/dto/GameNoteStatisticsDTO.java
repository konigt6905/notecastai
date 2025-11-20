package com.notecastai.gamenote.api.dto;

import com.notecastai.gamenote.domain.QuestionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GameNoteStatisticsDTO {

    private Long id;
    private Long gameNoteId;
    private Long userId;

    // Attempt tracking
    private Integer attemptNumber;
    private Boolean completed;
    private Instant completedAt;

    // Question type
    private QuestionType questionType;

    // Core metrics
    private Integer totalQuestions;
    private Integer questionsAttempted;
    private Integer questionsCorrect;
    private BigDecimal correctnessPercentage;

    // Time tracking
    private Integer totalTimeSeconds;
    private BigDecimal averageTimePerQuestion;

    // Scoring
    private Integer finalScore;
    private Map<String, Object> scoreBreakdown;

    // Type-specific statistics
    private Map<String, Object> typeSpecificStats;

    // Audit
    private Instant createdDate;
}
