package com.notecastai.gamenote.service.impl;

import com.notecastai.common.exeption.BusinessException;
import com.notecastai.common.util.SecurityUtils;
import com.notecastai.gamenote.api.dto.GameNoteStatisticsDTO;
import com.notecastai.gamenote.api.dto.GameNoteStatisticsSummaryDTO;
import com.notecastai.gamenote.api.dto.SubmitStatisticsRequest;
import com.notecastai.gamenote.api.mapper.GameNoteStatisticsMapper;
import com.notecastai.gamenote.domain.GameNoteEntity;
import com.notecastai.gamenote.domain.GameNoteStatisticsEntity;
import com.notecastai.gamenote.domain.QuestionType;
import com.notecastai.gamenote.infrastructure.repo.GameNoteRepository;
import com.notecastai.gamenote.infrastructure.repo.GameNoteStatisticsRepository;
import com.notecastai.gamenote.service.GameNoteStatisticsService;
import com.notecastai.user.domain.UserEntity;
import com.notecastai.user.infrastructure.repo.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

import static com.notecastai.common.exeption.BusinessException.BusinessCode.INVALID_REQUEST;

@Service
@RequiredArgsConstructor
@Slf4j
public class GameNoteStatisticsServiceImpl implements GameNoteStatisticsService {

    private final GameNoteStatisticsRepository statisticsRepository;
    private final GameNoteRepository gameNoteRepository;
    private final UserRepository userRepository;
    private final GameNoteStatisticsMapper mapper;

    // Score calculation weights and max points
    private static final double BASE_SCORE_WEIGHT = 0.70;  // 70% - applied to correctness percentage
    private static final double MAX_SPEED_BONUS = 20.0;     // 20 points max
    private static final double MAX_COMPLETION_BONUS = 10.0; // 10 points max

    // Time thresholds (in seconds per question)
    private static final double FAST_TIME_THRESHOLD = 5.0;    // Very fast
    private static final double NORMAL_TIME_THRESHOLD = 15.0; // Normal speed
    private static final double SLOW_TIME_THRESHOLD = 30.0;   // Slow

