package com.notecastai.voicenote.api.dto;

import com.notecastai.voicenote.domain.VoiceNoteStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateVoiceNoteResponse {
    private Long id;
    private VoiceNoteStatus status;
}
