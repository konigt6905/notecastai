package com.notecastai.integration.ai.provider.openrouter.client;

import com.notecastai.common.exeption.TechnicalException;
import com.notecastai.integration.ai.openrouter.dto.*;
import com.notecastai.integration.ai.provider.openrouter.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class OpenRouterClient {

    private final RestClient openRouterRestClient;

    /**
     * Execute chat completion with system and user prompts only (no history, no schema)
     */
    public OpenRouterResponse chatCompletion(
            OpenRouterModel model,
            String systemPrompt,
            String userPrompt
    ) {
        return chatCompletion(model, systemPrompt, userPrompt, null, null);
    }

    /**
     * Execute chat completion with structured output schema
     */
    public OpenRouterResponse chatCompletion(
            OpenRouterModel model,
            String systemPrompt,
            String userPrompt,
            OpenRouterJsonSchema jsonSchema
    ) {
        return chatCompletion(model, systemPrompt, userPrompt, null, jsonSchema);
    }

    /**
     * Execute chat completion for conversational responses (markdown, no structured JSON)
     */
    public OpenRouterResponse chatCompletionMarkdown(
            OpenRouterModel model,
            String systemPrompt,
            String userPrompt,
            List<OpenRouterChatMessage> messageHistory
    ) {
        List<OpenRouterChatMessage> messages = buildMessages(systemPrompt, userPrompt, messageHistory);

        OpenRouterChatRequest request = OpenRouterChatRequest.builder()
                .model(model.getModelId())
                .messages(messages)
                .maxTokens(model.getMaxTokens())
                .temperature(model.getTemperature())
                .stream(false)
                .build();

        // No response format - let AI respond naturally in markdown
        return executeRequest(request, model);
    }

    /**
     * Execute chat completion with system prompt, user prompt, optional history, and optional schema
     */
    public OpenRouterResponse chatCompletion(
            OpenRouterModel model,
            String systemPrompt,
            String userPrompt,
            List<OpenRouterChatMessage> messageHistory,
            OpenRouterJsonSchema jsonSchema
    ) {
        List<OpenRouterChatMessage> messages = buildMessages(systemPrompt, userPrompt, messageHistory);

        OpenRouterChatRequest.OpenRouterChatRequestBuilder requestBuilder = OpenRouterChatRequest.builder()
                .model(model.getModelId())
                .messages(messages)
                .maxTokens(model.getMaxTokens())
                .temperature(model.getTemperature())
                .stream(false);

        // Add structured output schema if provided
        if (jsonSchema != null) {
            requestBuilder.responseFormat(OpenRouterResponseFormat.builder()
                    .type("json_schema")
                    .jsonSchema(jsonSchema)
                    .build());
        } else if (model.isSupportsJsonMode()) {
            // Fallback to simple JSON mode if no schema provided
            requestBuilder.responseFormat(OpenRouterResponseFormat.builder()
                    .type("json_object")
                    .build());
        }

        return executeRequest(requestBuilder.build(), model);
    }

    /**
     * Execute chat completion with full control over request
     */
    public OpenRouterResponse chatCompletion(OpenRouterChatRequest request) {
        return executeRequest(request, null);
    }

    private List<OpenRouterChatMessage> buildMessages(
            String systemPrompt,
            String userPrompt,
            List<OpenRouterChatMessage> messageHistory
    ) {
        List<OpenRouterChatMessage> messages = new ArrayList<>();

        // Add system message
        if (systemPrompt != null && !systemPrompt.isBlank()) {
            messages.add(OpenRouterChatMessage.builder()
                    .role("system")
                    .content(systemPrompt)
                    .build());
        }

        // Add message history if provided
        if (messageHistory != null && !messageHistory.isEmpty()) {
            messages.addAll(messageHistory);
        }

        // Add current user message
        messages.add(OpenRouterChatMessage.builder()
                .role("user")
                .content(userPrompt)
                .build());

        return messages;
    }

    private OpenRouterResponse executeRequest(OpenRouterChatRequest request, OpenRouterModel model) {
        String modelInfo = model != null ? model.getModelId() : request.getModel();
        log.info("Executing OpenRouter chat completion for model: {}", modelInfo);
        log.debug("Request - MaxTokens: {}, Temperature: {}, ResponseFormat: {}",
                request.getMaxTokens(), request.getTemperature(),
                request.getResponseFormat() != null ? request.getResponseFormat().getType() : "none");

        try {
            OpenRouterChatResponse rawResponse = openRouterRestClient.post()
                    .uri("/chat/completions")
                    .body(request)
                    .retrieve()
                    .body(OpenRouterChatResponse.class);

            if (rawResponse == null) {
                throw TechnicalException.of(TechnicalException.Code.AI_SERVICE_ERROR)
                        .with("model", modelInfo)
                        .with("error", "Empty response from OpenRouter")
                        .build();
            }

            return mapToCustomResponse(rawResponse);

        } catch (RestClientException e) {
            log.error("OpenRouter API call failed for model {}: {}", modelInfo, e.getMessage(), e);
            throw TechnicalException.of(TechnicalException.Code.AI_SERVICE_ERROR)
                    .with("model", modelInfo)
                    .with("error", e.getMessage())
                    .cause(e)
                    .build();
        }
    }

    private OpenRouterResponse mapToCustomResponse(OpenRouterChatResponse rawResponse) {
        // Extract content
        String content = extractContent(rawResponse);

        // Extract finish reason
        String finishReason = null;
        if (rawResponse.getChoices() != null && !rawResponse.getChoices().isEmpty()) {
            finishReason = rawResponse.getChoices().get(0).getFinishReason();
        }

        // Extract token usage
        OpenRouterResponse.TokenUsage tokenUsage = null;
        if (rawResponse.getUsage() != null) {
            OpenRouterChatResponse.Usage usage = rawResponse.getUsage();
            tokenUsage = OpenRouterResponse.TokenUsage.builder()
                    .promptTokens(usage.getPromptTokens())
                    .completionTokens(usage.getCompletionTokens())
                    .totalTokens(usage.getTotalTokens())
                    .build();

            log.info("OpenRouter token usage for model {}: prompt={}, completion={}, total={}",
                    rawResponse.getModel(),
                    usage.getPromptTokens(),
                    usage.getCompletionTokens(),
                    usage.getTotalTokens());
        }

        OpenRouterResponse response = OpenRouterResponse.builder()
                .content(content)
                .model(rawResponse.getModel())
                .finishReason(finishReason)
                .tokenUsage(tokenUsage)
                .build();

        log.debug("OpenRouter response mapped: model={}, finishReason={}, contentLength={}",
                response.getModel(),
                response.getFinishReason(),
                content != null ? content.length() : 0);

        return response;
    }

    private String extractContent(OpenRouterChatResponse rawResponse) {
        if (rawResponse.getChoices() == null || rawResponse.getChoices().isEmpty()) {
            throw TechnicalException.of(TechnicalException.Code.AI_SERVICE_ERROR)
                    .with("error", "No choices in OpenRouter response")
                    .build();
        }

        OpenRouterChatResponse.Choice firstChoice = rawResponse.getChoices().get(0);
        if (firstChoice.getMessage() == null || firstChoice.getMessage().getContent() == null) {
            throw TechnicalException.of(TechnicalException.Code.AI_SERVICE_ERROR)
                    .with("error", "No message content in OpenRouter response")
                    .build();
        }

        return firstChoice.getMessage().getContent();
    }
}