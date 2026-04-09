package com.notecastai.voicenote.service.command;

import com.notecastai.note.domain.FormatType;
import com.notecastai.voicenote.api.dto.TranscriptionLanguage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateVoiceNoteCommand {
    private byte[] audioBytes;
    private String originalFilename;
    private String contentType;
    private long fileSize;
    private List<Long> tagIds;
    private String title;
    private String userInstructions;
    private TranscriptionLanguage language;
    private FormatType formatType;
}
