package com.notecastai.gamenote.repo;

import com.notecastai.gamenote.domain.GameNoteStatisticsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface GameNoteStatisticsDao extends JpaRepository<GameNoteStatisticsEntity, Long> {

    List<GameNoteStatisticsEntity> findByGameNoteIdAndUserIdOrderByAttemptNumberDesc(Long gameNoteId, Long userId);

    List<GameNoteStatisticsEntity> findByUserIdOrderByCreatedDateDesc(Long userId);

    Optional<GameNoteStatisticsEntity> findFirstByGameNoteIdAndUserIdOrderByFinalScoreDescCompletedAtDesc(
            Long gameNoteId, Long userId
    );

    Optional<GameNoteStatisticsEntity> findFirstByGameNoteIdAndUserIdOrderByCreatedDateDesc(
            Long gameNoteId, Long userId
    );

    @Query("SELECT COALESCE(MAX(s.attemptNumber), 0) FROM GameNoteStatisticsEntity s " +
           "WHERE s.gameNote.id = :gameNoteId AND s.user.id = :userId")
    Integer findMaxAttemptNumber(@Param("gameNoteId") Long gameNoteId, @Param("userId") Long userId);

    Long countByGameNoteIdAndUserId(Long gameNoteId, Long userId);

    Long countByUserIdAndCompleted(Long userId, Boolean completed);
}
