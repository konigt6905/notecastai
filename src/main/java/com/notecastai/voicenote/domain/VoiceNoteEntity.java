package com.notecastai.voicenote.domain;

import com.notecastai.common.BaseEntity;
import com.notecastai.note.domain.NoteEntity;
import com.notecastai.tag.domain.TagEntity;
import com.notecastai.user.domain.UserEntity;
import com.notecastai.voicenote.api.dto.TranscriptionLanguage;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.HashSet;
import java.util.Set;

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

    @ManyToOne(optional = false)
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

    @Column(name = "user_instructions")
    private String userInstructions;

    @Column(name = "s3_file_url", nullable = false)
    private String s3FileUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private VoiceNoteStatus status;

    @Column(name = "transcript", columnDefinition = "TEXT")
    private String transcript;

    @Basic(fetch = FetchType.LAZY)
    @Column(name = "word_timestamps_json", columnDefinition = "TEXT")
    private String wordTimestampsJson;

    @Basic(fetch = FetchType.LAZY)
    @Column(name = "segment_timestamps_json", columnDefinition = "TEXT")
    private String segmentTimestampsJson;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(name = "language")
    private TranscriptionLanguage language = TranscriptionLanguage.AUTO;

    @Column(name = "duration_seconds")
    private Integer durationSeconds;

    @Column(name = "transcript_processing_time_ms")
    private Long transcriptProcessingTimeMs;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Builder.Default
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "voice_note_tag",
            joinColumns = @JoinColumn(name = "voice_note_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id"),
            indexes = {
                    @Index(name = "idx_voicenotetag_voicenote", columnList = "voice_note_id"),
                    @Index(name = "idx_voicenotetag_tag", columnList = "tag_id")
            }
    )
    private Set<TagEntity> tags = new HashSet<>();

}