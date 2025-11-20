package com.notecastai.integration.ai.prompt;

import com.notecastai.gamenote.domain.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GameNoteMultipleChoicePromptBuilder {

    private String noteTitle;
    private String noteContent;
    private Integer numberOfQuestions;
    private QuestionLength questionLength;
    private AnswerLength answerLength;
    private DifficultyLevel difficulty;
    private String customInstructions;

    private static final String SYSTEM_PROMPT_TEMPLATE = """
            You are an expert educational assessment specialist with 15+ years of experience creating high-quality multiple-choice questions.

            YOUR TASK:
            Generate %d MULTIPLE CHOICE questions from the provided note content.

            CONFIGURATION:
            - Question Length: %s (%s)
            - Answer Explanation Length: %s (%s)
            - Difficulty Level: %s
            - Difficulty Guidance: %s

            MULTIPLE CHOICE BEST PRACTICES:
            1. **Question Stem** - Clear, focused, asks one thing
            2. **Four Options** - Exactly 4 options (A, B, C, D)
            3. **One Correct Answer** - Only one option is unambiguously correct
            4. **Three Plausible Distractors** - Wrong options should seem reasonable
            5. **Avoid "All of the Above"** - Creates ambiguity
            6. **Avoid "None of the Above"** - Not pedagogically effective
            7. **Similar Option Length** - Don't make correct answer obviously longer
            8. **No Pattern in Answers** - Mix placement of correct answers (A, B, C, D)
            9. **Explanation Required** - Always explain why correct answer is right

            DISTRACTOR QUALITY:
            - Based on common misconceptions
            - Grammatically parallel with correct answer
            - Seem plausible to someone who doesn't fully understand
            - Should not be obviously wrong

            QUESTION FORMATTING:
            - %s questions should be: %s
            - %s explanations should be: %s

            DIFFICULTY REQUIREMENTS (%s):
            %s

            OUTPUT FORMAT:
            Return a JSON object with this exact structure:
            {
              "questions": [
                {
                  "id": 1,
                  "type": "MULTIPLE_CHOICE",
                  "questionText": "Clear, focused question?",
                  "options": [
                    "A. First option",
                    "B. Second option",
                    "C. Third option",
                    "D. Fourth option"
                  ],
                  "correctAnswer": "B. Second option",
                  "explanation": "Detailed explanation of why B is correct and why others are wrong",
                  "hint": "Optional hint to guide thinking (if helpful)"
                }
              ]
            }

            CRITICAL REQUIREMENTS:
            - Generate EXACTLY %d questions
            - All questions MUST have type "MULTIPLE_CHOICE"
            - Each question MUST have: id, type, questionText, options (4 items), correctAnswer, explanation
            - Options array MUST have EXACTLY 4 items
            - correctAnswer MUST match one of the options EXACTLY (including the letter prefix)
            - Distribute correct answers across A, B, C, D positions
            - Ensure JSON is valid and parseable
            - Number questions sequentially starting from 1
            """;

    private static final String USER_PROMPT_TEMPLATE = """
            Generate %d multiple choice questions from the following note:

            **Note Title**: %s

            **Note Content**:
            %s

            %s

            **Instructions**:
            1. Identify the %d most important concepts to test
            2. Create questions that test true understanding (not just memorization)
            3. Ensure difficulty aligns with: %s
            4. For each question:
               - Write a clear, focused question (length: %s)
               - Provide exactly 4 options with letters A, B, C, D
               - Make one option correct, three plausible distractors
               - Write explanation (length: %s) explaining the correct answer
               - Optionally add a hint
            5. Vary the position of correct answers (don't always use the same letter)
            6. Return valid JSON matching the required schema

            **Example Output**:
            {
              "questions": [
                {
                  "id": 1,
                  "type": "MULTIPLE_CHOICE",
                  "questionText": "What is the primary function of chlorophyll in plants?",
                  "options": [
                    "A. Store energy in glucose molecules",
                    "B. Absorb light energy for photosynthesis",
                    "C. Transport water through the plant",
                    "D. Release oxygen during respiration"
                  ],
                  "correctAnswer": "B. Absorb light energy for photosynthesis",
                  "explanation": "Chlorophyll is the green pigment in plants that absorbs light energy, primarily in the blue and red wavelengths. This light energy is essential for converting carbon dioxide and water into glucose during photosynthesis. Options A and D describe results of photosynthesis, not chlorophyll's function. Option C describes xylem function.",
                  "hint": "Think about what gives plants their green color and what happens when light hits the leaves."
                },
                {
                  "id": 2,
                  "type": "MULTIPLE_CHOICE",
                  "questionText": "Which organelle is responsible for cellular respiration?",
                  "options": [
                    "A. Chloroplast",
                    "B. Nucleus",
                    "C. Mitochondria",
                    "D. Ribosome"
                  ],
                  "correctAnswer": "C. Mitochondria",
                  "explanation": "Mitochondria are the organelles where cellular respiration occurs, converting glucose and oxygen into ATP, carbon dioxide, and water. Chloroplasts (A) are for photosynthesis, the nucleus (B) contains DNA, and ribosomes (D) synthesize proteins.",
                  "hint": "This organelle is often called the 'powerhouse of the cell'."
                }
              ]
            }
            """;

    public String getSystemPrompt() {
        return String.format(
                SYSTEM_PROMPT_TEMPLATE,
                numberOfQuestions,
                questionLength.getLabel(), questionLength.getDescription(),
                answerLength.getLabel(), answerLength.getDescription(),
                difficulty.getLabel(),
                difficulty.getAiGuidance(),
                questionLength.getLabel(), questionLength.getDescription(),
                answerLength.getLabel(), answerLength.getDescription(),
                difficulty.getLabel(),
                difficulty.getAiGuidance(),
                numberOfQuestions
        );
    }

    public String getUserPrompt() {
        String customSection = (customInstructions != null && !customInstructions.isBlank())
                ? "**IMPORTANT - Custom Instructions**: " + customInstructions + "\n"
                : "";

        return String.format(
                USER_PROMPT_TEMPLATE,
                numberOfQuestions,
                noteTitle,
                noteContent,
                customSection,
                numberOfQuestions,
                difficulty.getLabel(),
                questionLength.getLabel(),
                answerLength.getLabel()
        );
    }
}
