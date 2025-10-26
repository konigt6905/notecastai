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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "note_id")
    private NoteEntity note;

    @Column(name = "filename", nullable = false)
    private String filename;

    @Column(name = "original_filename", nullable = false)
    private String originalFilename;

    @Column(name = "content_type", nullable = false)
    private String contentType;

    @Column(name = "file_size", nullable = false)
    private Long fileSize;

    @Column(name = "user_instructions", nullable = false)
    private String userInstructions;

    @Column(name = "s3_path", nullable = false)
    private String s3Path;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private AudioStatus status;

    @Lob
    @Column(name = "transcript")
    private String transcript;

    @Column(name = "language", length = 10)
    private String language;

    @Column(name = "duration_seconds")
    private Integer durationSeconds;

    @Lob
    @Column(name = "error_message")
    private String errorMessage;

}