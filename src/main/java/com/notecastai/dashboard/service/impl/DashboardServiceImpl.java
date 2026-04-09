package com.notecastai.dashboard.service.impl;

import com.notecastai.dashboard.api.dto.DashboardStatisticsDTO;
import com.notecastai.dashboard.service.DashboardService;
import com.notecastai.note.infrastructure.repo.NoteRepository;
import com.notecastai.notecast.infrastructure.repo.NoteCastRepository;
import com.notecastai.user.service.UserService;
import com.notecastai.voicenote.infrastructure.repo.VoiceNoteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class DashboardServiceImpl implements DashboardService {

    private final NoteRepository noteRepository;
    private final VoiceNoteRepository voiceNoteRepository;
    private final NoteCastRepository noteCastRepository;
    private final UserService userService;

    @Override
    public DashboardStatisticsDTO getStatistics() {
        Long userId = userService.getCurrentUserId();

        // Calculate date ranges
        Instant now = Instant.now();
        Instant oneWeekAgo = now.minus(7, ChronoUnit.DAYS);
        Instant twoWeeksAgo = now.minus(14, ChronoUnit.DAYS);

        // Get counts for Notes
        Long notesCurrentWeek = noteRepository.countByUserAndPeriod(userId, oneWeekAgo, now);
        Long notesPreviousWeek = noteRepository.countByUserAndPeriod(userId, twoWeeksAgo, oneWeekAgo);
        BigDecimal notesTrend = calculateTrendPercentage(notesCurrentWeek, notesPreviousWeek);

        // Get counts for VoiceNotes (only processed ones)
        Long voiceNotesCurrentWeek = voiceNoteRepository.countProcessedByUserAndPeriod(userId, oneWeekAgo, now);
        Long voiceNotesPreviousWeek = voiceNoteRepository.countProcessedByUserAndPeriod(userId, twoWeeksAgo, oneWeekAgo);
        BigDecimal voiceNotesTrend = calculateTrendPercentage(voiceNotesCurrentWeek, voiceNotesPreviousWeek);

        // Get counts for NoteCasts
        Long notecastsCurrentWeek = noteCastRepository.countByUserAndPeriod(userId, oneWeekAgo, now);
        Long notecastsPreviousWeek = noteCastRepository.countByUserAndPeriod(userId, twoWeeksAgo, oneWeekAgo);
        BigDecimal notecastsTrend = calculateTrendPercentage(notecastsCurrentWeek, notecastsPreviousWeek);

        // Get total counts (not just current week)
        Long totalNotes = noteRepository.countByUserAndPeriod(userId, Instant.EPOCH, now);
        Long totalVoiceNotes = voiceNoteRepository.countProcessedByUserAndPeriod(userId, Instant.EPOCH, now);
        Long totalNotecasts = noteCastRepository.countByUserAndPeriod(userId, Instant.EPOCH, now);

        return DashboardStatisticsDTO.builder()
                .notesCount(totalNotes)
                .notesTrend(notesTrend)
                .voiceNotesCount(totalVoiceNotes)
                .voiceNotesTrend(voiceNotesTrend)
                .notecastsCount(totalNotecasts)
                .notecastsTrend(notecastsTrend)
                .build();
    }

    private BigDecimal calculateTrendPercentage(Long currentCount, Long previousCount) {
        if (previousCount == 0 && currentCount == 0) {
            return BigDecimal.ZERO;
        }

        if (previousCount == 0 && currentCount > 0) {
            return new BigDecimal("100.0");
        }

        if (previousCount > 0 && currentCount == 0) {
            return new BigDecimal("-100.0");
        }

        // Calculate percentage change: ((current - previous) / previous) * 100
        BigDecimal current = new BigDecimal(currentCount);
        BigDecimal previous = new BigDecimal(previousCount);
        BigDecimal change = current.subtract(previous);
        BigDecimal percentageChange = change.divide(previous, 4, RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100"))
                .setScale(1, RoundingMode.HALF_UP);

        return percentageChange;
    }
}