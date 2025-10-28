package com.notecastai.voicenote.api.dto;

import com.notecastai.note.api.dto.NoteDTO;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UploadVoiceNoteResponse {
    private VoiceNoteDTO voiceNote;
    private NoteDTO note;
}
