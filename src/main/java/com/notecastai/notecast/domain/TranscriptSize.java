package com.notecastai.notecast.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Target length of a generated TTS transcript.
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

    private final String label;
    private final int minWords;
    private final int maxWords;
    private final int minSentences;
    private final int maxSentences;
    private final String approximateDuration;
    /** Passed to the prompt verbatim. */
    private final String promptGuidance;

    public int getAverageWords() {
        return (minWords + maxWords) / 2;
    }

    public int getAverageSentences() {
        return (minSentences + maxSentences) / 2;
    }
}