package com.notecastai.note.service.command;

import com.notecastai.note.domain.FormatType;
import com.notecastai.note.domain.NoteType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateNoteCommand {
    private String title;
    private String knowledgeBase;
    private List<Long> tagIds;
    private NoteType type;
    private FormatType formatType;
    private String instructions;
    private boolean adjustTitleWithAi;
    private boolean adjustTagsWithAi;
}
