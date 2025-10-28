package com.notecastai.integration.ai.provider.openrouter.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OpenRouterResponseFormat {

    private String type; // "json_object" or "json_schema"

    @JsonProperty("json_schema")
    private OpenRouterJsonSchema jsonSchema;
}