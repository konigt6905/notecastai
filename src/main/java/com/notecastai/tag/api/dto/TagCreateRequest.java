package com.notecastai.tag.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TagCreateRequest {

    @NotNull
    private Long userId;

    @NotBlank
    private String name;
}