package com.notecastai.note.api.dto;


import com.notecastai.note.domain.FormatType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class NoteKnowledgeFormatRequest {

    private FormatType formatType;
    private String instructions;  // optional
}