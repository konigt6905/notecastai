package com.notecastai.integration.ai;

import com.notecastai.integration.ai.dto.GameNoteAiRequest;
import com.notecastai.integration.ai.dto.GameNoteAiResponse;

public interface GameNoteAiGenerator {

    GameNoteAiResponse generateGameQuestions(GameNoteAiRequest request);

}
