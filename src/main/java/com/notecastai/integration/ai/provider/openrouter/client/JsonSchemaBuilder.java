package com.notecastai.integration.ai.provider.openrouter.client;

import com.notecastai.integration.ai.provider.openrouter.dto.OpenRouterJsonSchema;

import java.util.List;
import java.util.Map;

public class JsonSchemaBuilder {

    /**
     * Build schema for NewNoteAiResponse
     */
    public static OpenRouterJsonSchema buildNewNoteSchema() {
        return OpenRouterJsonSchema.builder()
                .name("new_note_response")
                .strict(true)
                .schema(Map.of(
                        "type", "object",
                        "properties", Map.of(
                                "adjustedTitle", Map.of(
                                        "type", "string",
                                        "description", "Clear, concise title (3-8 words)"
                                ),
                                "formattedNote", Map.of(
                                        "type", "string",
                                        "description", "Full formatted note content using Markdown"
                                ),
                                "proposedTags", Map.of(
                                        "type", "array",
                                        "description", "List of proposed tags from available tags",
                                        "items", Map.of("type", "string")
                                ),
                                "proposedAiActions", Map.of(
                                        "type", "array",
                                        "description", "List of 6 proposed AI actions",
                                        "items", Map.of(
                                                "type", "object",
                                                "properties", Map.of(
                                                        "name", Map.of(
                                                                "type", "string",
                                                                "description", "Action name (2-4 words)"
                                                        ),
                                                        "prompt", Map.of(
                                                                "type", "string",
                                                                "description", "Clear instruction for the action"
                                                        )
                                                ),
                                                "required", List.of("name", "prompt"),
                                                "additionalProperties", false
                                        ),
                                        "minItems", 6,
                                        "maxItems", 6
                                )
                        ),
                        "required", List.of("adjustedTitle", "formattedNote", "proposedTags", "proposedAiActions"),
                        "additionalProperties", false
                ))
                .build();
    }

    /**
     * Build schema for FormatNoteAiResponse
     */
    public static OpenRouterJsonSchema buildFormatNoteSchema() {
        return OpenRouterJsonSchema.builder()
                .name("format_note_response")
                .strict(true)
                .schema(Map.of(
                        "type", "object",
                        "properties", Map.of(
                                "adjustedTitle", Map.of(
                                        "type", "string",
                                        "description", "Updated title (3-8 words)"
                                ),
                                "knowledgeBase", Map.of(
                                        "type", "string",
                                        "description", "Reformatted content as plain text"
                                ),
                                "proposedTags", Map.of(
                                        "type", "array",
                                        "description", "List of proposed tags from available tags",
                                        "items", Map.of("type", "string")
                                ),
                                "proposedAiActions", Map.of(
                                        "type", "array",
                                        "description", "List of 6 proposed AI actions",
                                        "items", Map.of(
                                                "type", "object",
                                                "properties", Map.of(
                                                        "name", Map.of(
                                                                "type", "string",
                                                                "description", "Action name (2-4 words)"
                                                        ),
                                                        "prompt", Map.of(
                                                                "type", "string",
                                                                "description", "Clear instruction for the action"
                                                        )
                                                ),
                                                "required", List.of("name", "prompt"),
                                                "additionalProperties", false
                                        ),
                                        "minItems", 6,
                                        "maxItems", 6
                                )
                        ),
                        "required", List.of("adjustedTitle", "knowledgeBase", "proposedTags", "proposedAiActions"),
                        "additionalProperties", false
                ))
                .build();
    }

    /**
     * Build schema for TranscriptAiResponse
     */
    public static OpenRouterJsonSchema buildTranscriptSchema() {
        return OpenRouterJsonSchema.builder()
                .name("transcript_response")
                .strict(true)
                .schema(Map.of(
                        "type", "object",
                        "properties", Map.of(
                                "transcript", Map.of(
                                        "type", "string",
                                        "description", "TTS-ready transcript text. No labels, no special formatting. Just natural spoken text."
                                ),
                                "estimatedDuration", Map.of(
                                        "type", "string",
                                        "description", "Estimated speaking time in format 'X min Y sec'"
                                ),
                                "wordCount", Map.of(
                                        "type", "integer",
                                        "description", "Exact word count of the transcript"
                                )
                        ),
                        "required", List.of("transcript", "estimatedDuration", "wordCount"),
                        "additionalProperties", false
                ))
                .build();
    }

    /**
     * Build schema for Flashcard game questions
     */
    public static OpenRouterJsonSchema buildGameNoteFlashcardSchema() {
        return OpenRouterJsonSchema.builder()
                .name("flashcard_questions_response")
                .strict(true)
                .schema(Map.of(
                        "type", "object",
                        "properties", Map.of(
                                "questions", Map.of(
                                        "type", "array",
                                        "description", "Array of flashcard questions",
                                        "items", Map.of(
                                                "type", "object",
                                                "properties", Map.of(
                                                        "id", Map.of(
                                                                "type", "integer",
                                                                "description", "Sequential question ID starting from 1"
                                                        ),
                                                        "type", Map.of(
                                                                "type", "string",
                                                                "description", "Must be FLASHCARD",
                                                                "enum", List.of("FLASHCARD")
                                                        ),
                                                        "questionText", Map.of(
                                                                "type", "string",
                                                                "description", "The flashcard question"
                                                        ),
                                                        "answer", Map.of(
                                                                "type", "string",
                                                                "description", "The answer to the flashcard question"
                                                        ),
                                                        "explanation", Map.of(
                                                                "type", "string",
                                                                "description", "Explanation providing additional context"
                                                        ),
                                                        "hint", Map.of(
                                                                "type", "string",
                                                                "description", "Optional hint to help recall"
                                                        )
                                                ),
                                                "required", List.of("id", "type", "questionText", "answer"),
                                                "additionalProperties", false
                                        ),
                                        "minItems", 1
                                )
                        ),
                        "required", List.of("questions"),
                        "additionalProperties", false
                ))
                .build();
    }

