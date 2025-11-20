package com.notecastai.gamenote.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Types of quiz questions that can be generated.
 */
@Getter
@RequiredArgsConstructor
public enum QuestionType {
    FLASHCARD("Flashcard", "Question on front, answer on back"),
    MULTIPLE_CHOICE("Multiple Choice", "One correct answer with 3 distractors"),
    TRUE_FALSE("True/False", "Binary choice statement"),
    OPEN_QUESTION("Open Question", "Free-form answer required");

    private final String label;
    private final String description;

}
