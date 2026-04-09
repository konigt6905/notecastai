package com.notecastai.integration.ai.prompt;

import com.notecastai.gamenote.domain.DifficultyLevel;
import lombok.*;

/**
 * Builds prompts for generating SHORT true/false questions.
 * Statements max 15 words.
 */
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GameNoteTrueFalsePromptBuilder {

    private String noteTitle;
    private String noteContent;
    private Integer numberOfQuestions;
    private DifficultyLevel difficulty;
    private String customInstructions;

    private static final String SYSTEM_PROMPT = """
            You are an expert creating concise true/false quiz questions.

            YOUR TASK:
            Generate %d TRUE/FALSE questions from the provided content.

            CRITICAL LENGTH REQUIREMENTS:
            - Statements MUST be 1 SHORT sentence (max 15 words)
            - Explanations MUST be 1-2 SHORT sentences (max 40 words)
            - Statements should be DECLARATIVE (not questions)

            DIFFICULTY LEVEL: %s
            %s

            TRUE/FALSE RULES:
            1. Statements must be ABSOLUTELY true or false (no partial truths)
            2. Avoid "always", "never", "all" unless testing that specific concept
            3. Test single concept per statement
            4. Be specific, avoid vague statements
            5. Balance true and false (aim for 50/50 split)
            6. correctAnswer must be "TRUE" or "FALSE" (uppercase)

            EXAMPLE QUESTIONS (FOLLOW THIS EXACT FORMAT):

            {
              "questions": [
                {
                  "id": 1,
                  "type": "TRUE_FALSE",
                  "questionText": "JavaScript is the same language as Java.",
                  "correctAnswer": "FALSE",
                  "explanation": "JavaScript and Java are completely different languages."
                },
                {
                  "id": 2,
                  "type": "TRUE_FALSE",
                  "questionText": "NULL returns 'object' with typeof in JavaScript.",
                  "correctAnswer": "TRUE",
                  "explanation": "This is a historical bug in JavaScript's implementation."
                },
                {
                  "id": 3,
                  "type": "TRUE_FALSE",
                  "questionText": "== checks both value and type equality.",
                  "correctAnswer": "FALSE",
                  "explanation": "Use === for strict equality. == does type coercion."
                },
                {
                  "id": 4,
                  "type": "TRUE_FALSE",
                  "questionText": "JavaScript arrays can hold mixed data types.",
                  "correctAnswer": "TRUE",
                  "explanation": "Arrays are dynamic and can contain different types."
                },
                {
                  "id": 5,
                  "type": "TRUE_FALSE",
                  "questionText": "NaN is a number type in JavaScript.",
                  "correctAnswer": "TRUE",
                  "explanation": "typeof NaN returns 'number'. It means Not-a-Number."
                }
              ]
            }

            OUTPUT FORMAT:
            Return ONLY valid JSON:
            {
              "questions": [
                {
                  "id": 1,
                  "type": "TRUE_FALSE",
                  "questionText": "Short declarative statement (max 15 words).",
                  "correctAnswer": "TRUE",
                  "explanation": "Brief explanation (max 40 words)."
                }
              ]
            }

            STRICT RULES:
            - Generate EXACTLY %d questions
            - All have type "TRUE_FALSE"
            - Statement max: 15 words
            - Explanation max: 40 words
            - correctAnswer MUST be "TRUE" or "FALSE" (uppercase)
            - Roughly 50%% true, 50%% false
            - Statements are declarative, not questions
            """;

    private static final String USER_PROMPT = """
            Generate %d SHORT true/false statements from this note:

            **Note Title**: %s

            **Note Content**:
            %s

            %s

            Requirements:
            - Statements: MAX 15 words (declarative, not questions)
            - Explanations: MAX 40 words
            - Difficulty: %s
            - correctAnswer: "TRUE" or "FALSE" (uppercase)
            - Balance: roughly 50%% true, 50%% false

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
