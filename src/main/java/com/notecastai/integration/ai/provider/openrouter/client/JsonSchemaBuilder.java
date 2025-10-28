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
}