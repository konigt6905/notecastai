package com.notecastai.integration.ai.validator;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.notecastai.common.exeption.AiValidationException;
import com.notecastai.integration.ai.prompt.AiAction;
import com.notecastai.integration.ai.provider.openrouter.dto.FormatNoteAiResponse;
import com.notecastai.integration.ai.provider.openrouter.dto.NewNoteAiResponse;
import com.notecastai.tag.domain.TagEntity;
import com.notecastai.tag.repo.TagRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Component
public class AiNoteResponseValidator {

    private final ObjectMapper lenientObjectMapper;
    private final TagRepository tagRepository;

    public AiNoteResponseValidator(@Qualifier("lenientObjectMapper") ObjectMapper lenientObjectMapper, TagRepository tagRepository) {
        this.lenientObjectMapper = lenientObjectMapper;
        this.tagRepository = tagRepository;
    }

    private static final int REQUIRED_AI_ACTIONS = 6;

    /**
     * Clean JSON string - remove markdown fences and extract JSON
     */
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

        // If doesn't start with { or [, try to extract JSON
        if (!trimmed.startsWith("{") && !trimmed.startsWith("[")) {
            int firstBrace = trimmed.indexOf('{');
            int firstBracket = trimmed.indexOf('[');
            int start = -1;

            if (firstBrace != -1 && (firstBracket == -1 || firstBrace < firstBracket)) {
                start = firstBrace;
            } else if (firstBracket != -1) {
                start = firstBracket;
            }

            if (start != -1) {
                int lastBrace = trimmed.lastIndexOf('}');
                int lastBracket = trimmed.lastIndexOf(']');
                int end = -1;

                if (trimmed.charAt(start) == '{' && lastBrace != -1 && lastBrace >= start) {
                    end = lastBrace;
                } else if (trimmed.charAt(start) == '[' && lastBracket != -1 && lastBracket >= start) {
                    end = lastBracket;
                }

                if (end != -1) {
                    log.debug("Extracted JSON from indices {} to {}", start, end);
                    trimmed = trimmed.substring(start, end + 1).trim();
                }
            }
        }