    @Override
    @Transactional
    public GameNoteStatisticsDTO submitStatistics(Long gameNoteId, SubmitStatisticsRequest request) {
        // Validate inputs
        if (request.getAnswers() == null || request.getAnswers().isEmpty()) {
            throw BusinessException.of(INVALID_REQUEST.append("Answers cannot be empty"));
        }

        // Get current user
        UserEntity user = userRepository.getByClerkUserId(SecurityUtils.getCurrentClerkUserIdOrThrow());

        // Get game note
        GameNoteEntity gameNote = gameNoteRepository.getOrThrow(gameNoteId);

        // Calculate next attempt number
        Integer maxAttemptNumber = statisticsRepository.getMaxAttemptNumber(gameNoteId, user.getId());
        Integer attemptNumber = (maxAttemptNumber != null ? maxAttemptNumber : 0) + 1;

        // Calculate core metrics
        int totalQuestions = gameNote.getNumberOfQuestions();
        int questionsAttempted = request.getAnswers().size();
        int questionsCorrect = (int) request.getAnswers().stream()
                .filter(SubmitStatisticsRequest.QuestionAnswerDTO::getIsCorrect)
                .count();

        BigDecimal correctnessPercentage = questionsAttempted > 0
                ? BigDecimal.valueOf((double) questionsCorrect / questionsAttempted * 100)
                .setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        // Calculate average time per question
        BigDecimal averageTimePerQuestion = questionsAttempted > 0
                ? BigDecimal.valueOf((double) request.getTotalTimeSeconds() / questionsAttempted)
                .setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        // Calculate final score
        ScoreCalculation scoreCalc = calculateScore(
                correctnessPercentage.doubleValue(),
                averageTimePerQuestion.doubleValue(),
                request.getCompleted()
        );

        // Build score breakdown
        Map<String, Object> scoreBreakdown = new LinkedHashMap<>();
        scoreBreakdown.put("baseScore", scoreCalc.baseScore);
        scoreBreakdown.put("speedBonus", scoreCalc.speedBonus);
        scoreBreakdown.put("completionBonus", scoreCalc.completionBonus);
        scoreBreakdown.put("totalScore", scoreCalc.finalScore);
        scoreBreakdown.put("formula", "Base (70%) + Speed (20%) + Completion (10%)");

        // Build type-specific stats
        Map<String, Object> typeSpecificStats = buildTypeSpecificStats(
                gameNote.getQuestionType(),
                request.getAnswers(),
                request.getTypeSpecificData()
        );

        // Create entity
        GameNoteStatisticsEntity entity = GameNoteStatisticsEntity.builder()
                .gameNote(gameNote)
                .user(user)
                .attemptNumber(attemptNumber)
                .completed(request.getCompleted())
                .completedAt(request.getCompleted() ? Instant.now() : null)
                .questionType(gameNote.getQuestionType())
                .totalQuestions(totalQuestions)
                .questionsAttempted(questionsAttempted)
                .questionsCorrect(questionsCorrect)
                .correctnessPercentage(correctnessPercentage)
                .totalTimeSeconds(request.getTotalTimeSeconds())
                .averageTimePerQuestion(averageTimePerQuestion)
                .finalScore(scoreCalc.finalScore)
                .scoreBreakdown(scoreBreakdown)
                .typeSpecificStats(typeSpecificStats)
                .build();

        GameNoteStatisticsEntity saved = statisticsRepository.save(entity);

        log.info("Statistics submitted: gameNoteId={}, userId={}, attempt={}, score={}, correctness={}%",
                gameNoteId, user.getId(), attemptNumber, scoreCalc.finalScore, correctnessPercentage);

        return mapper.toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<GameNoteStatisticsDTO> getAllAttempts(Long gameNoteId) {
        UserEntity user = userRepository.getByClerkUserId(SecurityUtils.getCurrentClerkUserIdOrThrow());
        List<GameNoteStatisticsEntity> attempts = statisticsRepository.findByGameNoteAndUser(gameNoteId, user.getId());
        return mapper.toDto(attempts);
    }

    @Override
    @Transactional(readOnly = true)
    public GameNoteStatisticsDTO getLatestAttempt(Long gameNoteId) {
        UserEntity user = userRepository.getByClerkUserId(SecurityUtils.getCurrentClerkUserIdOrThrow());
        return statisticsRepository.findLatestAttempt(gameNoteId, user.getId())
                .map(mapper::toDto)
                .orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public GameNoteStatisticsDTO getBestAttempt(Long gameNoteId) {
        UserEntity user = userRepository.getByClerkUserId(SecurityUtils.getCurrentClerkUserIdOrThrow());
        return statisticsRepository.findBestAttempt(gameNoteId, user.getId())
                .map(mapper::toDto)
                .orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<GameNoteStatisticsSummaryDTO> getUserSummary() {
        UserEntity user = userRepository.getByClerkUserId(SecurityUtils.getCurrentClerkUserIdOrThrow());
        List<GameNoteStatisticsEntity> allStats = statisticsRepository.findByUser(user.getId());

        // Group by game note
        Map<Long, List<GameNoteStatisticsEntity>> groupedByGameNote = allStats.stream()
                .collect(Collectors.groupingBy(stat -> stat.getGameNote().getId()));

        // Create summary for each game note
        return groupedByGameNote.entrySet().stream()
                .map(entry -> {
                    Long gameNoteId = entry.getKey();
                    List<GameNoteStatisticsEntity> attempts = entry.getValue();
                    GameNoteEntity gameNote = attempts.get(0).getGameNote();
                    return mapper.toSummaryDto(gameNoteId, gameNote.getTitle(), attempts);
                })
                .sorted((a, b) -> {
                    // Sort by latest attempt date descending
                    if (a.getLatestAttemptDate() == null) return 1;
                    if (b.getLatestAttemptDate() == null) return -1;
                    return b.getLatestAttemptDate().compareTo(a.getLatestAttemptDate());
                })
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public GameNoteStatisticsSummaryDTO getGameNoteSummary(Long gameNoteId) {
        UserEntity user = userRepository.getByClerkUserId(SecurityUtils.getCurrentClerkUserIdOrThrow());
        GameNoteEntity gameNote = gameNoteRepository.getOrThrow(gameNoteId);
        List<GameNoteStatisticsEntity> attempts = statisticsRepository.findByGameNoteAndUser(gameNoteId, user.getId());

        return mapper.toSummaryDto(gameNoteId, gameNote.getTitle(), attempts);
    }

    // ========== Private Helper Methods ==========

    /**
     * Calculate final score based on correctness, speed, and completion.
     * Formula: Base (70%) + Speed (up to 20 points) + Completion (10 points)
     */
    private ScoreCalculation calculateScore(double correctnessPercentage, double avgTimePerQuestion, boolean completed) {
        // Base score from correctness (70% weight)
        double baseScore = correctnessPercentage * BASE_SCORE_WEIGHT;

        // Speed bonus (up to 20 points)
        // Fast answers get bonus, slow answers get penalty
        double speedBonus;
        if (avgTimePerQuestion <= FAST_TIME_THRESHOLD) {
            speedBonus = MAX_SPEED_BONUS; // Full bonus
        } else if (avgTimePerQuestion <= NORMAL_TIME_THRESHOLD) {
            // Linear interpolation between FAST and NORMAL
            double ratio = (NORMAL_TIME_THRESHOLD - avgTimePerQuestion) / (NORMAL_TIME_THRESHOLD - FAST_TIME_THRESHOLD);
            speedBonus = MAX_SPEED_BONUS * ratio;
        } else if (avgTimePerQuestion <= SLOW_TIME_THRESHOLD) {
            // Linear interpolation between NORMAL and SLOW (goes negative)
            double ratio = (SLOW_TIME_THRESHOLD - avgTimePerQuestion) / (SLOW_TIME_THRESHOLD - NORMAL_TIME_THRESHOLD);
            speedBonus = 0.0 * ratio;
        } else {
            speedBonus = 0.0; // Too slow, no bonus
        }

        // Completion bonus (10 points if completed)
        double completionBonus = completed ? MAX_COMPLETION_BONUS : 0.0;

        // Total score (capped at 0-100)
        int finalScore = (int) Math.round(baseScore + speedBonus + completionBonus);
        finalScore = Math.max(0, Math.min(100, finalScore));

        return new ScoreCalculation(
                (int) Math.round(baseScore),
                (int) Math.round(speedBonus),
                (int) Math.round(completionBonus),
                finalScore
        );
    }

    /**
     * Build type-specific statistics based on question type.
     */
    private Map<String, Object> buildTypeSpecificStats(
            QuestionType questionType,
            List<SubmitStatisticsRequest.QuestionAnswerDTO> answers,
            Map<String, Object> additionalData
    ) {
        Map<String, Object> stats = new LinkedHashMap<>();

        // Store detailed question data
        List<Map<String, Object>> questionDetails = answers.stream()
                .map(this::mapQuestionAnswer)
                .toList();
        stats.put("questions", questionDetails);

        // Calculate type-specific metrics
        switch (questionType) {
            case FLASHCARD -> buildFlashcardStats(stats, answers);
            case MULTIPLE_CHOICE -> buildMultipleChoiceStats(stats, answers);
            case TRUE_FALSE -> buildTrueFalseStats(stats, answers);
            case OPEN_QUESTION -> buildOpenQuestionStats(stats, answers);
        }

        // Add any additional custom data
        if (additionalData != null && !additionalData.isEmpty()) {
            stats.put("customData", additionalData);
        }

        return stats;
    }

    private Map<String, Object> mapQuestionAnswer(SubmitStatisticsRequest.QuestionAnswerDTO answer) {
        Map<String, Object> questionData = new LinkedHashMap<>();
        questionData.put("questionId", answer.getQuestionId());
        questionData.put("selectedAnswer", answer.getSelectedAnswer());
        questionData.put("correctAnswer", answer.getCorrectAnswer());
        questionData.put("isCorrect", answer.getIsCorrect());
        questionData.put("timeSpent", answer.getTimeSpent());

        // Add type-specific fields if present
        if (answer.getEliminatedOptions() != null) {
            questionData.put("eliminatedOptions", answer.getEliminatedOptions());
        }
        if (answer.getChangeCount() != null) {
            questionData.put("changeCount", answer.getChangeCount());
        }
        if (answer.getConfidence() != null) {
            questionData.put("confidence", answer.getConfidence());
        }
        if (answer.getSelfScore() != null) {
            questionData.put("selfScore", answer.getSelfScore());
        }
        if (answer.getUserAnswer() != null) {
            questionData.put("userAnswer", answer.getUserAnswer());
        }
        if (answer.getRevisionCount() != null) {
            questionData.put("revisionCount", answer.getRevisionCount());
        }

        return questionData;
    }

    private void buildFlashcardStats(Map<String, Object> stats, List<SubmitStatisticsRequest.QuestionAnswerDTO> answers) {
        List<Double> recallTimes = answers.stream()
                .map(SubmitStatisticsRequest.QuestionAnswerDTO::getTimeSpent)
                .toList();

        stats.put("averageRecallTime", recallTimes.stream().mapToDouble(Double::doubleValue).average().orElse(0.0));
        stats.put("fastestRecall", recallTimes.stream().mapToDouble(Double::doubleValue).min().orElse(0.0));
        stats.put("slowestRecall", recallTimes.stream().mapToDouble(Double::doubleValue).max().orElse(0.0));
        stats.put("perfectRecallCount", answers.stream()
                .filter(a -> a.getIsCorrect() && a.getTimeSpent() < 5.0)
                .count());
    }

    private void buildMultipleChoiceStats(Map<String, Object> stats, List<SubmitStatisticsRequest.QuestionAnswerDTO> answers) {
        long firstAttemptCorrect = answers.stream()
                .filter(a -> a.getIsCorrect() && (a.getChangeCount() == null || a.getChangeCount() == 0))
                .count();
        double firstAttemptAccuracy = answers.isEmpty() ? 0.0 : (double) firstAttemptCorrect / answers.size() * 100;

        double avgEliminationCount = answers.stream()
                .filter(a -> a.getEliminatedOptions() != null)
                .mapToInt(a -> a.getEliminatedOptions().size())
                .average()
                .orElse(0.0);

        long answersWithChanges = answers.stream()
                .filter(a -> a.getChangeCount() != null && a.getChangeCount() > 0)
                .count();
        double answerChangeRate = answers.isEmpty() ? 0.0 : (double) answersWithChanges / answers.size();

        stats.put("firstAttemptAccuracy", Math.round(firstAttemptAccuracy * 100.0) / 100.0);
        stats.put("averageEliminationCount", Math.round(avgEliminationCount * 100.0) / 100.0);
        stats.put("answerChangeRate", Math.round(answerChangeRate * 100.0) / 100.0);
    }

    private void buildTrueFalseStats(Map<String, Object> stats, List<SubmitStatisticsRequest.QuestionAnswerDTO> answers) {
        List<SubmitStatisticsRequest.QuestionAnswerDTO> trueAnswers = answers.stream()
                .filter(a -> "true".equalsIgnoreCase(a.getCorrectAnswer()))
                .toList();
        List<SubmitStatisticsRequest.QuestionAnswerDTO> falseAnswers = answers.stream()
                .filter(a -> "false".equalsIgnoreCase(a.getCorrectAnswer()))
                .toList();

        double trueAccuracy = trueAnswers.isEmpty() ? 0.0 :
                (double) trueAnswers.stream().filter(SubmitStatisticsRequest.QuestionAnswerDTO::getIsCorrect).count() / trueAnswers.size() * 100;
        double falseAccuracy = falseAnswers.isEmpty() ? 0.0 :
                (double) falseAnswers.stream().filter(SubmitStatisticsRequest.QuestionAnswerDTO::getIsCorrect).count() / falseAnswers.size() * 100;

        double avgDecisionTime = answers.stream()
                .mapToDouble(SubmitStatisticsRequest.QuestionAnswerDTO::getTimeSpent)
                .average()
                .orElse(0.0);

        stats.put("trueAccuracy", Math.round(trueAccuracy * 100.0) / 100.0);
        stats.put("falseAccuracy", Math.round(falseAccuracy * 100.0) / 100.0);
        stats.put("averageDecisionTime", Math.round(avgDecisionTime * 100.0) / 100.0);
    }

    private void buildOpenQuestionStats(Map<String, Object> stats, List<SubmitStatisticsRequest.QuestionAnswerDTO> answers) {
        double avgSelfScore = answers.stream()
                .filter(a -> a.getSelfScore() != null)
                .mapToInt(SubmitStatisticsRequest.QuestionAnswerDTO::getSelfScore)
                .average()
                .orElse(0.0);

        double avgAnswerLength = answers.stream()
                .filter(a -> a.getUserAnswer() != null)
                .mapToInt(a -> a.getUserAnswer().length())
                .average()
                .orElse(0.0);

        int totalRevisions = answers.stream()
                .filter(a -> a.getRevisionCount() != null)
                .mapToInt(SubmitStatisticsRequest.QuestionAnswerDTO::getRevisionCount)
                .sum();

        double avgTimePerQuestion = answers.stream()
                .mapToDouble(SubmitStatisticsRequest.QuestionAnswerDTO::getTimeSpent)
                .average()
                .orElse(0.0);

        stats.put("averageSelfScore", Math.round(avgSelfScore * 100.0) / 100.0);
        stats.put("averageAnswerLength", (int) avgAnswerLength);
        stats.put("totalRevisions", totalRevisions);
        stats.put("averageTimePerQuestion", Math.round(avgTimePerQuestion * 100.0) / 100.0);
    }

    /**
     * Internal class to hold score calculation results.
     */
    private record ScoreCalculation(
            int baseScore,
            int speedBonus,
            int completionBonus,
            int finalScore
    ) {
    }
}
