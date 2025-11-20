package com.notecastai.gamenote.api.mapper;

import com.notecastai.gamenote.api.dto.GameNoteDTO;
import com.notecastai.gamenote.api.dto.GameNoteShortDTO;
import com.notecastai.gamenote.api.dto.GameQuestionDTO;
import com.notecastai.gamenote.domain.GameNoteEntity;
import com.notecastai.gamenote.domain.GameNoteStatisticsEntity;
import com.notecastai.gamenote.domain.GameQuestionEntity;
import com.notecastai.note.api.mapper.NoteMapper;
import com.notecastai.tag.api.mapper.TagMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.OptionalDouble;

@Slf4j
@Component
@RequiredArgsConstructor
public class GameNoteMapper {

    private final TagMapper tagMapper;
    private final NoteMapper noteMapper;

    public GameNoteDTO toDto(GameNoteEntity entity) {
        return toDto(entity, null);
    }

    public GameNoteDTO toDto(GameNoteEntity entity, List<GameNoteStatisticsEntity> statistics) {
        if (entity == null) return null;

        return GameNoteDTO.builder()
                .id(entity.getId())
                .userId(entity.getUser().getId())
                .sourceNote(entity.getSourceNote() != null ? noteMapper.toDto(entity.getSourceNote()) : null)
                .title(entity.getTitle())
                .status(entity.getStatus())
                .numberOfQuestions(entity.getNumberOfQuestions())
                .questionLength(entity.getQuestionLength())
                .answerLength(entity.getAnswerLength())
                .difficulty(entity.getDifficulty())
                .questionType(entity.getQuestionType())
                .customInstructions(entity.getCustomInstructions())
                .errorMessage(entity.getErrorMessage())
                .questions(toQuestionDto(entity.getQuestions()))
                .tags(tagMapper.toDto(entity.getTags().stream().toList()))
                .createdDate(entity.getCreatedDate())
                .updatedDate(entity.getUpdatedDate())
                .statistics(buildStatisticsSummary(statistics))
                .build();
    }

    public GameNoteShortDTO toShortDto(GameNoteEntity entity) {
        return toShortDto(entity, null);
    }

    public GameNoteShortDTO toShortDto(GameNoteEntity entity, List<GameNoteStatisticsEntity> statistics) {
        if (entity == null) return null;

        GameNoteShortDTO.GameNoteShortDTOBuilder builder = GameNoteShortDTO.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .status(entity.getStatus())
                .numberOfQuestions(entity.getNumberOfQuestions())
                .questionType(entity.getQuestionType())
                .difficulty(entity.getDifficulty())
                .sourceNoteTitle(entity.getSourceNote() != null ? entity.getSourceNote().getTitle() : null)
                .tags(tagMapper.toDto(entity.getTags().stream().toList()))
                .createdDate(entity.getCreatedDate());

        // Add statistics if available
        if (statistics != null && !statistics.isEmpty()) {
            builder.totalAttempts(statistics.size());

            // Get latest attempt
            GameNoteStatisticsEntity latest = statistics.get(0); // Already ordered by attempt number desc
            builder.lastScore(latest.getFinalScore());

            // Get best score
            Integer bestScore = statistics.stream()
                    .mapToInt(GameNoteStatisticsEntity::getFinalScore)
                    .max()
                    .orElse(0);
            builder.bestScore(bestScore);

            // Calculate mastery level (based on last 3 completed attempts)
            List<GameNoteStatisticsEntity> completed = statistics.stream()
                    .filter(GameNoteStatisticsEntity::getCompleted)
                    .limit(3)
                    .toList();

            if (!completed.isEmpty()) {
                double avgRecentScore = completed.stream()
                        .mapToInt(GameNoteStatisticsEntity::getFinalScore)
                        .average()
                        .orElse(0);

                String masteryLevel;
                if (avgRecentScore >= 90) {
                    masteryLevel = "MASTERED";
                } else if (avgRecentScore >= 70) {
                    masteryLevel = "PRACTICING";
                } else {
                    masteryLevel = "LEARNING";
                }
                builder.masteryLevel(masteryLevel);
            }
        }

        return builder.build();
    }

