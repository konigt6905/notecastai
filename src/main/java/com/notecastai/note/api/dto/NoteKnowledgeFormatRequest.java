package com.notecastai.note.api.dto;


import com.notecastai.note.domain.FormateType;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class NoteKnowledgeFormatRequest {

    private FormateType formateType;
    private String instructions;  // optional
}