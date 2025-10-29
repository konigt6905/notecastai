package com.notecastai.note.domain;

import com.notecastai.common.BaseEntity;
import com.notecastai.tag.domain.TagEntity;
import com.notecastai.user.domain.UserEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;


@Entity
@Table(
        name = "note",
        indexes = {
                @Index(name = "idx_note_user", columnList = "user_id"),
                @Index(name = "idx_note_created", columnList = "created_date")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class NoteEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Column(nullable = false)
    private String title;

    @Lob
    @Basic(fetch = FetchType.LAZY)
    @Column(name = "knowledge_base", columnDefinition = "TEXT")
    private String knowledgeBase;

    @Lob
    @Basic(fetch = FetchType.LAZY)
    @Column(name = "formatted_note", columnDefinition = "TEXT")
    private String formattedNote;

    @Enumerated(EnumType.STRING)
    @Column(name = "current_formate", nullable = false, length = 40)
    private FormateType currentFormate;

    /** Normalized tags (Many-to-Many via join table). */
    @Builder.Default
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

    @Builder.Default
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
            name = "note_ai_action",
            joinColumns = @JoinColumn(name = "note_id"),
            indexes = @Index(name = "idx_ai_action_note", columnList = "note_id")
    )
    @OrderColumn(name = "position") // list order
    private List<AiAction> proposedAiActions = new ArrayList<>();

    @Embeddable
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AiAction {

        @Column(name = "name", nullable = false, length = 200)
        private String name;

        @Lob
        @Column(name = "prompt", columnDefinition = "TEXT", nullable = false)
        private String prompt;
    }
}