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
public class GameNoteFlashcardPromptBuilder {

    private String noteTitle;
    private String noteContent;
    private Integer numberOfQuestions;
    private QuestionLength questionLength;
    private AnswerLength answerLength;
    private DifficultyLevel difficulty;
    private String customInstructions;

    private static final String SYSTEM_PROMPT_TEMPLATE = """
            You are an expert educational content creator specializing in creating effective flashcard-based learning materials.

            YOUR TASK:
            Generate %d FLASHCARD questions from the provided note content. Flashcards are question-answer pairs used for spaced repetition learning.

            CONFIGURATION:
            - Question Length: %s (%s)
            - Answer Length: %s (%s)
            - Difficulty Level: %s
            - Difficulty Guidance: %s

            FLASHCARD BEST PRACTICES:
            1. **Questions should be clear and specific** - Avoid ambiguous wording
            2. **One concept per card** - Don't combine multiple ideas
            3. **Use active recall** - Frame questions to test memory, not just recognition
            4. **Progressive difficulty** - Start easier, build to harder concepts
            5. **Include context when needed** - Provide hints for complex topics
            6. **Answers should be concise but complete** - No unnecessary elaboration
            7. **Use the same language** - Keep language consistent with source material

            QUESTION FORMATTING:
            - %s questions should be: %s
            - %s answers should be: %s

            DIFFICULTY REQUIREMENTS (%s):
            %s

            OUTPUT FORMAT:
            Return a JSON object with this exact structure:
            {
              "questions": [
                {
                  "id": 1,
                  "type": "FLASHCARD",
                  "questionText": "Clear, specific question",
                  "answer": "Concise, accurate answer",
                  "explanation": "Why this answer is correct (optional context)",
                  "hint": "Optional hint to help recall (if useful)"
                }
              ]
            }

            IMPORTANT:
            - Generate EXACTLY %d questions
            - All questions MUST have type "FLASHCARD"
            - Each question MUST have: id, type, questionText, answer
            - Explanation and hint are optional but recommended
            - Ensure JSON is valid and parseable
            - Number questions sequentially starting from 1
            """;

    private static final String USER_PROMPT_TEMPLATE = """
            Generate %d flashcard questions from the following note:

            **Note Title**: %s

            **Note Content**:
            %s

            %s

            **Instructions**:
            1. Read the note carefully and identify the %d most important concepts
            2. Create clear, focused questions that test understanding
            3. Ensure questions progress from easier to harder (following %s difficulty)
            4. Keep questions length: %s (%s)
            5. Keep answers length: %s (%s)
            6. Add helpful explanations and hints where appropriate
            7. Return valid JSON matching the required schema

            **Example Output**:
            {
              "questions": [
                {
                  "id": 1,
                  "type": "FLASHCARD",
                  "questionText": "What is the primary function of mitochondria in eukaryotic cells?",
                  "answer": "Mitochondria produce ATP (adenosine triphosphate) through cellular respiration, providing energy for cellular processes.",
                  "explanation": "Mitochondria are often called the 'powerhouse of the cell' because they convert nutrients into usable energy through oxidative phosphorylation.",
                  "hint": "Think about what mitochondria are commonly called and their role in energy production."
                },
                {
                  "id": 2,
                  "type": "FLASHCARD",
                  "questionText": "Define photosynthesis in simple terms.",
                  "answer": "Photosynthesis is the process by which plants convert light energy into chemical energy stored in glucose, using carbon dioxide and water.",
                  "explanation": "This process occurs in chloroplasts and releases oxygen as a byproduct, which is essential for most life on Earth.",
                  "hint": "Consider how plants make their own food using sunlight."
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
                questionLength.getLabel(), questionLength.getDescription(),
                answerLength.getLabel(), answerLength.getDescription()
        );
    }
}
