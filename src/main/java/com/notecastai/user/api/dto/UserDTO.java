package com.notecastai.user.api.dto;

import com.notecastai.note.domain.FormateType;
import com.notecastai.voicenote.api.dto.TranscriptionLanguage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
@AllArgsConstructor
public class UserDTO {
    Long id;
    String clerkUserId;
    String email;
    Boolean emailVerified;
    String fullName;
    String givenName;
    String familyName;
    String pictureUrl;
    FormateType defaultFormate;
    PreferredVoice preferredVoice;
    TranscriptionLanguage preferredLanguage;
}