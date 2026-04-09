package com.notecastai.gamenote.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Defines the expected length of generated questions.
 */
@Getter
@RequiredArgsConstructor
public enum QuestionLength {
    SMALL("Small", "Brief question (1-2 sentences)", 50),
    MEDIUM("Medium", "Moderate question (2-4 sentences)", 100),
    LONG("Long", "Detailed question (4-6 sentences)", 150);

    private final String label;
    private final String description;
    private final int maxCharacters;
}
