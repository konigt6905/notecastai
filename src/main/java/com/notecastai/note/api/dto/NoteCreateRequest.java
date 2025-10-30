package com.notecastai.note.api.dto;

import com.notecastai.note.domain.FormateType;
import com.notecastai.note.domain.NoteType;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class NoteCreateRequest {

    // Optional;
    private String title;
    @NotNull
    private String knowledgeBase;
    // Optional
    private List<Long> tagIds;
    private NoteType type;
    // Optional
    private FormateType formateType;
    private boolean autoAiFormate;
    // Optional
    private String instructions;

}
