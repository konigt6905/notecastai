package com.notecastai.gamenote.domain;

import com.notecastai.common.BaseEntity;
import com.notecastai.user.domain.UserEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Entity
@Table(
        name = "game_note_statistics",
        indexes = {
                @Index(name = "idx_game_stats_user", columnList = "user_id"),
                @Index(name = "idx_game_stats_game_note", columnList = "game_note_id"),
                @Index(name = "idx_game_stats_user_game", columnList = "user_id, game_note_id"),
                @Index(name = "idx_game_stats_completed_at", columnList = "completed_at"),
                @Index(name = "idx_game_stats_score", columnList = "final_score")
        }
)
public class GameNoteStatisticsEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "game_note_id", nullable = false)
    private GameNoteEntity gameNote;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    // Attempt tracking
    @Column(name = "attempt_number", nullable = false)
    private Integer attemptNumber;

    @Column(name = "completed", nullable = false)
    private Boolean completed;

    @Column(name = "completed_at")
    private Instant completedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "question_type", nullable = false, length = 30)
    private QuestionType questionType;

    // Core metrics
    @Column(name = "total_questions", nullable = false)
    private Integer totalQuestions;

    @Column(name = "questions_attempted", nullable = false)
    private Integer questionsAttempted;

    @Column(name = "questions_correct", nullable = false)
    private Integer questionsCorrect;

    @Column(name = "correctness_percentage", nullable = false, precision = 5, scale = 2)
    private BigDecimal correctnessPercentage;

    // Time tracking
    @Column(name = "total_time_seconds", nullable = false)
    private Integer totalTimeSeconds;

    @Column(name = "average_time_per_question", precision = 8, scale = 2)
    private BigDecimal averageTimePerQuestion;

    // Scoring
    @Column(name = "final_score", nullable = false)
    private Integer finalScore;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "score_breakdown", columnDefinition = "jsonb")
    @Builder.Default
    private Map<String, Object> scoreBreakdown = new HashMap<>();

    // Type-specific statistics
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "type_specific_stats", columnDefinition = "jsonb")
    @Builder.Default
    private Map<String, Object> typeSpecificStats = new HashMap<>();
}
