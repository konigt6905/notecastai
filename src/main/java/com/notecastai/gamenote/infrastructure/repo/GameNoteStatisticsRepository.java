package com.notecastai.gamenote.infrastructure.repo;

import com.notecastai.common.BaseRepository;
import com.notecastai.common.exeption.BusinessException;
import com.notecastai.gamenote.domain.GameNoteStatisticsEntity;
import com.notecastai.gamenote.repo.GameNoteStatisticsDao;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

import static com.notecastai.common.exeption.BusinessException.BusinessCode.ENTITY_NOT_FOUND;

@Slf4j
@Repository
public class GameNoteStatisticsRepository extends BaseRepository<GameNoteStatisticsEntity, Long, GameNoteStatisticsDao> {

    protected GameNoteStatisticsRepository(GameNoteStatisticsDao gameNoteStatisticsDao) {
        super(gameNoteStatisticsDao);
    }

    public GameNoteStatisticsEntity getOrThrow(Long id) {
        return findById(id).orElseThrow(() ->
                BusinessException.of(ENTITY_NOT_FOUND.append(" GameNoteStatistics with id %d not found".formatted(id)))
        );
    }

    public List<GameNoteStatisticsEntity> findByGameNoteAndUser(Long gameNoteId, Long userId) {
        return dao.findByGameNoteIdAndUserIdOrderByAttemptNumberDesc(gameNoteId, userId);
    }

    public List<GameNoteStatisticsEntity> findByUser(Long userId) {
        return dao.findByUserIdOrderByCreatedDateDesc(userId);
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
