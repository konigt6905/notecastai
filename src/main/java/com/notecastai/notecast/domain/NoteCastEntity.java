package com.notecastai.notecast.domain;

import com.notecastai.common.BaseEntity;
import com.notecastai.note.domain.NoteEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @SuperBuilder
@Entity
@Table(
        name = "note_cast",
        indexes = {
                @Index(name = "idx_notecast_note", columnList = "note_id"),
                @Index(name = "idx_notecast_status", columnList = "status")
        }
)
public class NoteCastEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // source note
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "note_id", nullable = false)
    private NoteEntity note;

    @Column(name = "s3_file_url")
    private String s3FileUrl;

    @Lob
    @Column(name = "transcript")
    private String transcript;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NoteCastStatus status;

}