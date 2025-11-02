package com.notecastai.note.api.dto;

import com.notecastai.note.domain.FormateType;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NoteCombineRequest {

    @NotEmpty(message = "At least one note ID is required")
    @Size(min = 1, max = 30, message = "You can combine between 1 and 30 notes")
    private List<Long> noteIds;

    // Optional
    private String title;

    // Optional
    private List<Long> tagIds;

    // Optional
    private FormateType formateType;

    private boolean autoAiFormate;

    // Optional
    private String instructions;
}
