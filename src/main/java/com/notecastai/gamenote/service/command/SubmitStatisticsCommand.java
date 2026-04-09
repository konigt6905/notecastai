package com.notecastai.gamenote.service.command;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubmitStatisticsCommand {
    private Integer attemptNumber;
    private Boolean completed;
    private Integer totalTimeSeconds;
    private List<QuestionAnswer> answers;
    private Map<String, Object> typeSpecificData;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QuestionAnswer {
        private Long questionId;
        private String selectedAnswer;
        private String correctAnswer;
        private Boolean isCorrect;
        private Double timeSpent;
        private List<String> eliminatedOptions;
        private Integer changeCount;
        private String confidence;
        private Integer selfScore;
        private String userAnswer;
        private Integer revisionCount;
    }
}
