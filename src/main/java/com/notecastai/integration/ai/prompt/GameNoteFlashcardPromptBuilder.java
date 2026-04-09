package com.notecastai.integration.ai.prompt;

import com.notecastai.gamenote.domain.DifficultyLevel;
import lombok.*;

/**
 * Builds prompts for generating SHORT flashcard questions.
 * Questions max 15 words, answers max 5 words.
 */
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GameNoteFlashcardPromptBuilder {

    private String noteTitle;
    private String noteContent;
    private Integer numberOfQuestions;
    private DifficultyLevel difficulty;
    private String customInstructions;

    private static final String SYSTEM_PROMPT = """
            You are an expert educational content creator specializing in creating concise, effective flashcards.

            YOUR TASK:
            Generate %d FLASHCARD questions from the provided note content.

            CRITICAL REQUIREMENTS FOR LENGTH:
            - Questions MUST be 1 SHORT sentence (max 15 words)
            - Answers MUST be 1-5 words only (single term, short phrase, or brief definition)
            - Explanations MUST be 1-2 SHORT sentences (max 30 words)
            - NO long paragraphs, NO detailed explanations

            DIFFICULTY LEVEL: %s
            %s

            FLASHCARD BEST PRACTICES:
            1. ONE concept per card - keep it atomic
            2. Questions should test recall, not recognition
            3. Use clear, simple language
            4. Answers should be memorable and concise
            5. Think "term -> definition" or "question -> short answer" format

            EXAMPLE QUESTIONS (FOLLOW THIS EXACT FORMAT):

            {
              "questions": [
                {
                  "id": 1,
                  "type": "FLASHCARD",
                  "questionText": "What CSS property controls flex item direction?",
                  "answer": "flex-direction",
                  "explanation": "Sets how flex items flow in the container."
                },
                {
                  "id": 2,
                  "type": "FLASHCARD",
                  "questionText": "Which property aligns items on the main axis?",
                  "answer": "justify-content",
                  "explanation": "Common values: flex-start, center, space-between."
                },
                {
                  "id": 3,
                  "type": "FLASHCARD",
                  "questionText": "What is the default value of flex-wrap?",
                  "answer": "nowrap",
                  "explanation": "Items try to fit on one line by default."
                },
                {
                  "id": 4,
                  "type": "FLASHCARD",
                  "questionText": "In CSS Grid, what defines column sizes?",
                  "answer": "grid-template-columns",
                  "explanation": "Use values like '1fr 1fr' or 'repeat(3, 1fr)'."
                },
                {
                  "id": 5,
                  "type": "FLASHCARD",
                  "questionText": "What is flex shorthand for?",
                  "answer": "flex-grow, flex-shrink, flex-basis",
                  "explanation": "Example: 'flex: 1' sets grow=1, shrink=1, basis=0%%."
                }
              ]
            }

            OUTPUT FORMAT:
            Return ONLY valid JSON with this exact structure:
            {
              "questions": [
                {
                  "id": 1,
                  "type": "FLASHCARD",
                  "questionText": "SHORT question (max 15 words)?",
                  "answer": "1-5 word answer",
                  "explanation": "Brief 1-2 sentence explanation."
                }
              ]
            }

            STRICT RULES:
            - Generate EXACTLY %d questions
            - All questions MUST have type "FLASHCARD"
            - Question max: 15 words
            - Answer max: 5 words
            - Explanation max: 30 words
            - JSON must be valid and parseable
            - Number questions sequentially from 1
            """;

    private static final String USER_PROMPT = """
            Generate %d SHORT flashcard questions from this note:

            **Note Title**: %s

            **Note Content**:
            %s

            %s

            Remember:
            - Questions: MAX 15 words
            - Answers: MAX 5 words
            - Explanations: MAX 30 words
            - Difficulty: %s

            Return valid JSON only.
            """;

    public String getSystemPrompt() {
        return String.format(
                SYSTEM_PROMPT,
                numberOfQuestions,
                difficulty.getLabel(),
                difficulty.getAiGuidance(),
                numberOfQuestions
        );
    }

    public String getUserPrompt() {
        String customSection = (customInstructions != null && !customInstructions.isBlank())
                ? "**Custom Instructions**: " + customInstructions + "\n"
                : "";

        return String.format(
                USER_PROMPT,
                numberOfQuestions,
                noteTitle,
                noteContent,
                customSection,
                difficulty.getLabel()
        );
    }
}
