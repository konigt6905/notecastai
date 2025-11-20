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
public class GameNoteTrueFalsePromptBuilder {

    private String noteTitle;
    private String noteContent;
    private Integer numberOfQuestions;
    private QuestionLength questionLength;
    private AnswerLength answerLength;
    private DifficultyLevel difficulty;
    private String customInstructions;

    private static final String SYSTEM_PROMPT_TEMPLATE = """
            You are an expert educational content creator specializing in creating effective true/false assessment questions.

            YOUR TASK:
            Generate %d TRUE/FALSE questions from the provided note content.

            CONFIGURATION:
            - Statement Length: %s (%s)
            - Explanation Length: %s (%s)
            - Difficulty Level: %s
            - Difficulty Guidance: %s

            TRUE/FALSE BEST PRACTICES:
            1. **Statements must be ABSOLUTELY true or false** - No partial truths
            2. **Avoid qualifiers** - Words like "always", "never", "all" often make statements false
            3. **Test single concept** - Don't combine multiple ideas in one statement
            4. **Be specific** - Vague statements are ambiguous
            5. **No double negatives** - They confuse rather than test knowledge
            6. **Balance true and false** - Roughly 50/50 split
            7. **Explanation is critical** - Must clarify why statement is true or false

            COMMON PITFALLS TO AVOID:
            - "All X are Y" - Usually false (too absolute)
            - "X never happens" - Usually false (too absolute)
            - "X is sometimes Y" - Usually true (too vague)
            - Partially true statements - Must be completely true or completely false

            STATEMENT FORMATTING:
            - %s statements should be: %s
            - %s explanations should be: %s

            DIFFICULTY REQUIREMENTS (%s):
            %s

            OUTPUT FORMAT:
            Return a JSON object with this exact structure:
            {
              "questions": [
                {
                  "id": 1,
                  "type": "TRUE_FALSE",
                  "questionText": "Clear, unambiguous statement",
                  "correctAnswer": "true",
                  "explanation": "Detailed explanation of why this is true/false",
                  "hint": "Optional hint to help reasoning (if useful)"
                }
              ]
            }

            CRITICAL REQUIREMENTS:
            - Generate EXACTLY %d questions
            - All questions MUST have type "TRUE_FALSE"
            - Each question MUST have: id, type, questionText, correctAnswer, explanation
            - questionText should be a declarative statement (not a question)
            - correctAnswer MUST be exactly "true" or "false" (lowercase)
            - Aim for roughly 50%% true and 50%% false questions
            - Ensure JSON is valid and parseable
            - Number questions sequentially starting from 1
            """;

    private static final String USER_PROMPT_TEMPLATE = """
            Generate %d true/false questions from the following note:

            **Note Title**: %s

            **Note Content**:
            %s

            %s

            **Instructions**:
            1. Identify the %d most important facts or concepts
            2. Create clear, unambiguous statements (not questions)
            3. Ensure difficulty aligns with: %s
            4. For each statement:
               - Write a clear declarative statement (length: %s)
               - Ensure it is ABSOLUTELY true or false (no partial truths)
               - Set correctAnswer to "true" or "false"
               - Write explanation (length: %s) clarifying why it's true/false
               - Optionally add a hint
            5. Balance true and false statements (roughly 50/50)
            6. Return valid JSON matching the required schema

            EXAMPLES:
            - Good: "Photosynthesis occurs in chloroplasts" → true
            - Bad: "Plants sometimes use photosynthesis" → too vague
            - Good: "The mitochondria is the powerhouse of the cell" → true
            - Bad: "All cells have mitochondria" → false (red blood cells don't)

            **Example Output**:
            {
              "questions": [
                {
                  "id": 1,
                  "type": "TRUE_FALSE",
                  "questionText": "Photosynthesis produces oxygen as a byproduct.",
                  "correctAnswer": "true",
                  "explanation": "During photosynthesis, plants split water molecules (H2O) to obtain electrons and hydrogen ions. This process releases oxygen (O2) as a waste product, which is then released into the atmosphere through stomata in the leaves.",
                  "hint": "Consider what gas plants release during the day that animals need to breathe."
                },
                {
                  "id": 2,
                  "type": "TRUE_FALSE",
                  "questionText": "DNA is stored in the mitochondria of all cells.",
                  "correctAnswer": "false",
                  "explanation": "While mitochondria do contain their own small amount of DNA (mtDNA), the primary storage of DNA in eukaryotic cells is in the nucleus, not the mitochondria. Additionally, not all cells have mitochondria (e.g., mature red blood cells lack them).",
                  "hint": "Think about the main organelle responsible for storing genetic information in the cell."
                },
                {
                  "id": 3,
                  "type": "TRUE_FALSE",
                  "questionText": "Enzymes are proteins that speed up chemical reactions in living organisms.",
                  "correctAnswer": "true",
                  "explanation": "Enzymes are biological catalysts, typically proteins, that accelerate chemical reactions by lowering the activation energy required. They remain unchanged after the reaction and can be used repeatedly.",
                  "hint": "Consider the role of catalysts in biological processes."
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
