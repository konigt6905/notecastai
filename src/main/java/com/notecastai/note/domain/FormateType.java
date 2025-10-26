package com.notecastai.note.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum FormateType {

    DEFAULT("Default", """
        Try to formate text in appropriate way, consider text type and content.
        """),

    BULLET_SUMMARY("Bullet summary", """
        Rewrite the note as a concise bullet-point summary.
        Keep sentences short. Preserve key facts and numbers.
        """),

    MEETING_MINUTES("Meeting minutes", """
        Format as meeting minutes:
        - Attendees
        - Agenda
        - Decisions
        - Action Items (owner, due date)
        - Risks
        - Next meeting
        """),

    STUDY_NOTES("Study notes", """
        Convert to study notes:
        - Key concepts
        - Definitions
        - Examples
        - Quick Q&A flashcards
        Highlight formulas and steps.
        """),

    ACTION_ITEMS("Action items", """
        Extract only actionable tasks with clear owners (if present),
        deadlines (infer if obvious), and brief context.
        Output as a checklist.
        """),

    BLOG_OUTLINE("Blog outline", """
        Create a blog post outline:
        - Title ideas
        - H2/H3 sections
        - Bullet points per section
        End with a clear call-to-action.
        """);



    /** Human-friendly name to show in the UI. */
    private final String label;

    /** Prompt text for the AI to apply this formatting. */
    private final String promptText;
}