package com.notecastai.notecast.api.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NoteCastStyleDTO {

    @NotNull
    private String code;
    @NotNull
    private String label;
    @NotNull
    private String promptText;
}
