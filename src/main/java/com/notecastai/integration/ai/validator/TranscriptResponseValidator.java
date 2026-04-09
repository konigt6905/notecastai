package com.notecastai.integration.ai.validator;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.notecastai.common.exception.AiValidationException;
import com.notecastai.integration.ai.dto.TranscriptAiResponse;
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

    private static final Pattern SPEAKER_LABEL_PATTERN = Pattern.compile("^\\s*[A-Z][a-z]+\\s*:", Pattern.MULTILINE);
    private static final Pattern STAGE_DIRECTION_PATTERN = Pattern.compile("\\[.*?\\]|\\(.*?\\)");
    private static final Pattern URL_PATTERN = Pattern.compile("https?://|www\\.");
    private static final Pattern MARKDOWN_PATTERN = Pattern.compile("[*_`#\\[\\]]");

    public String cleanJsonString(String rawJson) {
        if (rawJson == null || rawJson.isBlank()) {
            throw new AiValidationException("AI response is null or empty", rawJson);
        }

        String trimmed = rawJson.trim();

        Pattern fencePattern = Pattern.compile("^```(?:json)?\\s*([\\s\\S]*?)\\s*```$",
                Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
        Matcher fenceMatcher = fencePattern.matcher(trimmed);
        if (fenceMatcher.matches()) {
            log.debug("Removed JSON markdown fence");
            trimmed = fenceMatcher.group(1).trim();
        }

        // models sometimes wrap the JSON in a sentence like "Here is the response:"
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

            validateRequiredTextField(rootNode, "transcript", errors);
            validateRequiredTextField(rootNode, "estimatedDuration", errors);
            validateRequiredIntegerField(rootNode, "wordCount", errors);

            TranscriptAiResponse response = lenientObjectMapper.treeToValue(rootNode, TranscriptAiResponse.class);

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

        if (transcript.length() < MIN_CHAR_COUNT) {
            errors.add(String.format("Transcript too short (minimum %d characters)", MIN_CHAR_COUNT));
        }

        Matcher speakerMatcher = SPEAKER_LABEL_PATTERN.matcher(transcript);
        if (speakerMatcher.find()) {
            log.warn("Transcript contains speaker labels (e.g., 'John:'). This should be removed.");
            errors.add("Transcript contains speaker labels - should be continuous text only");
        }

        Matcher stageMatcher = STAGE_DIRECTION_PATTERN.matcher(transcript);
        if (stageMatcher.find()) {
            log.warn("Transcript contains brackets or parentheses, possibly stage directions");
            errors.add("Transcript contains brackets/parentheses - should be spoken text only");
        }

        Matcher urlMatcher = URL_PATTERN.matcher(transcript);
        if (urlMatcher.find()) {
            log.warn("Transcript contains URLs - not TTS-friendly");
            errors.add("Transcript contains URLs - not suitable for TTS");
        }

        Matcher markdownMatcher = MARKDOWN_PATTERN.matcher(transcript);
        if (markdownMatcher.find()) {
            log.warn("Transcript contains markdown syntax - should be plain text");
            errors.add("Transcript contains markdown - should be plain spoken text");
        }

        if (transcript.contains("```") || transcript.contains("---")) {
            errors.add("Transcript contains code blocks or dividers - not TTS-friendly");
        }

        if (transcript.matches(".*\\d+\\.\\s+[A-Z].*")) {
            // warning only, numbered lists read OK in TTS most of the time
            log.warn("Transcript might contain numbered lists (e.g., '1. Item')");
        }
    }

    /**
     * Best-effort cleanup. Prefer regenerating instead of calling this.
     */
    public String sanitizeTranscript(String transcript) {
        if (transcript == null) return null;

        String cleaned = transcript;

        cleaned = cleaned.replaceAll("[*_`]", "");
        cleaned = cleaned.replaceAll("\\[.*?\\]", "");
        cleaned = cleaned.replaceAll("\\(.*?\\)", "");
        cleaned = cleaned.replaceAll("https?://\\S+", "");
        cleaned = cleaned.replaceAll("www\\.\\S+", "");
        cleaned = cleaned.replaceAll("\\s+", " ");
        cleaned = cleaned.replaceAll("\\.{2,}", ".");

        return cleaned.trim();
    }
}
