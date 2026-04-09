package com.notecastai.gamenote.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.notecastai.gamenote.domain.*;
import com.notecastai.tag.api.dto.TagDTO;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GameNoteShortDTO {

    @NotNull
    private Long id;

    @NotNull
    private String title;

    @NotNull
    private GameNoteStatus status;

    @NotNull
    private Integer numberOfQuestions;

    @NotNull
    private QuestionType questionType;

    @NotNull
    private DifficultyLevel difficulty;

    private String sourceNoteTitle;

    private List<TagDTO> tags;

    @NotNull
    private Instant createdDate;

    // Statistics summary (for list view)
    private Integer totalAttempts;
    private Integer lastScore;
    private Integer bestScore;
    private String masteryLevel;  // LEARNING, PRACTICING, MASTERED, or null if no attempts

}
