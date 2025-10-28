package com.notecastai.integration.ai.provider.openrouter.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.notecastai.integration.ai.prompt.AiAction;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NewNoteAiResponse {

    @JsonProperty("adjustedTitle")
    private String adjustedTitle;

    @JsonProperty("formattedNote")
    private String formattedNote;

    @JsonProperty("proposedTags")
    private List<String> proposedTags;

    @JsonProperty("proposedAiActions")
    private List<AiAction> proposedAiActions;

    @JsonIgnore
    private List<Long> tagIds; // Mapped after validation

}