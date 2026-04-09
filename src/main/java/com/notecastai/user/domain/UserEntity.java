package com.notecastai.user.domain;

import com.notecastai.common.BaseEntity;
import com.notecastai.note.domain.FormatType;
import com.notecastai.notecast.domain.TtsVoice;
import com.notecastai.voicenote.api.dto.TranscriptionLanguage;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "user", indexes = {
        @Index(name = "idx_user_clerk", columnList = "clerk_user_id", unique = true)
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class UserEntity extends BaseEntity {

    @Column(name = "clerk_user_id", nullable = false, unique = true)
    private String clerkUserId;

    @Column(name = "email")
    private String email;

    @Builder.Default
    @Column(name = "email_verified")
    private Boolean emailVerified = false;

    @Column(name = "full_name")
    private String fullName;

    @Column(name = "given_name", length = 100)
    private String givenName;

    @Column(name = "family_name", length = 100)
    private String familyName;

    @Column(name = "picture_url", columnDefinition = "TEXT")
    private String pictureUrl;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(name = "default_format", nullable = false, length = 40)
    private FormatType defaultFormat = FormatType.DEFAULT;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(name = "default_voice", nullable = false, length = 40)
    private TtsVoice defaultVoice = TtsVoice.getDefault();

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(name = "preferred_language")
    private TranscriptionLanguage preferredLanguage = TranscriptionLanguage.AUTO;

}