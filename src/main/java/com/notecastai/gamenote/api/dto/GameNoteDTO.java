package com.notecastai.gamenote.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.notecastai.gamenote.domain.*;
import com.notecastai.note.api.dto.NoteDTO;
import com.notecastai.tag.api.dto.TagDTO;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@With
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GameNoteDTO {

    @NotNull
    private Long id;

    @NotNull
    private Long userId;

    private NoteDTO sourceNote;

    @NotNull
    private String title;

    @NotNull
    private GameNoteStatus status;

    @NotNull
    private Integer numberOfQuestions;

    @NotNull
    private QuestionLength questionLength;

    @NotNull
    private AnswerLength answerLength;

    @NotNull
    private DifficultyLevel difficulty;

    @NotNull
    private QuestionType questionType;

    private String customInstructions;

    private List<GameQuestionDTO> questions;

    private String errorMessage;

    private List<TagDTO> tags;

    @NotNull
    private Instant createdDate;

    @NotNull
    private Instant updatedDate;

    // Statistics summary (for detail view)
    private GameNoteStatsSummary statistics;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class GameNoteStatsSummary {
        private Integer totalAttempts;
        private Integer completedAttempts;
        private String masteryLevel;  // LEARNING, PRACTICING, MASTERED, or null if no attempts

        // Latest attempt
        private Integer lastScore;
        private Instant lastAttemptDate;
        private Boolean lastAttemptCompleted;

        // Best attempt
        private Integer bestScore;
        private Instant bestScoreDate;

        // Averages
        private Integer averageScore;
        private Integer averageTimeSeconds;
    }

}
