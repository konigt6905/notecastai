package com.notecastai.gamenote.service;

import com.notecastai.gamenote.api.dto.GameNoteStatisticsDTO;
import com.notecastai.gamenote.api.dto.GameNoteStatisticsSummaryDTO;
import com.notecastai.gamenote.service.command.SubmitStatisticsCommand;

import java.util.List;

public interface GameNoteStatisticsService {

    /**
     * Submit statistics for a game note session.
     * Creates a new statistics entry with auto-incremented attempt number.
     */
    GameNoteStatisticsDTO submitStatistics(Long gameNoteId, SubmitStatisticsCommand command);

    /**
     * Get all attempts for a specific game note for the current user.
     * Returns attempts ordered by attempt number descending (latest first).
     */
    List<GameNoteStatisticsDTO> getAllAttempts(Long gameNoteId);

    GameNoteStatisticsDTO getLatestAttempt(Long gameNoteId);

    GameNoteStatisticsDTO getBestAttempt(Long gameNoteId);

    /** One summary per game note the current user has attempted. */
    List<GameNoteStatisticsSummaryDTO> getUserSummaries();

    GameNoteStatisticsSummaryDTO getGameNoteSummary(Long gameNoteId);
}
