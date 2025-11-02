package com.notecastai.voicenote.api.dto;

import com.notecastai.note.api.dto.NoteDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UploadVoiceNoteResponse {
    private VoiceNoteDTO voiceNote;
    private NoteDTO note;
}
