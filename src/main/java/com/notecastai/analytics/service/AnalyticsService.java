package com.notecastai.analytics.service;

import com.notecastai.analytics.api.dto.AnalyticsPeriod;
import com.notecastai.analytics.api.dto.AnalyticsStatsResponse;

public interface AnalyticsService {
    AnalyticsStatsResponse getStats(Long userId, AnalyticsPeriod period);
}