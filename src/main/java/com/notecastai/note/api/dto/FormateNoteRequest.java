package com.notecastai.note.api.dto;

import com.notecastai.note.domain.FormateType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FormateNoteRequest {

    // Optional
    private FormateType formateType;
    // Optional
    private String instructions;
}
