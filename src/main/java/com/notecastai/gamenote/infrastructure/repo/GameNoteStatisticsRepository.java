package com.notecastai.gamenote.infrastructure.repo;

import com.notecastai.common.exception.BusinessException;
import com.notecastai.gamenote.domain.GameNoteStatisticsEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

import static com.notecastai.common.exception.BusinessException.BusinessCode.ENTITY_NOT_FOUND;

@Slf4j
@Repository
@RequiredArgsConstructor
public class GameNoteStatisticsRepository {

    private final GameNoteStatisticsDao dao;

    public GameNoteStatisticsEntity save(GameNoteStatisticsEntity entity) {
        return dao.save(entity);
    }

    public void delete(GameNoteStatisticsEntity entity) {
        dao.delete(entity);
    }

    public Optional<GameNoteStatisticsEntity> findById(Long id) {
        return dao.findById(id);
    }

    public GameNoteStatisticsEntity getOrThrow(Long id) {
        return dao.findById(id).orElseThrow(() ->
                BusinessException.of(ENTITY_NOT_FOUND.append(" GameNoteStatistics with id %d not found".formatted(id)))
        );
    }

    public List<GameNoteStatisticsEntity> findByGameNoteAndUser(Long gameNoteId, Long userId) {
        return dao.findByGameNoteIdAndUserIdOrderByAttemptNumberDesc(gameNoteId, userId);
    }

    public List<GameNoteStatisticsEntity> findByUser(Long userId) {
        return dao.findByUserIdOrderByCreatedDateDesc(userId);
    }

    public List<GameNoteStatisticsEntity> findByGameNoteIdsAndUser(List<Long> gameNoteIds, Long userId) {
        if (gameNoteIds == null || gameNoteIds.isEmpty()) {
            return List.of();
        }
        return dao.findByGameNoteIdInAndUserIdOrderByAttemptNumberDesc(gameNoteIds, userId);
    }

    public Optional<GameNoteStatisticsEntity> findBestAttempt(Long gameNoteId, Long userId) {
        return dao.findFirstByGameNoteIdAndUserIdOrderByFinalScoreDescCompletedAtDesc(gameNoteId, userId);
    }

    public Optional<GameNoteStatisticsEntity> findLatestAttempt(Long gameNoteId, Long userId) {
        return dao.findFirstByGameNoteIdAndUserIdOrderByCreatedDateDesc(gameNoteId, userId);
    }

    public Integer getMaxAttemptNumber(Long gameNoteId, Long userId) {
        return dao.findMaxAttemptNumber(gameNoteId, userId);
    }

    public Long countByGameNoteAndUser(Long gameNoteId, Long userId) {
        return dao.countByGameNoteIdAndUserId(gameNoteId, userId);
    }

    public Long countCompletedByUser(Long userId) {
        return dao.countByUserIdAndCompleted(userId, true);
    }
}
