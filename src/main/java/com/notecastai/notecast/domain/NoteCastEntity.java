package com.notecastai.notecast.domain;

import com.notecastai.common.BaseEntity;
import com.notecastai.note.domain.NoteEntity;
import com.notecastai.voicenote.api.dto.TranscriptionLanguage;
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
        name = "note_cast",
        indexes = {
                @Index(name = "idx_notecast_note", columnList = "note_id"),
                @Index(name = "idx_notecast_status", columnList = "status"),
                @Index(name = "idx_notecast_created", columnList = "created_date")
        }
)
public class NoteCastEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "note_id", nullable = false)
    private NoteEntity note;

    @Column(name = "s3_file_url")
    private String s3FileUrl;

    @Lob
    @Basic(fetch = FetchType.LAZY)
    @Column(name = "transcript")
    private String transcript;

    @Column(name = "duration_seconds")
    private Integer durationSeconds;

    @Column(name = "audio_processing_time_seconds")
    private Long processingTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "language", length = 10)
    private TranscriptionLanguage language = TranscriptionLanguage.ENGLISH;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private NoteCastStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "style", nullable = false, length = 30)
    private NoteCastStyle style;

    @Column(name = "error_message", length = 1000)
    private String errorMessage;

}