        return trimmed;
    }

    /**
     * Validate and parse NewNoteAiResponse
     */
    public NewNoteAiResponse validateNewNoteResponse(String rawJson, Long userId) {
        String cleanedJson = cleanJsonString(rawJson);

        try {
            // Parse with lenient mapper
            JsonNode rootNode = lenientObjectMapper.readTree(cleanedJson);
            List<String> errors = new ArrayList<>();

            // Validate required fields
            validateRequiredTextField(rootNode, "adjustedTitle", errors);
            validateRequiredTextField(rootNode, "formattedNote", errors);
            validateArrayField(rootNode, "proposedTags", errors, false); // Can be empty
            validateArrayField(rootNode, "proposedAiActions", errors, true); // Must have items

            // Parse to DTO
            NewNoteAiResponse response = lenientObjectMapper.treeToValue(rootNode, NewNoteAiResponse.class);

            // Additional validations
            validateTitle(response.getAdjustedTitle(), errors);
            validateFormattedNote(response.getFormattedNote(), errors);
            validateProposedTags(response.getProposedTags(), userId, errors);
            validateAiActions(response.getProposedAiActions(), errors);

            if (!errors.isEmpty()) {
                log.error("NewNoteAiResponse validation failed: {}", String.join(", ", errors));
                throw new AiValidationException("AI response validation failed", errors, rawJson);
            }

            log.info("NewNoteAiResponse validation successful: title={}, tags={}, actions={}",
                    response.getAdjustedTitle(), response.getProposedTags().size(),
                    response.getProposedAiActions().size());

            return response;

        } catch (AiValidationException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to parse NewNoteAiResponse: {}", e.getMessage(), e);
            throw new AiValidationException("Failed to parse AI response: " + e.getMessage(), rawJson);
        }
    }

    /**
     * Validate and parse FormatNoteAiResponse
     */
    public FormatNoteAiResponse validateFormatNoteResponse(String rawJson, Long userId) {
        String cleanedJson = cleanJsonString(rawJson);

        try {
            JsonNode rootNode = lenientObjectMapper.readTree(cleanedJson);
            List<String> errors = new ArrayList<>();

            validateRequiredTextField(rootNode, "adjustedTitle", errors);
            validateRequiredTextField(rootNode, "knowledgeBase", errors);
            validateArrayField(rootNode, "proposedTags", errors, false);
            validateArrayField(rootNode, "proposedAiActions", errors, true);

            FormatNoteAiResponse response = lenientObjectMapper.treeToValue(rootNode, FormatNoteAiResponse.class);

            validateTitle(response.getAdjustedTitle(), errors);
            validateKnowledgeBase(response.getKnowledgeBase(), errors);
            validateProposedTags(response.getProposedTags(), userId, errors);
            validateAiActions(response.getProposedAiActions(), errors);

            if (!errors.isEmpty()) {
                log.error("FormatNoteAiResponse validation failed: {}", String.join(", ", errors));
                throw new AiValidationException("AI response validation failed", errors, rawJson);
            }

            log.info("FormatNoteAiResponse validation successful: title={}, knowledgeBase length={}, tags={}, actions={}",
                    response.getAdjustedTitle(), response.getKnowledgeBase().length(),
                    response.getProposedTags().size(), response.getProposedAiActions().size());

            return response;

        } catch (AiValidationException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to parse FormatNoteAiResponse: {}", e.getMessage(), e);
            throw new AiValidationException("Failed to parse AI response: " + e.getMessage(), rawJson);
        }
    }

    // Validation helper methods

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

    private void validateArrayField(JsonNode node, String fieldName, List<String> errors, boolean mustHaveItems) {
        JsonNode field = node.path(fieldName);
        if (field.isMissingNode()) {
            errors.add(fieldName + " is missing");
        } else if (!field.isArray()) {
            errors.add(fieldName + " is not an array");
        } else if (mustHaveItems && field.isEmpty()) {
            errors.add(fieldName + " cannot be empty");
        }
    }

    private void validateTitle(String title, List<String> errors) {
        if (title == null || title.isBlank()) {
            errors.add("Title cannot be empty");
            return;
        }

        String[] words = title.trim().split("\\s+");
        if (words.length < 2) {
            errors.add("Title too short (minimum 2 words)");
        } else if (words.length > 10) {
            errors.add("Title too long (maximum 10 words)");
        }
    }

    private void validateFormattedNote(String formattedNote, List<String> errors) {
        if (formattedNote == null || formattedNote.isBlank()) {
            errors.add("Formatted note cannot be empty");
            return;
        }

        if (formattedNote.length() < 50) {
            errors.add("Formatted note too short (minimum 50 characters)");
        }
    }

    private void validateKnowledgeBase(String knowledgeBase, List<String> errors) {
        if (knowledgeBase == null || knowledgeBase.isBlank()) {
            errors.add("Knowledge base cannot be empty");
            return;
        }
    }

    private void validateProposedTags(List<String> proposedTags, Long userId, List<String> errors) {
        if (proposedTags == null) {
            errors.add("Proposed tags cannot be null");
            return;
        }

        if (proposedTags.isEmpty()) {
            log.warn("No tags proposed by AI");
            return; // Empty is acceptable
        }

        if (proposedTags.size() > 10) {
            errors.add("Too many proposed tags (maximum 10)");
        }

        // Check if tags exist in user's tag list
        List<String> invalidTags = new ArrayList<>();
        for (String tagName : proposedTags) {
            if (tagName == null || tagName.isBlank()) {
                invalidTags.add("(empty tag)");
                continue;
            }

            TagEntity tag = tagRepository.findByNameAndUserId(tagName.trim(), userId);
            if (tag == null) {
                invalidTags.add(tagName);
            }
        }

        if (!invalidTags.isEmpty()) {
            log.warn("Proposed tags not found in user's tags: {}", invalidTags);
            // Don't add to errors - we'll filter these out in service layer
        }
    }

    private void validateAiActions(List<AiAction> actions, List<String> errors) {
        if (actions == null) {
            errors.add("Proposed AI actions cannot be null");
            return;
        }

        if (actions.size() != REQUIRED_AI_ACTIONS) {
            errors.add(String.format("Expected exactly %d AI actions, found %d",
                    REQUIRED_AI_ACTIONS, actions.size()));
        }

        for (int i = 0; i < actions.size(); i++) {
            AiAction action = actions.get(i);
            String prefix = "Action[" + i + "]";

            if (action.getName() == null || action.getName().isBlank()) {
                errors.add(prefix + " name cannot be empty");
            } else {
                String[] words = action.getName().trim().split("\\s+");
                if (words.length > 6) {
                    errors.add(prefix + " name too long (maximum 6 words)");
                }
            }

            if (action.getPrompt() == null || action.getPrompt().isBlank()) {
                errors.add(prefix + " prompt cannot be empty");
            } else if (action.getPrompt().length() < 20) {
                errors.add(prefix + " prompt too short (minimum 20 characters)");
            }
        }
    }

    /**
     * Filter and map valid tags to IDs
     */
    public List<Long> filterAndMapTagsToIds(List<String> proposedTags, Long userId) {
        if (proposedTags == null || proposedTags.isEmpty()) {
            return List.of();
        }

        return proposedTags.stream()
                .filter(tagName -> tagName != null && !tagName.isBlank())
                .map(tagName -> tagRepository.findByNameAndUserId(tagName.trim(), userId))
                .filter(tag -> tag != null)
                .map(TagEntity::getId)
                .distinct()
                .collect(Collectors.toList());
    }
}