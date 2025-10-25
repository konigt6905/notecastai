package com.notecastai.note.domain;

import com.notecastai.common.BaseEntity;
import com.notecastai.tag.domain.TagEntity;
import com.notecastai.user.domain.UserEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @SuperBuilder
@Entity
@Table(
        name = "note",
        indexes = {
                @Index(name = "idx_note_user", columnList = "user_id"),
                @Index(name = "idx_note_created", columnList = "created_date")
        }
)
public class NoteEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Column(nullable = false)
    private String title;

    @Lob
    @Column(name = "knowledge_base")
    private String knowledgeBase;

    /** Normalized tags (Many-to-Many via join table). */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "note_tag",
            joinColumns = @JoinColumn(name = "note_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id"),
            indexes = {
                    @Index(name = "idx_notetag_note", columnList = "note_id"),
                    @Index(name = "idx_notetag_tag",  columnList = "tag_id")
            }
    )
    private List<TagEntity> tags = new ArrayList<>();
}

