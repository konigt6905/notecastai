package com.notecastai.gamenote.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Difficulty levels for question generation.
 * Based on Bloom's Taxonomy cognitive levels.
 */
@Getter
@RequiredArgsConstructor
public enum DifficultyLevel {
    EASY("Easy", "Basic recall and understanding", "Focus on remembering facts and basic comprehension."),
    MEDIUM("Medium", "Application and analysis", "Focus on applying concepts and analyzing relationships."),
    HARD("Hard", "Synthesis and evaluation", "Focus on creating new ideas and evaluating information critically.");

    private final String label;
    private final String description;
    private final String aiGuidance;
}
