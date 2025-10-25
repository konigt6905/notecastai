package com.notecastai.tag.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class TagCreateRequest {

    @NotNull
    Long userId;

    @NotBlank
    String name;
}