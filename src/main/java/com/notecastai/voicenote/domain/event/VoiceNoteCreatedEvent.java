package com.notecastai.voicenote.domain.event;

import com.notecastai.note.domain.FormatType;
import com.notecastai.voicenote.api.dto.TranscriptionLanguage;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Getter
@Builder
@RequiredArgsConstructor
public class VoiceNoteCreatedEvent {

    private final Long voiceNoteId;
    private final byte[] audioBytes;
    private final String originalFilename;
    private final String contentType;
    private final long fileSize;
    private final TranscriptionLanguage preferredLanguage;

    // Fields needed to create the Note after transcription
    private final String title;
    private final String userInstructions;
    private final List<Long> tagIds;
    private final FormatType formatType;
}
