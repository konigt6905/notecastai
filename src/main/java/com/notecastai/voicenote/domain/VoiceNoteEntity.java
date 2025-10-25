package com.notecastai.voicenote.domain;

import com.notecastai.common.BaseEntity;
import com.notecastai.note.domain.NoteEntity;
import com.notecastai.user.domain.UserEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Entity
@Table(
        name = "audio_file",
        indexes = {
                @Index(name = "idx_audio_user", columnList = "user_id"),
                @Index(name = "idx_audio_status", columnList = "status")
        }
)
public class VoiceNoteEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Column(name = "filename", nullable = false)
    private String filename;

    @Column(name = "user_instructions", nullable = false)
    private String userInstructions;

    @Column(name = "s3_path", nullable = false)
    private String s3Path;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "note_id")
    private NoteEntity note;

    @Lob
    @Column(name = "transcript")
    private String transcript;

    @Column(name = "language")
    private String language;
}