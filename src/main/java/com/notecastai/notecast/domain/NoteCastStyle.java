package com.notecastai.notecast.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * NoteCastStyle controls how the AI should shape the TTS transcript text.
 * Each style provides a short label for UI and a detailed prompt for the AI.
 */
@Getter
@RequiredArgsConstructor
public enum NoteCastStyle {

    DEFAULT("Default", """
        Create a clear, natural-sounding narration suitable for text-to-speech.
        Use short to medium sentences. Avoid filler and hedging.
        Add light signposting between sections. Keep a steady, friendly tone.
        """),

    NEWS_BRIEF("News brief", """
        Produce a concise news-style bulletin.
        Lead with the most important facts, then supporting details.
        Keep sentences short, neutral, and objective. Avoid hype.
        Use transitions like: "In addition", "Meanwhile", "Finally".
        """),

    STORYTELLER("Storyteller", """
        Convert the content into a narrative with a beginning, middle, and end.
        Use vivid but simple language and smooth transitions.
        Include a hook at the start and a brief closing takeaway.
        Keep sentences TTS-friendly; avoid long multi-clause lines.
        """),

    DEEP_DIVE("Deep dive (detail-oriented)", """
        Produce a thorough, structured walkthrough of the topic.
        Organize into clearly labeled sections with brief summaries.
        Explain reasoning and context; define terms when first used.
        Keep sentences readable and avoid nested clauses.
        """),

    MAIN_IDEA("Main idea first", """
        Start with the core idea in one or two short sentences.
        Then give 3–5 crisp supporting points.
        End with a single-sentence takeaway.
        Keep wording direct and unambiguous for TTS.
        """),

    ACTION_FOCUSED("Action focused", """
        Turn the content into step-by-step guidance.
        Use imperative sentences and numbered steps.
        Include prerequisites, tips, and common pitfalls.
        Keep each step one or two short sentences.
        """),

    TEACHER_EXPLAINER("Teacher / explainer", """
        Teach the concept to a smart beginner.
        Use analogies and simple examples. Define jargon.
        Check understanding with rhetorical questions, then answer them.
        Keep pacing even; avoid rapid-fire lists.
        """),

    INTERVIEW_QA("Interview Q&A", """
        Recast as a host asking questions and a guest answering.
        Keep questions short and answers concise.
        Use natural spoken cues like "Great question" sparingly.
        Alternate Q and A cleanly; avoid overlaps.
        """),

    MOTIVATIONAL_COACH("Motivational coach", """
        Deliver an encouraging, forward-looking script.
        Emphasize benefits, momentum, and next steps.
        Use inclusive language ("you", "we"), but avoid clichés.
        Keep energy warm and sentences compact for TTS.
        """),

    MEDITATIVE_CALM("Calm / meditative", """
        Create a slow, soothing script with soft transitions.
        Use gentle, positive language and long pauses implied by short sentences.
        Avoid numbers and dense data. Favor imagery and reassurance.
        """),

    TECHNICAL_REPORT("Technical report", """
        Present the material in a precise, formal tone.
        Use exact terminology, define variables, and state assumptions.
        Sequence: objective, method, results, implications.
        Keep sentences medium length and unambiguous for TTS.
        """);

    private final String label;
    private final String promptText;

}