    public List<GameNoteDTO> toDto(List<GameNoteEntity> entities) {
        if (entities == null) return Collections.emptyList();
        return entities.stream().map(this::toDto).toList();
    }

    public Page<GameNoteShortDTO> toShortDto(Page<GameNoteEntity> page) {
        return page.map(this::toShortDto);
    }

    private List<GameQuestionDTO> toQuestionDto(List<GameQuestionEntity> entities) {
        if (entities == null || entities.isEmpty()) {
            return Collections.emptyList();
        }
        return entities.stream()
                .map(this::toQuestionDto)
                .toList();
    }

    private GameQuestionDTO toQuestionDto(GameQuestionEntity entity) {
        if (entity == null) return null;

        return GameQuestionDTO.builder()
                .id(entity.getId().intValue())
                .type(entity.getType())
                .questionText(entity.getQuestionText())
                .options(entity.getOptions())
                .correctAnswer(entity.getCorrectAnswer())
                .answer(entity.getAnswer())
                .explanation(entity.getExplanation())
                .hint(entity.getHint())
                .build();
    }

    private GameNoteDTO.GameNoteStatsSummary buildStatisticsSummary(List<GameNoteStatisticsEntity> statistics) {
        if (statistics == null || statistics.isEmpty()) {
            return null;
        }

        // Filter completed attempts
        List<GameNoteStatisticsEntity> completed = statistics.stream()
                .filter(GameNoteStatisticsEntity::getCompleted)
                .toList();

        // Get latest attempt (first in list, already ordered by attempt number desc)
        GameNoteStatisticsEntity latest = statistics.get(0);

        // Find best score
        GameNoteStatisticsEntity best = statistics.stream()
                .max((a, b) -> {
                    int scoreComparison = Integer.compare(a.getFinalScore(), b.getFinalScore());
                    if (scoreComparison != 0) return scoreComparison;
                    return a.getCompletedAt() != null && b.getCompletedAt() != null
                            ? a.getCompletedAt().compareTo(b.getCompletedAt())
                            : 0;
                })
                .orElse(null);

        // Calculate averages from completed attempts
        OptionalDouble avgScore = completed.stream()
                .mapToInt(GameNoteStatisticsEntity::getFinalScore)
                .average();

        OptionalDouble avgTime = completed.stream()
                .mapToInt(GameNoteStatisticsEntity::getTotalTimeSeconds)
                .average();

        // Calculate mastery level (based on last 3 completed attempts)
        String masteryLevel = null;
        List<GameNoteStatisticsEntity> recentCompleted = completed.stream()
                .limit(3)
                .toList();

        if (!recentCompleted.isEmpty()) {
            double avgRecentScore = recentCompleted.stream()
                    .mapToInt(GameNoteStatisticsEntity::getFinalScore)
                    .average()
                    .orElse(0);

            if (avgRecentScore >= 90) {
                masteryLevel = "MASTERED";
            } else if (avgRecentScore >= 70) {
                masteryLevel = "PRACTICING";
            } else {
                masteryLevel = "LEARNING";
            }
        }

        return GameNoteDTO.GameNoteStatsSummary.builder()
                .totalAttempts(statistics.size())
                .completedAttempts(completed.size())
                .masteryLevel(masteryLevel)
                .lastScore(latest.getFinalScore())
                .lastAttemptDate(latest.getCreatedDate())
                .lastAttemptCompleted(latest.getCompleted())
                .bestScore(best != null ? best.getFinalScore() : null)
                .bestScoreDate(best != null ? best.getCompletedAt() : null)
                .averageScore(avgScore.isPresent() ? (int) avgScore.getAsDouble() : null)
                .averageTimeSeconds(avgTime.isPresent() ? (int) avgTime.getAsDouble() : null)
                .build();
    }
}
