package com.notecastai.gamenote.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Defines the expected length of generated answers.
 */
@Getter
@RequiredArgsConstructor
public enum AnswerLength {
    SMALL("Small", "Brief, concise answer", 100),
    MEDIUM("Medium", "Moderate detail", 250),
    LONG("Long", "Detailed explanation", 500);

    private final String label;
    private final String description;
    private final int maxCharacters;
}
