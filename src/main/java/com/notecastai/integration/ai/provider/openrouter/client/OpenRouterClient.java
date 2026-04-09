package com.notecastai.integration.ai.provider.openrouter.client;

import com.notecastai.common.exception.TechnicalException;
import com.notecastai.integration.ai.provider.openrouter.dto.*;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class OpenRouterClient {

    private final RestClient openRouterRestClient;
    private final RateLimiter globalAiRateLimiter;

    public OpenRouterClient(RestClient openRouterRestClient,
                            @Qualifier("globalAiRateLimiter") RateLimiter globalAiRateLimiter) {
        this.openRouterRestClient = openRouterRestClient;
        this.globalAiRateLimiter = globalAiRateLimiter;
    }

    public OpenRouterResponse chatCompletion(
            OpenRouterModel model,
            String systemPrompt,
            String userPrompt,
            OpenRouterJsonSchema jsonSchema
    ) {
        return chatCompletion(model, systemPrompt, userPrompt, null, jsonSchema);
    }

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

        return executeRequest(request, model);
    }

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

        if (jsonSchema != null) {
            requestBuilder.responseFormat(OpenRouterResponseFormat.builder()
                    .type("json_schema")
                    .jsonSchema(jsonSchema)
                    .build());
        } else if (model.isSupportsJsonMode()) {
            requestBuilder.responseFormat(OpenRouterResponseFormat.builder()
                    .type("json_object")
                    .build());
        }

        return executeRequest(requestBuilder.build(), model);
    }

    public OpenRouterResponse chatCompletion(OpenRouterChatRequest request) {
        return executeRequest(request, null);
    }

    private List<OpenRouterChatMessage> buildMessages(
            String systemPrompt,
            String userPrompt,
            List<OpenRouterChatMessage> messageHistory
    ) {
        List<OpenRouterChatMessage> messages = new ArrayList<>();

        if (systemPrompt != null && !systemPrompt.isBlank()) {
            messages.add(OpenRouterChatMessage.builder()
                    .role("system")
                    .content(systemPrompt)
                    .build());
        }

        if (messageHistory != null && !messageHistory.isEmpty()) {
            messages.addAll(messageHistory);
        }

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
            RateLimiter.waitForPermission(globalAiRateLimiter);
        } catch (RequestNotPermitted e) {
            log.warn("Global AI rate limit exceeded for model: {}", modelInfo);
            throw TechnicalException.of(TechnicalException.Code.AI_SERVICE_ERROR)
                    .with("model", modelInfo)
                    .with("error", "AI service rate limit exceeded. Please try again shortly.")
                    .build();
        }

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
        String content = extractContent(rawResponse);

        String finishReason = null;
        if (rawResponse.getChoices() != null && !rawResponse.getChoices().isEmpty()) {
            finishReason = rawResponse.getChoices().get(0).getFinishReason();
        }

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