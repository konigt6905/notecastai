package com.notecastai.gamenote.api.mapper;

import com.notecastai.gamenote.api.dto.GameNoteStatisticsDTO;
import com.notecastai.gamenote.api.dto.GameNoteStatisticsSummaryDTO;
import com.notecastai.gamenote.domain.GameNoteStatisticsEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.OptionalDouble;

@Slf4j
@Component
@RequiredArgsConstructor
public class GameNoteStatisticsMapper {

    public GameNoteStatisticsDTO toDto(GameNoteStatisticsEntity entity) {
        if (entity == null) return null;

        return GameNoteStatisticsDTO.builder()
                .id(entity.getId())
                .gameNoteId(entity.getGameNote().getId())
                .userId(entity.getUser().getId())
                .attemptNumber(entity.getAttemptNumber())
                .completed(entity.getCompleted())
                .completedAt(entity.getCompletedAt())
                .questionType(entity.getQuestionType())
                .totalQuestions(entity.getTotalQuestions())
                .questionsAttempted(entity.getQuestionsAttempted())
                .questionsCorrect(entity.getQuestionsCorrect())
                .correctnessPercentage(entity.getCorrectnessPercentage())
                .totalTimeSeconds(entity.getTotalTimeSeconds())
                .averageTimePerQuestion(entity.getAverageTimePerQuestion())
                .finalScore(entity.getFinalScore())
                .scoreBreakdown(entity.getScoreBreakdown())
                .typeSpecificStats(entity.getTypeSpecificStats())
                .createdDate(entity.getCreatedDate())
                .build();
    }

    public List<GameNoteStatisticsDTO> toDto(List<GameNoteStatisticsEntity> entities) {
        if (entities == null) return Collections.emptyList();
        return entities.stream().map(this::toDto).toList();
    }

    public GameNoteStatisticsSummaryDTO toSummaryDto(
            Long gameNoteId,
            String gameNoteTitle,
            List<GameNoteStatisticsEntity> allAttempts
    ) {
        if (allAttempts == null || allAttempts.isEmpty()) {
            return GameNoteStatisticsSummaryDTO.builder()
                    .gameNoteId(gameNoteId)
                    .gameNoteTitle(gameNoteTitle)
                    .totalAttempts(0)
                    .completedAttempts(0)
                    .build();
        }

        // Filter completed attempts
        List<GameNoteStatisticsEntity> completed = allAttempts.stream()
                .filter(GameNoteStatisticsEntity::getCompleted)
                .toList();

        // Find best score
        GameNoteStatisticsEntity bestAttempt = allAttempts.stream()
                .max((a, b) -> {
                    int scoreComparison = Integer.compare(a.getFinalScore(), b.getFinalScore());
                    if (scoreComparison != 0) return scoreComparison;
                    return a.getCompletedAt() != null && b.getCompletedAt() != null
                            ? a.getCompletedAt().compareTo(b.getCompletedAt())
                            : 0;
                })
                .orElse(null);

        // Find latest attempt
        GameNoteStatisticsEntity latestAttempt = allAttempts.stream()
                .max((a, b) -> a.getCreatedDate().compareTo(b.getCreatedDate()))
                .orElse(null);

        // Calculate averages
        OptionalDouble avgScore = completed.stream()
                .mapToInt(GameNoteStatisticsEntity::getFinalScore)
                .average();

        OptionalDouble avgCorrectness = completed.stream()
                .mapToDouble(e -> e.getCorrectnessPercentage().doubleValue())
                .average();

        OptionalDouble avgTime = completed.stream()
                .mapToInt(GameNoteStatisticsEntity::getTotalTimeSeconds)
                .average();

        // Calculate improvement rate
        BigDecimal improvementRate = null;
        if (completed.size() >= 2) {
            GameNoteStatisticsEntity first = completed.get(completed.size() - 1); // Oldest completed
            GameNoteStatisticsEntity last = completed.get(0); // Latest completed

            if (first.getFinalScore() > 0) {
                double improvement = ((double) (last.getFinalScore() - first.getFinalScore()) / first.getFinalScore()) * 100;
                improvementRate = BigDecimal.valueOf(improvement).setScale(2, RoundingMode.HALF_UP);
            }
        }

        // Determine mastery level
        String masteryLevel = determineMasteryLevel(completed);

        return GameNoteStatisticsSummaryDTO.builder()
                .gameNoteId(gameNoteId)
                .gameNoteTitle(gameNoteTitle)
                .totalAttempts(allAttempts.size())
                .completedAttempts(completed.size())
                .bestScore(bestAttempt != null ? bestAttempt.getFinalScore() : null)
                .bestScoreDate(bestAttempt != null ? bestAttempt.getCompletedAt() : null)
                .latestScore(latestAttempt != null ? latestAttempt.getFinalScore() : null)
                .latestAttemptDate(latestAttempt != null ? latestAttempt.getCreatedDate() : null)
                .averageScore(avgScore.isPresent()
                        ? BigDecimal.valueOf(avgScore.getAsDouble()).setScale(2, RoundingMode.HALF_UP)
                        : null)
                .averageCorrectness(avgCorrectness.isPresent()
                        ? BigDecimal.valueOf(avgCorrectness.getAsDouble()).setScale(2, RoundingMode.HALF_UP)
                        : null)
                .averageTimeSeconds(avgTime.isPresent()
                        ? (int) avgTime.getAsDouble()
                        : null)
                .masteryLevel(masteryLevel)
                .improvementRate(improvementRate)
                .build();
    }

    private String determineMasteryLevel(List<GameNoteStatisticsEntity> completedAttempts) {
        if (completedAttempts.isEmpty()) {
            return "NOT_STARTED";
        }

        // Get last 3 attempts or all if less than 3
        int recentCount = Math.min(3, completedAttempts.size());
        List<GameNoteStatisticsEntity> recent = completedAttempts.subList(0, recentCount);

        double avgRecentScore = recent.stream()
                .mapToInt(GameNoteStatisticsEntity::getFinalScore)
                .average()
                .orElse(0);

        if (avgRecentScore >= 90) {
            return "MASTERED";
        } else if (avgRecentScore >= 70) {
            return "PRACTICING";
        } else {
            return "LEARNING";
        }
    }
}
