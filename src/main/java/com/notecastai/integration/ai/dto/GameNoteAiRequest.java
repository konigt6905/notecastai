package com.notecastai.integration.ai.dto;

import com.notecastai.gamenote.domain.*;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GameNoteAiRequest {
    private String noteTitle;
    private String noteContent;
    private Integer numberOfQuestions;
    private QuestionLength questionLength;
    private AnswerLength answerLength;
    private DifficultyLevel difficulty;
    private QuestionType questionType;
    private String customInstructions;
}
