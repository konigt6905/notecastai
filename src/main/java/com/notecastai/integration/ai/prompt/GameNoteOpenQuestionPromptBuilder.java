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
public class GameNoteOpenQuestionPromptBuilder {

    private String noteTitle;
    private String noteContent;
    private Integer numberOfQuestions;
    private QuestionLength questionLength;
    private AnswerLength answerLength;
    private DifficultyLevel difficulty;
    private String customInstructions;

    private static final String SYSTEM_PROMPT_TEMPLATE = """
            You are an expert educator specializing in creating thought-provoking open-ended questions that promote critical thinking and deep understanding.

            YOUR TASK:
            Generate %d OPEN QUESTION items from the provided note content. These questions require explanatory answers and test comprehension, analysis, and synthesis.

            CONFIGURATION:
            - Question Length: %s (%s)
            - Model Answer Length: %s (%s)
            - Difficulty Level: %s
            - Difficulty Guidance: %s

            OPEN QUESTION BEST PRACTICES:
            1. **Encourage deep thinking** - Questions should require explanation, not just recall
            2. **Test understanding, not memorization** - Focus on "why" and "how"
            3. **Clear but open-ended** - Question should be clear, but allow for elaboration
            4. **Realistic expectations** - Answer should be achievable at the given difficulty level
            5. **Model answer as guide** - Provide a comprehensive model answer
            6. **Progressive difficulty** - Start with understanding, move to application/analysis
            7. **Context matters** - Provide hints that guide thinking without giving away answer

            QUESTION TYPES BY DIFFICULTY:
            - **Easy**: Explain, describe, define, list
            - **Medium**: Compare, contrast, analyze, relate
            - **Hard**: Evaluate, synthesize, propose, critique

            QUESTION FORMATTING:
            - %s questions should be: %s
            - %s model answers should be: %s

            DIFFICULTY REQUIREMENTS (%s):
            %s

            OUTPUT FORMAT:
            Return a JSON object with this exact structure:
            {
              "questions": [
                {
                  "id": 1,
                  "type": "OPEN_QUESTION",
                  "questionText": "Thought-provoking question that requires explanation?",
                  "answer": "Comprehensive model answer demonstrating expected depth",
                  "explanation": "Additional context or key points to consider",
                  "hint": "Optional hint to guide thinking direction (if useful)"
                }
              ]
            }

            CRITICAL REQUIREMENTS:
            - Generate EXACTLY %d questions
            - All questions MUST have type "OPEN_QUESTION"
            - Each question MUST have: id, type, questionText, answer, explanation
            - Questions should end with question mark
            - Answer should demonstrate the depth and detail expected
            - Explanation can highlight key concepts or common mistakes
            - Ensure JSON is valid and parseable
            - Number questions sequentially starting from 1
            """;

    private static final String USER_PROMPT_TEMPLATE = """
            Generate %d open-ended questions from the following note:

            **Note Title**: %s

            **Note Content**:
            %s

            %s

            **Instructions**:
            1. Identify the %d most important concepts that warrant deep exploration
            2. Create questions that test true understanding (not just recall)
            3. Ensure difficulty aligns with: %s
            4. For each question:
               - Write a clear, thought-provoking question (length: %s)
               - Question should require explanation, not one-word answer
               - Provide comprehensive model answer (length: %s)
               - Add explanation with key points or common pitfalls
               - Optionally add hint to guide thinking
            5. Progress from simpler to more complex questions
            6. Return valid JSON matching the required schema

            EXAMPLE QUESTION STEMS:
            - Easy: "Explain the process of...", "What are the main components of...", "Describe how..."
            - Medium: "Compare and contrast X and Y", "Analyze the relationship between...", "How does X affect Y?"
            - Hard: "Evaluate the effectiveness of...", "Propose a solution for...", "Synthesize the key concepts..."

            **Example Output**:
            {
              "questions": [
                {
                  "id": 1,
                  "type": "OPEN_QUESTION",
                  "questionText": "Explain the process of photosynthesis and describe why it is essential for life on Earth.",
                  "answer": "Photosynthesis is the process by which plants, algae, and some bacteria convert light energy into chemical energy stored in glucose. The process occurs in two main stages: the light-dependent reactions (which occur in the thylakoid membranes and produce ATP and NADPH) and the light-independent reactions or Calvin cycle (which occur in the stroma and use ATP and NADPH to fix carbon dioxide into glucose). Photosynthesis is essential for life on Earth because it produces oxygen that most organisms need for respiration, creates the base of most food chains by producing organic compounds, and removes carbon dioxide from the atmosphere, helping regulate Earth's climate.",
                  "explanation": "A complete answer should mention both stages of photosynthesis, the main inputs (light, CO2, water) and outputs (glucose, oxygen), and explain at least two reasons why it's essential (oxygen production, food chain base, carbon dioxide removal).",
                  "hint": "Think about what plants need (inputs) to make their food and what they release (outputs) that benefits other organisms."
                },
                {
                  "id": 2,
                  "type": "OPEN_QUESTION",
                  "questionText": "Compare and contrast mitochondria and chloroplasts in terms of their structure and function.",
                  "answer": "Both mitochondria and chloroplasts are double-membrane organelles that produce energy through similar mechanisms (electron transport chains and chemiosmosis), and both contain their own DNA and ribosomes, suggesting an evolutionary origin through endosymbiosis. However, they differ significantly in function and structure: Mitochondria perform cellular respiration, converting glucose and oxygen into ATP and releasing CO2 and water, while chloroplasts perform photosynthesis, converting light energy, CO2, and water into glucose and oxygen. Structurally, mitochondria have cristae (inner membrane folds) where ATP synthesis occurs, while chloroplasts have thylakoids (stacked into grana) where light-dependent reactions occur, plus a stroma where the Calvin cycle takes place. In essence, chloroplasts store energy in glucose while mitochondria release energy from glucose.",
                  "explanation": "A strong answer should identify both similarities (double membranes, energy production, own DNA) and differences (opposite reactions, different internal structures, opposite inputs/outputs). The answer should demonstrate understanding that these organelles perform complementary roles in energy metabolism.",
                  "hint": "Consider that these organelles perform opposite but complementary reactions related to energy."
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
