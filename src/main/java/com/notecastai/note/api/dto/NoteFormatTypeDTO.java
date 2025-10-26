package com.notecastai.note.api.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class NoteFormatTypeDTO {

    private String code;
    private String label;
    private String promptText;
}