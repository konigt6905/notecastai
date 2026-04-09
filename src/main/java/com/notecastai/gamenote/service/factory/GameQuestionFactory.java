package com.notecastai.gamenote.service.factory;

import com.notecastai.gamenote.api.dto.GameQuestionDTO;
import com.notecastai.gamenote.domain.GameNoteEntity;
import com.notecastai.gamenote.domain.GameQuestionEntity;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Builds {@link GameQuestionEntity} instances from AI-generated question DTOs.
 */
@Component
public class GameQuestionFactory {

    /**
     * Creates question entities from DTOs and attaches them to the parent game note.
     * Handles ordering and options list initialization.
     */
    public List<GameQuestionEntity> createFromDtos(GameNoteEntity gameNote, List<GameQuestionDTO> questions) {
        List<GameQuestionEntity> entities = new ArrayList<>(questions.size());
        int order = 0;

        for (GameQuestionDTO dto : questions) {
            GameQuestionEntity entity = GameQuestionEntity.builder()
                    .gameNote(gameNote)
                    .questionOrder(order++)
                    .type(dto.getType())
                    .questionText(dto.getQuestionText())
                    .options(dto.getOptions() != null
                            ? new ArrayList<>(dto.getOptions())
                            : new ArrayList<>())
                    .correctAnswer(dto.getCorrectAnswer())
                    .answer(dto.getAnswer())
                    .explanation(dto.getExplanation())
                    .hint(dto.getHint())
                    .build();

            entities.add(entity);
        }

        return entities;
    }
}
