package com.notecastai.user.api.dto;

import com.notecastai.note.domain.FormateType;
import com.notecastai.voicenote.api.dto.TranscriptionLanguage;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserUpdateRequest {

    private FormateType defaultFormate;

    private PreferredVoice preferredVoice;

    private TranscriptionLanguage preferredLanguage;
}
