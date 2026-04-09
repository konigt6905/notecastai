package com.notecastai.note.api.dto;

import com.notecastai.note.domain.FormatType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FormatNoteRequest {

    // Optional
    private FormatType formatType;
    // Optional
    private String instructions;
}
