package com.notecastai.analytics.service.impl;

import com.notecastai.analytics.api.dto.AnalyticsPeriod;
import com.notecastai.analytics.api.dto.AnalyticsStatsResponse;
import com.notecastai.analytics.service.AnalyticsService;
import com.notecastai.note.infrastructure.repo.NoteRepository;
import com.notecastai.notecast.infrastructure.repo.NoteCastRepository;
import com.notecastai.tag.infrastructure.repo.TagRepository;
import com.notecastai.user.service.UserService;
import com.notecastai.voicenote.infrastructure.repo.VoiceNoteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AnalyticsServiceImpl implements AnalyticsService {

    private final NoteRepository noteRepository;
    private final VoiceNoteRepository voiceNoteRepository;
    private final NoteCastRepository noteCastRepository;
    private final UserService userService;
    private final TagRepository tagRepository;

    @Override
    public AnalyticsStatsResponse getStats(AnalyticsPeriod period) {
        Long userId = userService.getCurrentUserId();

        Instant fromDate = calculateFromDate(period);
        Instant now = Instant.now();

        Long notesCreated = noteRepository.countByUserAndPeriod(userId, fromDate, now);
        Long voiceNotesProcessed = voiceNoteRepository.countProcessedByUserAndPeriod(userId, fromDate, now);
        Long notecastsGenerated = noteCastRepository.countByUserAndPeriod(userId, fromDate, now);

        AnalyticsStatsResponse.TrendData trends = calculateTrends(userId);

        List<AnalyticsStatsResponse.TopTagDTO> topTags = getTopTags(userId);

        return AnalyticsStatsResponse.builder()
                .notesCreated(notesCreated)
                .voiceNotesProcessed(voiceNotesProcessed)
                .notecastsGenerated(notecastsGenerated)
                .trends(trends)
                .topTags(topTags)
                .build();
    }

    private Instant calculateFromDate(AnalyticsPeriod period) {
        Instant now = Instant.now();
        return switch (period) {
            case WEEK -> now.minus(7, ChronoUnit.DAYS);
            case MONTH -> now.minus(30, ChronoUnit.DAYS);
            case YEAR -> now.minus(365, ChronoUnit.DAYS);
            case ALL -> Instant.EPOCH;
        };
    }

    private AnalyticsStatsResponse.TrendData calculateTrends(Long userId) {
        Instant now = Instant.now();
        Instant oneWeekAgo = now.minus(7, ChronoUnit.DAYS);
        Instant twoWeeksAgo = now.minus(14, ChronoUnit.DAYS);

        Long notesThisWeek = noteRepository.countByUserAndPeriod(userId, oneWeekAgo, now);
        Long notesLastWeek = noteRepository.countByUserAndPeriod(userId, twoWeeksAgo, oneWeekAgo);

        Double growth = 0.0;
        if (notesLastWeek > 0) {
            growth = ((notesThisWeek - notesLastWeek) / (double) notesLastWeek) * 100;
        } else if (notesThisWeek > 0) {
            growth = 100.0;
        }

        return AnalyticsStatsResponse.TrendData.builder()
                .notesThisWeek(notesThisWeek)
                .notesLastWeek(notesLastWeek)
                .growth(growth)
                .build();
    }

    private List<AnalyticsStatsResponse.TopTagDTO> getTopTags(Long userId) {
        var topTagProjections = tagRepository.findTopTagsByUserId(userId, 10);

        return topTagProjections.stream()
                .map(projection -> AnalyticsStatsResponse.TopTagDTO.builder()
                        .id(projection.getId())
                        .name(projection.getName())
                        .count(projection.getUsageCount())
                        .build())
                .collect(Collectors.toList());
    }
}