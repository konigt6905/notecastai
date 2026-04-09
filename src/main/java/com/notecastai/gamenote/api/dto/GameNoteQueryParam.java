package com.notecastai.gamenote.api.dto;

import com.notecastai.gamenote.domain.DifficultyLevel;
import com.notecastai.gamenote.domain.GameNoteStatus;
import com.notecastai.gamenote.domain.QuestionType;
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
public class GameNoteQueryParam {

    private Long userId;
    private String search;
    private GameNoteStatus status;
    private QuestionType questionType;
    private DifficultyLevel difficulty;
    private List<Long> tagIds;
    private Instant from;
    private Instant to;

}
