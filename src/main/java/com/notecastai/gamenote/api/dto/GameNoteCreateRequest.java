package com.notecastai.gamenote.api.dto;

import com.notecastai.gamenote.domain.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GameNoteCreateRequest {

    @NotNull(message = "Source note ID is required")
    private Long sourceNoteId;

    @Size(max = 500, message = "Title must not exceed 500 characters")
    private String title;

    @NotNull(message = "Difficulty is required")
    private DifficultyLevel difficulty;

    @NotNull(message = "Question type is required")
    private QuestionType questionType;

    @Size(max = 2000, message = "Custom instructions must not exceed 2000 characters")
    private String customInstructions;

}
