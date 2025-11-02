package com.notecastai.note.api.dto;

import com.notecastai.note.domain.FormateType;
import com.notecastai.note.domain.NoteType;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateNoteRequest {

    // Optional;
    private String title;
    @NotNull
    private String knowledgeBase;
    // Optional
    private List<Long> tagIds;
    private NoteType type;
    // Optional
    private FormateType formateType;
    // Optional
    private String instructions;
    private boolean adjustTitleWithAi;
    private boolean adjustTagsWithAi;
}
