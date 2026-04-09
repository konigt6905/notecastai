package com.notecastai.integration.ai.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.notecastai.gamenote.api.dto.GameQuestionDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GameNoteAiResponse {

    @JsonProperty("questions")
    private List<GameQuestionDTO> questions;

}
