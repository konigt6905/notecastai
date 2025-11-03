package com.notecastai.analytics.service.impl;

import com.notecastai.analytics.api.dto.AnalyticsPeriod;
import com.notecastai.analytics.api.dto.AnalyticsStatsResponse;
import com.notecastai.analytics.service.AnalyticsService;
import com.notecastai.note.infrastructure.repo.NoteRepository;
import com.notecastai.notecast.infrastructure.repo.NoteCastRepository;
import com.notecastai.tag.repo.TagRepository;
import com.notecastai.user.infrastructure.repo.UserRepository;
import com.notecastai.voicenote.repo.VoiceNoteRepository;
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
    private final UserRepository userRepository;
    private final TagRepository tagRepository;

    @Override
    public AnalyticsStatsResponse getStats(Long userId, AnalyticsPeriod period) {
        // Validate user exists
        var user = userRepository.getOrThrow(userId);

        // Calculate date range based on period
        Instant fromDate = calculateFromDate(period);
        Instant now = Instant.now();

        // Get counts for the period
        Long notesCreated = noteRepository.countByUserAndPeriod(user, fromDate, now);
        Long voiceNotesProcessed = voiceNoteRepository.countProcessedByUserAndPeriod(user, fromDate, now);
        Long notecastsGenerated = noteCastRepository.countByUserAndPeriod(user, fromDate, now);

        // Calculate trends (week over week)
        AnalyticsStatsResponse.TrendData trends = calculateTrends(user);

        // Get top tags
        List<AnalyticsStatsResponse.TopTagDTO> topTags = getTopTags(user);

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

    private AnalyticsStatsResponse.TrendData calculateTrends(com.notecastai.user.domain.UserEntity user) {
        Instant now = Instant.now();
        Instant oneWeekAgo = now.minus(7, ChronoUnit.DAYS);
        Instant twoWeeksAgo = now.minus(14, ChronoUnit.DAYS);

        Long notesThisWeek = noteRepository.countByUserAndPeriod(user, oneWeekAgo, now);
        Long notesLastWeek = noteRepository.countByUserAndPeriod(user, twoWeeksAgo, oneWeekAgo);

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

    private List<AnalyticsStatsResponse.TopTagDTO> getTopTags(com.notecastai.user.domain.UserEntity user) {
        var topTagProjections = tagRepository.findTopTagsByUserId(user.getId(), 10);

        return topTagProjections.stream()
                .map(projection -> AnalyticsStatsResponse.TopTagDTO.builder()
                        .id(projection.getId())
                        .name(projection.getName())
                        .count(projection.getUsageCount())
                        .build())
                .collect(Collectors.toList());
    }
}