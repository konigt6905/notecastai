package com.notecastai.gamenote.domain;

import com.notecastai.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Entity
@Table(
        name = "game_question",
        indexes = {
                @Index(name = "idx_game_question_game_note", columnList = "game_note_id")
        }
)
public class GameQuestionEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "game_note_id", nullable = false)
    private GameNoteEntity gameNote;

    @Column(name = "question_order", nullable = false)
    private Integer questionOrder;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 30)
    private QuestionType type;

    @Column(name = "question_text", nullable = false, columnDefinition = "TEXT")
    private String questionText;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "game_question_option",
            joinColumns = @JoinColumn(name = "game_question_id")
    )
    @Column(name = "option_text", columnDefinition = "TEXT")
    @OrderColumn(name = "option_order")
    @Builder.Default
    private List<String> options = new ArrayList<>();

    @Column(name = "correct_answer", columnDefinition = "TEXT")
    private String correctAnswer;

    @Column(name = "answer", columnDefinition = "TEXT")
    private String answer;

    @Column(name = "explanation", columnDefinition = "TEXT")
    private String explanation;

    @Column(name = "hint", columnDefinition = "TEXT")
    private String hint;
}
