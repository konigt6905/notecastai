package com.notecastai.note.service.command;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateNoteManualCommand {
    private String title;
    private String knowledgeBase;
    private List<Long> tagIds;
}
