package com.notecastai.notecast.api.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class NoteCastStyleDTO {

    @NotNull
    private String code;
    @NotNull
    private String label;
    @NotNull
    private String promptText;
}
