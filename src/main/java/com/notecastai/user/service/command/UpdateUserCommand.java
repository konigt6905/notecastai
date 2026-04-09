package com.notecastai.user.service.command;

import com.notecastai.note.domain.FormatType;
import com.notecastai.notecast.domain.TtsVoice;
import com.notecastai.voicenote.api.dto.TranscriptionLanguage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserCommand {
    private FormatType defaultFormat;
    private TtsVoice defaultVoice;
    private TranscriptionLanguage preferredLanguage;
}
