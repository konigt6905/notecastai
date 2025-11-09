package com.notecastai.integration.ai.validator;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.notecastai.common.exeption.AiValidationException;
import com.notecastai.integration.ai.provider.openrouter.dto.TranscriptAiResponse;
import com.notecastai.notecast.domain.TranscriptSize;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component
public class TranscriptResponseValidator {

    private final ObjectMapper lenientObjectMapper;

    public TranscriptResponseValidator(@Qualifier("lenientObjectMapper") ObjectMapper lenientObjectMapper) {
        this.lenientObjectMapper = lenientObjectMapper;
    }

    private static final int MIN_CHAR_COUNT = 100;

    // Patterns to detect non-TTS-friendly content
    private static final Pattern SPEAKER_LABEL_PATTERN = Pattern.compile("^\\s*[A-Z][a-z]+\\s*:", Pattern.MULTILINE);
    private static final Pattern STAGE_DIRECTION_PATTERN = Pattern.compile("\\[.*?\\]|\\(.*?\\)");
    private static final Pattern URL_PATTERN = Pattern.compile("https?://|www\\.");
    private static final Pattern MARKDOWN_PATTERN = Pattern.compile("[*_`#\\[\\]]");

    public String cleanJsonString(String rawJson) {
        if (rawJson == null || rawJson.isBlank()) {
            throw new AiValidationException("AI response is null or empty", rawJson);
        }

        String trimmed = rawJson.trim();

        // Remove markdown code fences
        Pattern fencePattern = Pattern.compile("^```(?:json)?\\s*([\\s\\S]*?)\\s*```$",
                Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
        Matcher fenceMatcher = fencePattern.matcher(trimmed);
        if (fenceMatcher.matches()) {
            log.debug("Removed JSON markdown fence");
            trimmed = fenceMatcher.group(1).trim();
        }

        // Extract JSON if wrapped in text
        if (!trimmed.startsWith("{")) {
            int start = trimmed.indexOf('{');
            int end = trimmed.lastIndexOf('}');
            if (start != -1 && end != -1 && end > start) {
                log.debug("Extracted JSON from indices {} to {}", start, end);
                trimmed = trimmed.substring(start, end + 1);
            }
        }

        return trimmed;
    }

    public TranscriptAiResponse validateTranscriptResponse(String rawJson, TranscriptSize expectedSize) {
        String cleanedJson = cleanJsonString(rawJson);

        try {
            JsonNode rootNode = lenientObjectMapper.readTree(cleanedJson);
            List<String> errors = new ArrayList<>();

            // Validate required fields exist
            validateRequiredTextField(rootNode, "transcript", errors);
            validateRequiredTextField(rootNode, "estimatedDuration", errors);
            validateRequiredIntegerField(rootNode, "wordCount", errors);

            // Parse to DTO
            TranscriptAiResponse response = lenientObjectMapper.treeToValue(rootNode, TranscriptAiResponse.class);

            // Deep validation with size context
            validateTranscriptContent(response.getTranscript(), errors);

            if (!errors.isEmpty()) {
                log.error("TranscriptAiResponse validation failed: {}", String.join(", ", errors));
                throw new AiValidationException("Transcript validation failed", errors, rawJson);
            }

            log.info("TranscriptAiResponse validation successful: size={}, wordCount={}, duration={}",
                    expectedSize.getLabel(), response.getWordCount(), response.getEstimatedDuration());

            return response;

        } catch (AiValidationException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to parse TranscriptAiResponse: {}", e.getMessage(), e);
            throw new AiValidationException("Failed to parse transcript response: " + e.getMessage(), rawJson);
        }
    }

    private void validateRequiredTextField(JsonNode node, String fieldName, List<String> errors) {
        JsonNode field = node.path(fieldName);
        if (field.isMissingNode()) {
            errors.add(fieldName + " is missing");
        } else if (!field.isTextual()) {
            errors.add(fieldName + " is not a text string");
        } else if (field.asText().trim().isEmpty()) {
            errors.add(fieldName + " cannot be empty");
        }
    }

    private void validateRequiredIntegerField(JsonNode node, String fieldName, List<String> errors) {
        JsonNode field = node.path(fieldName);
        if (field.isMissingNode()) {
            errors.add(fieldName + " is missing");
        } else if (!field.isInt()) {
            errors.add(fieldName + " is not an integer");
        }
    }

    private void validateTranscriptContent(String transcript, List<String> errors) {
        if (transcript == null || transcript.isBlank()) {
            errors.add("Transcript cannot be empty");
            return;
        }

        // Check minimum length
        if (transcript.length() < MIN_CHAR_COUNT) {
            errors.add(String.format("Transcript too short (minimum %d characters)", MIN_CHAR_COUNT));
        }

        // Check for speaker labels
        Matcher speakerMatcher = SPEAKER_LABEL_PATTERN.matcher(transcript);
        if (speakerMatcher.find()) {
            log.warn("Transcript contains speaker labels (e.g., 'John:'). This should be removed.");
            errors.add("Transcript contains speaker labels - should be continuous text only");
        }

        // Check for stage directions
        Matcher stageMatcher = STAGE_DIRECTION_PATTERN.matcher(transcript);
        if (stageMatcher.find()) {
            log.warn("Transcript contains brackets or parentheses, possibly stage directions");
            errors.add("Transcript contains brackets/parentheses - should be spoken text only");
        }

        // Check for URLs
        Matcher urlMatcher = URL_PATTERN.matcher(transcript);
        if (urlMatcher.find()) {
            log.warn("Transcript contains URLs - not TTS-friendly");
            errors.add("Transcript contains URLs - not suitable for TTS");
        }

        // Check for markdown
        Matcher markdownMatcher = MARKDOWN_PATTERN.matcher(transcript);
        if (markdownMatcher.find()) {
            log.warn("Transcript contains markdown syntax - should be plain text");
            errors.add("Transcript contains markdown - should be plain spoken text");
        }

        // Check for common non-TTS issues
        if (transcript.contains("```") || transcript.contains("---")) {
            errors.add("Transcript contains code blocks or dividers - not TTS-friendly");
        }

        if (transcript.matches(".*\\d+\\.\\s+[A-Z].*")) {
            log.warn("Transcript might contain numbered lists (e.g., '1. Item')");
            // This is a warning, not necessarily an error for TTS
        }
    }

    /**
     * Clean up transcript if minor issues found
     * (Use cautiously - better to have AI regenerate)
     */
    public String sanitizeTranscript(String transcript) {
        if (transcript == null) return null;

        String cleaned = transcript;

        // Remove any remaining markdown
        cleaned = cleaned.replaceAll("[*_`]", "");

        // Remove brackets and parentheses with content
        cleaned = cleaned.replaceAll("\\[.*?\\]", "");
        cleaned = cleaned.replaceAll("\\(.*?\\)", "");

        // Remove URLs
        cleaned = cleaned.replaceAll("https?://\\S+", "");
        cleaned = cleaned.replaceAll("www\\.\\S+", "");

        // Clean up multiple spaces
        cleaned = cleaned.replaceAll("\\s+", " ");

        // Clean up multiple periods
        cleaned = cleaned.replaceAll("\\.{2,}", ".");

        return cleaned.trim();
    }
}
