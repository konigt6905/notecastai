package com.notecastai.note.service.command;

import com.notecastai.note.domain.FormatType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FormatNoteCommand {
    private FormatType formatType;
    private String instructions;
}
