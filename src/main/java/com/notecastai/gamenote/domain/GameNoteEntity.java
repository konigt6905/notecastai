package com.notecastai.gamenote.domain;

import com.notecastai.common.BaseEntity;
import com.notecastai.note.domain.NoteEntity;
import com.notecastai.tag.domain.TagEntity;
import com.notecastai.user.domain.UserEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Entity
@Table(
        name = "game_note",
        indexes = {
                @Index(name = "idx_game_note_user", columnList = "user_id"),
                @Index(name = "idx_game_note_status", columnList = "status"),
                @Index(name = "idx_game_note_source_note", columnList = "source_note_id")
        }
)
public class GameNoteEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "source_note_id", nullable = false)
    private NoteEntity sourceNote;

    @Column(name = "title", nullable = false, length = 500)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private GameNoteStatus status;

    @Column(name = "number_of_questions", nullable = false)
    private Integer numberOfQuestions;

    @Enumerated(EnumType.STRING)
    @Column(name = "question_length", nullable = false, length = 20)
    private QuestionLength questionLength;

    @Enumerated(EnumType.STRING)
    @Column(name = "answer_length", nullable = false, length = 20)
    private AnswerLength answerLength;

    @Enumerated(EnumType.STRING)
    @Column(name = "difficulty", nullable = false, length = 20)
    private DifficultyLevel difficulty;

    @Enumerated(EnumType.STRING)
    @Column(name = "question_type", nullable = false, length = 30)
    private QuestionType questionType;

    @Basic(fetch = FetchType.LAZY)
    @Column(name = "custom_instructions", columnDefinition = "TEXT")
    private String customInstructions;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Builder.Default
    @OneToMany(mappedBy = "gameNote", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("questionOrder ASC")
    private List<GameQuestionEntity> questions = new ArrayList<>();

    @Builder.Default
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "game_note_tag",
            joinColumns = @JoinColumn(name = "game_note_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id"),
            indexes = {
                    @Index(name = "idx_gamenotetag_gamenote", columnList = "game_note_id"),
                    @Index(name = "idx_gamenotetag_tag", columnList = "tag_id")
            }
    )
    private Set<TagEntity> tags = new HashSet<>();

    public void addQuestion(GameQuestionEntity question) {
        questions.add(question);
        question.setGameNote(this);
    }

    public void removeQuestion(GameQuestionEntity question) {
        questions.remove(question);
        question.setGameNote(null);
    }

}
