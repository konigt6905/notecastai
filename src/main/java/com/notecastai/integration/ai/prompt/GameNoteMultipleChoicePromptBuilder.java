package com.notecastai.integration.ai.prompt;

import com.notecastai.gamenote.domain.DifficultyLevel;
import lombok.*;

/**
 * Builds prompts for generating SHORT multiple choice questions.
 * Questions max 20 words, options max 8 words each.
 */
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GameNoteMultipleChoicePromptBuilder {

    private String noteTitle;
    private String noteContent;
    private Integer numberOfQuestions;
    private DifficultyLevel difficulty;
    private String customInstructions;

    private static final String SYSTEM_PROMPT = """
            You are an expert creating concise multiple-choice quiz questions.

            YOUR TASK:
            Generate %d MULTIPLE CHOICE questions from the provided content.

            CRITICAL LENGTH REQUIREMENTS:
            - Questions MUST be 1 SHORT sentence (max 20 words)
            - Each option MUST be SHORT (max 8 words)
            - Explanations MUST be 1-2 SHORT sentences (max 40 words)
            - Options should NOT have letter prefixes (A., B., etc.) - just the text

            DIFFICULTY LEVEL: %s
            %s

            MULTIPLE CHOICE RULES:
            1. EXACTLY 4 options per question
            2. ONE correct answer only
            3. THREE plausible distractors
            4. Options should be similar length
            5. Avoid "all of the above" or "none of the above"
            6. Mix position of correct answer (don't always put in same spot)

            EXAMPLE QUESTIONS (FOLLOW THIS EXACT FORMAT):

            {
              "questions": [
                {
                  "id": 1,
                  "type": "MULTIPLE_CHOICE",
                  "questionText": "Which hook handles side effects in React?",
                  "options": ["useState", "useEffect", "useReducer", "useContext"],
                  "correctAnswer": "useEffect",
                  "explanation": "useEffect handles side effects like data fetching and subscriptions."
                },
                {
                  "id": 2,
                  "type": "MULTIPLE_CHOICE",
                  "questionText": "What is the virtual DOM?",
                  "options": [
                    "A copy of the real DOM",
                    "A JavaScript representation of the DOM",
                    "A browser debugging plugin",
                    "A React database"
                  ],
                  "correctAnswer": "A JavaScript representation of the DOM",
                  "explanation": "The virtual DOM is a lightweight JS object that React uses for efficient updates."
                },
                {
                  "id": 3,
                  "type": "MULTIPLE_CHOICE",
                  "questionText": "How do you pass data to child components?",
                  "options": ["State", "Props", "Context", "Redux"],
                  "correctAnswer": "Props",
                  "explanation": "Props are the standard way to pass data from parent to child."
                },
                {
                  "id": 4,
                  "type": "MULTIPLE_CHOICE",
                  "questionText": "Which creates a functional component?",
                  "options": [
                    "function MyComp() { return <div /> }",
                    "const MyComp = new Component()",
                    "class MyComp extends React {}",
                    "React.createComponent(MyComp)"
                  ],
                  "correctAnswer": "function MyComp() { return <div /> }",
                  "explanation": "Functional components are JS functions that return JSX."
                }
              ]
            }

            OUTPUT FORMAT:
            Return ONLY valid JSON:
            {
              "questions": [
                {
                  "id": 1,
                  "type": "MULTIPLE_CHOICE",
                  "questionText": "Short question (max 20 words)?",
                  "options": ["Option 1", "Option 2", "Option 3", "Option 4"],
                  "correctAnswer": "The correct option text (must match exactly)",
                  "explanation": "Brief explanation (max 40 words)."
                }
              ]
            }

            STRICT RULES:
            - Generate EXACTLY %d questions
            - All have type "MULTIPLE_CHOICE"
            - Question max: 20 words
            - Each option max: 8 words
            - Explanation max: 40 words
            - correctAnswer MUST exactly match one option
            - Options array MUST have exactly 4 items
            - NO letter prefixes on options
            """;

    private static final String USER_PROMPT = """
            Generate %d SHORT multiple choice questions from this note:

            **Note Title**: %s

            **Note Content**:
            %s

            %s

            Requirements:
            - Questions: MAX 20 words
            - Options: MAX 8 words each, exactly 4 options
            - Explanations: MAX 40 words
            - Difficulty: %s
            - correctAnswer must exactly match one option

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
