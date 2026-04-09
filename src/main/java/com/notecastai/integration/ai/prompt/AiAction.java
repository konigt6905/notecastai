package com.notecastai.integration.ai.prompt;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiAction {

    @JsonProperty("name")
    private String name;

    @JsonProperty("prompt")
    private String prompt;
}
