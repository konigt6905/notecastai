package com.notecastai.user.api.dto;

import com.notecastai.note.domain.FormateType;
import com.notecastai.notecast.domain.TtsVoice;
import com.notecastai.voicenote.api.dto.TranscriptionLanguage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserUpdateRequest {

    private FormateType defaultFormate;

    private TtsVoice defaultVoice;

    private TranscriptionLanguage preferredLanguage;
}
