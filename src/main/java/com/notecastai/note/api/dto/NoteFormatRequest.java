package com.notecastai.note.api.dto;


import com.notecastai.note.domain.FormateType;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class NoteFormatRequest {
    private FormateType formateType; // optional
    private String instructions;  // optional
}