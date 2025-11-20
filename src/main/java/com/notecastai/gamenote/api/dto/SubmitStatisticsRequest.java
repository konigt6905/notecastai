package com.notecastai.gamenote.api.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SubmitStatisticsRequest {

    @NotNull(message = "Attempt number is required")
    @Min(value = 1, message = "Attempt number must be at least 1")
    private Integer attemptNumber;

    @NotNull(message = "Completed status is required")
    private Boolean completed;

    @NotNull(message = "Total time is required")
    @Min(value = 0, message = "Total time cannot be negative")
    private Integer totalTimeSeconds;

    @NotNull(message = "Answers are required")
    private List<QuestionAnswerDTO> answers;

    private Map<String, Object> typeSpecificData;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class QuestionAnswerDTO {

        @NotNull(message = "Question ID is required")
        private Long questionId;

        private String selectedAnswer;

        private String correctAnswer;

        @NotNull(message = "Correctness flag is required")
        private Boolean isCorrect;

        @NotNull(message = "Time spent is required")
        @Min(value = 0, message = "Time spent cannot be negative")
        private Double timeSpent;

        // Optional fields for different question types
        private List<String> eliminatedOptions;
        private Integer changeCount;
        private String confidence;
        private Integer selfScore;
        private String userAnswer;
        private Integer revisionCount;
    }
}
