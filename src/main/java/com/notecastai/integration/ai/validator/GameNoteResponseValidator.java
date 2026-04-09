package com.notecastai.integration.ai.validator;

import com.fasterxml.jackson.core.io.JsonEOFException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.notecastai.common.exception.AiValidationException;
import com.notecastai.gamenote.api.dto.GameQuestionDTO;
import com.notecastai.gamenote.domain.QuestionType;
import com.notecastai.integration.ai.dto.GameNoteAiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component
public class GameNoteResponseValidator {

    private final ObjectMapper lenientObjectMapper;

    public GameNoteResponseValidator(@Qualifier("lenientObjectMapper") ObjectMapper lenientObjectMapper) {
        this.lenientObjectMapper = lenientObjectMapper;
    }

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

        // model wrapped the JSON in prose, try to find the actual payload
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

    public GameNoteAiResponse validateGameNoteResponse(String rawJson, QuestionType expectedType, int expectedCount) {
        String cleanedJson = cleanJsonString(rawJson);

        try {
            JsonNode rootNode = lenientObjectMapper.readTree(cleanedJson);
            List<String> errors = new ArrayList<>();

            validateArrayField(rootNode, "questions", errors, true);

            GameNoteAiResponse response = lenientObjectMapper.treeToValue(rootNode, GameNoteAiResponse.class);

            validateQuestions(response.getQuestions(), expectedType, expectedCount, errors);

            if (!errors.isEmpty()) {
                log.error("GameNoteAiResponse validation failed: {}", String.join(", ", errors));
                throw new AiValidationException("AI response validation failed", errors, rawJson);
            }

            log.info("GameNoteAiResponse validation successful: type={}, count={}/{}",
                    expectedType, response.getQuestions().size(), expectedCount);

            return response;

        } catch (AiValidationException e) {
            throw e;
        } catch (Exception e) {
            if (e instanceof JsonEOFException) {
                throw new AiValidationException(
                        "AI response was truncated (likely too long / cut off).",
                        AiValidationException.Code.TOO_LONG_CONTENT,
                        rawJson
                );
            }
            log.error("Failed to parse GameNoteAiResponse: {}", e.getMessage(), e);
            throw new AiValidationException("Failed to parse AI response: " + e.getMessage(), rawJson);
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

    private void validateQuestions(List<GameQuestionDTO> questions, QuestionType expectedType,
                                   int expectedCount, List<String> errors) {
        if (questions == null) {
            errors.add("Questions list cannot be null");
            return;
        }

        if (questions.size() != expectedCount) {
            // accept short responses as long as we got at least one question
            log.warn("Expected {} questions, got {}. Will use what was provided.", expectedCount, questions.size());
            if (questions.isEmpty()) {
                errors.add("No questions were generated");
                return;
            }
        }

        Set<Integer> seenIds = new HashSet<>();
        for (int i = 0; i < questions.size(); i++) {
            GameQuestionDTO question = questions.get(i);
            String prefix = "Question[" + i + "]";

            if (question.getId() == null) {
                errors.add(prefix + " id is missing");
            } else if (seenIds.contains(question.getId())) {
                errors.add(prefix + " duplicate id: " + question.getId());
            } else {
                seenIds.add(question.getId());
            }

            if (question.getType() == null) {
                errors.add(prefix + " type is missing");
            } else if (question.getType() != expectedType) {
                errors.add(prefix + " wrong type (expected " + expectedType + ", got " + question.getType() + ")");
            }

            if (question.getQuestionText() == null || question.getQuestionText().isBlank()) {
                errors.add(prefix + " questionText is missing or empty");
            } else if (question.getQuestionText().length() < 10) {
                errors.add(prefix + " questionText too short (minimum 10 characters)");
            }

            switch (expectedType) {
                case FLASHCARD -> validateFlashcard(question, prefix, errors);
                case MULTIPLE_CHOICE -> validateMultipleChoice(question, prefix, errors);
                case TRUE_FALSE -> validateTrueFalse(question, prefix, errors);
            }
        }
    }

    private void validateFlashcard(GameQuestionDTO question, String prefix, List<String> errors) {
        if (question.getAnswer() == null || question.getAnswer().isBlank()) {
            errors.add(prefix + " answer is required for flashcards");
        } else if (question.getAnswer().length() < 5) {
            errors.add(prefix + " answer too short (minimum 5 characters)");
        }

        if (question.getExplanation() == null || question.getExplanation().isBlank()) {
            log.warn("{} missing explanation (recommended)", prefix);
        }
    }

    private void validateMultipleChoice(GameQuestionDTO question, String prefix, List<String> errors) {
        if (question.getOptions() == null || question.getOptions().isEmpty()) {
            errors.add(prefix + " options array is required for multiple choice");
            return;
        }

        if (question.getOptions().size() != 4) {
            errors.add(prefix + " must have exactly 4 options (found " + question.getOptions().size() + ")");
        }

        if (question.getCorrectAnswer() == null || question.getCorrectAnswer().isBlank()) {
            errors.add(prefix + " correctAnswer is required for multiple choice");
        } else {
            boolean found = question.getOptions().stream()
                    .anyMatch(opt -> opt.equalsIgnoreCase(question.getCorrectAnswer().trim()));
            if (!found) {
                errors.add(prefix + " correctAnswer does not match any option");
            }
        }

        if (question.getExplanation() == null || question.getExplanation().isBlank()) {
            errors.add(prefix + " explanation is required for multiple choice");
        } else if (question.getExplanation().length() < 20) {
            errors.add(prefix + " explanation too short (minimum 20 characters)");
        }
    }

    private void validateTrueFalse(GameQuestionDTO question, String prefix, List<String> errors) {
        if (question.getCorrectAnswer() == null || question.getCorrectAnswer().isBlank()) {
            errors.add(prefix + " correctAnswer is required for true/false");
        } else {
            String answer = question.getCorrectAnswer().trim().toLowerCase();
            if (!answer.equals("true") && !answer.equals("false")) {
                errors.add(prefix + " correctAnswer must be 'true' or 'false' (found: " + question.getCorrectAnswer() + ")");
            }
        }

        if (question.getExplanation() == null || question.getExplanation().isBlank()) {
            errors.add(prefix + " explanation is required for true/false");
        } else if (question.getExplanation().length() < 15) {
            errors.add(prefix + " explanation too short (minimum 15 characters)");
        }
    }

    private void validateOpenQuestion(GameQuestionDTO question, String prefix, List<String> errors) {
        if (question.getAnswer() == null || question.getAnswer().isBlank()) {
            errors.add(prefix + " answer is required for open questions");
        } else if (question.getAnswer().length() < 20) {
            errors.add(prefix + " answer too short (minimum 20 characters)");
        }

        if (question.getExplanation() == null || question.getExplanation().isBlank()) {
            log.warn("{} missing explanation (recommended)", prefix);
        }
    }
}
