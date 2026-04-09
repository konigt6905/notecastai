package com.notecastai.note.service.command;

import com.notecastai.note.domain.FormatType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CombineNoteCommand {
    private List<Long> noteIds;
    private String title;
    private List<Long> tagIds;
    private FormatType formatType;
    private boolean adjustTitleWithAi;
    private boolean adjustTagsWithAi;
    private String instructions;
}
