package com.notecastai.gamenote.service;

import com.notecastai.gamenote.api.dto.GameNoteStatisticsDTO;
import com.notecastai.gamenote.api.dto.GameNoteStatisticsSummaryDTO;
import com.notecastai.gamenote.api.dto.SubmitStatisticsRequest;

import java.util.List;

public interface GameNoteStatisticsService {

    /**
     * Submit statistics for a game note session.
     * Creates a new statistics entry with auto-incremented attempt number.
     */
    GameNoteStatisticsDTO submitStatistics(Long gameNoteId, SubmitStatisticsRequest request);

    /**
     * Get all attempts for a specific game note for the current user.
     * Returns attempts ordered by attempt number descending (latest first).
     */
    List<GameNoteStatisticsDTO> getAllAttempts(Long gameNoteId);

    /**
     * Get the latest attempt for a specific game note for the current user.
     */
    GameNoteStatisticsDTO getLatestAttempt(Long gameNoteId);

    /**
     * Get the best scoring attempt for a specific game note for the current user.
     */
    GameNoteStatisticsDTO getBestAttempt(Long gameNoteId);

    /**
     * Get summary statistics across all game notes for the current user.
     */
    List<GameNoteStatisticsSummaryDTO> getUserSummary();

    /**
     * Get summary statistics for a specific game note for the current user.
     */
    GameNoteStatisticsSummaryDTO getGameNoteSummary(Long gameNoteId);
}
