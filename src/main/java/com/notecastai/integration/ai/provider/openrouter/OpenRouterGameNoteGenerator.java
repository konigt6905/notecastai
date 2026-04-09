package com.notecastai.integration.ai.provider.openrouter;

import com.notecastai.common.exception.BusinessException;
import com.notecastai.integration.ai.GameNoteAiGenerator;
import com.notecastai.integration.ai.dto.GameNoteAiRequest;
import com.notecastai.integration.ai.dto.GameNoteAiResponse;
import com.notecastai.integration.ai.prompt.GameNoteFlashcardPromptBuilder;
import com.notecastai.integration.ai.prompt.GameNoteMultipleChoicePromptBuilder;
import com.notecastai.integration.ai.prompt.GameNoteTrueFalsePromptBuilder;
import com.notecastai.integration.ai.provider.openrouter.client.JsonSchemaBuilder;
import com.notecastai.integration.ai.provider.openrouter.client.OpenRouterClient;
import com.notecastai.integration.ai.provider.openrouter.dto.OpenRouterJsonSchema;
import com.notecastai.integration.ai.provider.openrouter.dto.OpenRouterModel;
import com.notecastai.integration.ai.provider.openrouter.dto.OpenRouterResponse;
import com.notecastai.integration.ai.validator.GameNoteResponseValidator;
import io.github.resilience4j.retry.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OpenRouterGameNoteGenerator implements GameNoteAiGenerator {

    private final OpenRouterClient openRouterClient;
    private final GameNoteResponseValidator gameNoteValidator;
    private final Retry noteAiRetry;

    @Override
    public GameNoteAiResponse generateGameQuestions(GameNoteAiRequest request) {
        log.info("Generating game questions: type={}, difficulty={}, count={}",
                request.getQuestionType(), request.getDifficulty(), request.getNumberOfQuestions());

        String systemPrompt;
        String userPrompt;
        OpenRouterJsonSchema jsonSchema;

        switch (request.getQuestionType()) {
            case FLASHCARD -> {
                GameNoteFlashcardPromptBuilder builder = GameNoteFlashcardPromptBuilder.builder()
                        .noteTitle(request.getNoteTitle())
                        .noteContent(request.getNoteContent())
                        .numberOfQuestions(request.getNumberOfQuestions())
                        .difficulty(request.getDifficulty())
                        .customInstructions(request.getCustomInstructions())
                        .build();
                systemPrompt = builder.getSystemPrompt();
                userPrompt = builder.getUserPrompt();
                jsonSchema = JsonSchemaBuilder.buildGameNoteFlashcardSchema();
            }
            case MULTIPLE_CHOICE -> {
                GameNoteMultipleChoicePromptBuilder builder = GameNoteMultipleChoicePromptBuilder.builder()
                        .noteTitle(request.getNoteTitle())
                        .noteContent(request.getNoteContent())
                        .numberOfQuestions(request.getNumberOfQuestions())
                        .difficulty(request.getDifficulty())
                        .customInstructions(request.getCustomInstructions())
                        .build();
                systemPrompt = builder.getSystemPrompt();
                userPrompt = builder.getUserPrompt();
                jsonSchema = JsonSchemaBuilder.buildGameNoteMultipleChoiceSchema();
            }
            case TRUE_FALSE -> {
                GameNoteTrueFalsePromptBuilder builder = GameNoteTrueFalsePromptBuilder.builder()
                        .noteTitle(request.getNoteTitle())
                        .noteContent(request.getNoteContent())
                        .numberOfQuestions(request.getNumberOfQuestions())
                        .difficulty(request.getDifficulty())
                        .customInstructions(request.getCustomInstructions())
                        .build();
                systemPrompt = builder.getSystemPrompt();
                userPrompt = builder.getUserPrompt();
                jsonSchema = JsonSchemaBuilder.buildGameNoteTrueFalseSchema();
            }
            default -> throw BusinessException.of(BusinessException.BusinessCode.INVALID_REQUEST
                    .append(" Unknown question type: " + request.getQuestionType()));
        }

        return Retry.decorateSupplier(noteAiRetry, () -> {
            log.debug("Calling OpenRouter AI for {} question generation", request.getQuestionType());

            OpenRouterResponse response = openRouterClient.chatCompletion(
                    OpenRouterModel.GROK_FAST_1,
                    systemPrompt,
                    userPrompt,
                    jsonSchema
            );

            GameNoteAiResponse aiResponse = gameNoteValidator.validateGameNoteResponse(
                    response.getContent(),
                    request.getQuestionType(),
                    request.getNumberOfQuestions()
            );

            log.info("Game questions generated and validated successfully: type={}, count={}/{}",
                    request.getQuestionType(),
                    aiResponse.getQuestions().size(),
                    request.getNumberOfQuestions());

            return aiResponse;
        }).get();
    }
}
