package com.notecastai.note.api.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class NoteQuestionResponse {

    @NotNull
    private String responseText;

}