    /**
     * Build schema for Multiple Choice game questions
     */
    public static OpenRouterJsonSchema buildGameNoteMultipleChoiceSchema() {
        return OpenRouterJsonSchema.builder()
                .name("multiple_choice_questions_response")
                .strict(true)
                .schema(Map.of(
                        "type", "object",
                        "properties", Map.of(
                                "questions", Map.of(
                                        "type", "array",
                                        "description", "Array of multiple choice questions",
                                        "items", Map.of(
                                                "type", "object",
                                                "properties", Map.of(
                                                        "id", Map.of(
                                                                "type", "integer",
                                                                "description", "Sequential question ID starting from 1"
                                                        ),
                                                        "type", Map.of(
                                                                "type", "string",
                                                                "description", "Must be MULTIPLE_CHOICE",
                                                                "enum", List.of("MULTIPLE_CHOICE")
                                                        ),
                                                        "questionText", Map.of(
                                                                "type", "string",
                                                                "description", "The multiple choice question"
                                                        ),
                                                        "options", Map.of(
                                                                "type", "array",
                                                                "description", "Exactly 4 answer options with letters A, B, C, D",
                                                                "items", Map.of("type", "string"),
                                                                "minItems", 4,
                                                                "maxItems", 4
                                                        ),
                                                        "correctAnswer", Map.of(
                                                                "type", "string",
                                                                "description", "The correct answer (must match one option exactly)"
                                                        ),
                                                        "explanation", Map.of(
                                                                "type", "string",
                                                                "description", "Detailed explanation of why the answer is correct"
                                                        ),
                                                        "hint", Map.of(
                                                                "type", "string",
                                                                "description", "Optional hint to guide thinking"
                                                        )
                                                ),
                                                "required", List.of("id", "type", "questionText", "options", "correctAnswer", "explanation"),
                                                "additionalProperties", false
                                        ),
                                        "minItems", 1
                                )
                        ),
                        "required", List.of("questions"),
                        "additionalProperties", false
                ))
                .build();
    }

    /**
     * Build schema for True/False game questions
     */
    public static OpenRouterJsonSchema buildGameNoteTrueFalseSchema() {
        return OpenRouterJsonSchema.builder()
                .name("true_false_questions_response")
                .strict(true)
                .schema(Map.of(
                        "type", "object",
                        "properties", Map.of(
                                "questions", Map.of(
                                        "type", "array",
                                        "description", "Array of true/false questions",
                                        "items", Map.of(
                                                "type", "object",
                                                "properties", Map.of(
                                                        "id", Map.of(
                                                                "type", "integer",
                                                                "description", "Sequential question ID starting from 1"
                                                        ),
                                                        "type", Map.of(
                                                                "type", "string",
                                                                "description", "Must be TRUE_FALSE",
                                                                "enum", List.of("TRUE_FALSE")
                                                        ),
                                                        "questionText", Map.of(
                                                                "type", "string",
                                                                "description", "A declarative statement (not a question)"
                                                        ),
                                                        "correctAnswer", Map.of(
                                                                "type", "string",
                                                                "description", "Must be exactly 'true' or 'false' (lowercase)",
                                                                "enum", List.of("true", "false")
                                                        ),
                                                        "explanation", Map.of(
                                                                "type", "string",
                                                                "description", "Explanation of why the statement is true or false"
                                                        ),
                                                        "hint", Map.of(
                                                                "type", "string",
                                                                "description", "Optional hint to guide reasoning"
                                                        )
                                                ),
                                                "required", List.of("id", "type", "questionText", "correctAnswer", "explanation"),
                                                "additionalProperties", false
                                        ),
                                        "minItems", 1
                                )
                        ),
                        "required", List.of("questions"),
                        "additionalProperties", false
                ))
                .build();
    }

    /**
     * Build schema for Open Question game questions
     */
    public static OpenRouterJsonSchema buildGameNoteOpenQuestionSchema() {
        return OpenRouterJsonSchema.builder()
                .name("open_question_response")
                .strict(true)
                .schema(Map.of(
                        "type", "object",
                        "properties", Map.of(
                                "questions", Map.of(
                                        "type", "array",
                                        "description", "Array of open-ended questions",
                                        "items", Map.of(
                                                "type", "object",
                                                "properties", Map.of(
                                                        "id", Map.of(
                                                                "type", "integer",
                                                                "description", "Sequential question ID starting from 1"
                                                        ),
                                                        "type", Map.of(
                                                                "type", "string",
                                                                "description", "Must be OPEN_QUESTION",
                                                                "enum", List.of("OPEN_QUESTION")
                                                        ),
                                                        "questionText", Map.of(
                                                                "type", "string",
                                                                "description", "The open-ended question requiring explanation"
                                                        ),
                                                        "answer", Map.of(
                                                                "type", "string",
                                                                "description", "Comprehensive model answer"
                                                        ),
                                                        "explanation", Map.of(
                                                                "type", "string",
                                                                "description", "Key points or guidance for a good answer"
                                                        ),
                                                        "hint", Map.of(
                                                                "type", "string",
                                                                "description", "Optional hint to guide thinking direction"
                                                        )
                                                ),
                                                "required", List.of("id", "type", "questionText", "answer"),
                                                "additionalProperties", false
                                        ),
                                        "minItems", 1
                                )
                        ),
                        "required", List.of("questions"),
                        "additionalProperties", false
                ))
                .build();
    }
}