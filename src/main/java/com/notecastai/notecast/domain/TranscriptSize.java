package com.notecastai.notecast.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Controls the length of generated TTS transcripts.
 * Each size specifies target word count, sentence count, and approximate duration.
 */
@Getter
@RequiredArgsConstructor
public enum TranscriptSize {

    EXTRA_SHORT(
            "Extra short",
            300,
            500,
            15,
            25,
            "2-3 minutes",
            """
            Keep it very brief and focused. Cover only the most essential points.
            Use 15-25 short, punchy sentences. Target around 300-500 words.
            This is a quick overview - get straight to the point and wrap up efficiently.
            """
    ),

    SHORT(
            "Short",
            500,
            800,
            25,
            40,
            "3-5 minutes",
            """
            Provide a concise but complete overview. Hit the key points with some context.
            Use 25-40 clear sentences. Target around 500-800 words.
            This is a focused summary - be thorough but don't elaborate excessively.
            """
    ),

    MEDIUM(
            "Medium",
            800,
            1200,
            40,
            60,
            "5-8 minutes",
            """
            Deliver a well-rounded explanation with good detail. Cover main points and supporting information.
            Use 40-60 well-structured sentences. Target around 800-1200 words.
            This is a standard deep-dive - balance breadth and depth effectively.
            """
    ),

    LARGE(
            "Large",
            1200,
            1800,
            60,
            90,
            "8-12 minutes",
            """
            Provide comprehensive coverage with detailed explanations and examples.
            Use 60-90 informative sentences. Target around 1200-1800 words.
            This is an in-depth exploration - give thorough treatment to all aspects.
            """
    ),

    EXTRA_LARGE(
            "Extra large",
            1800,
            2500,
            90,
            120,
            "12-17 minutes",
            """
            Deliver exhaustive, detailed content covering all angles with rich context and examples.
            Use 90-120 comprehensive sentences. Target around 1800-2500 words.
            This is a complete masterclass - leave no stone unturned in your explanation.
            """
    );

    /** Human-friendly label for UI */
    private final String label;

    /** Minimum target word count */
    private final int minWords;

    /** Maximum target word count */
    private final int maxWords;

    /** Minimum sentence count */
    private final int minSentences;

    /** Maximum sentence count */
    private final int maxSentences;

    /** Approximate speaking duration */
    private final String approximateDuration;

    /** Instructions for the AI on how to handle this size */
    private final String promptGuidance;

    /**
     * Get average word count for this size
     */
    public int getAverageWords() {
        return (minWords + maxWords) / 2;
    }

    /**
     * Get average sentence count for this size
     */
    public int getAverageSentences() {
        return (minSentences + maxSentences) / 2;
    }
}