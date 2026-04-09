package com.notecastai.gamenote.service.command;

import com.notecastai.gamenote.domain.DifficultyLevel;
import com.notecastai.gamenote.domain.QuestionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateGameNoteCommand {
    private Long sourceNoteId;
    private String title;
    private DifficultyLevel difficulty;
    private QuestionType questionType;
    private String customInstructions;
}
