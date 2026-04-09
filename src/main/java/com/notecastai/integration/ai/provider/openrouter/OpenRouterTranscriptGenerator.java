package com.notecastai.integration.ai.provider.openrouter;

import com.notecastai.integration.ai.NoteCastTranscriptGenerator;
import com.notecastai.integration.ai.dto.TranscriptAiResponse;
import com.notecastai.integration.ai.prompt.TranscriptPromptBuilder;
import com.notecastai.integration.ai.provider.openrouter.client.JsonSchemaBuilder;
import com.notecastai.integration.ai.provider.openrouter.client.OpenRouterClient;
import com.notecastai.integration.ai.provider.openrouter.dto.OpenRouterModel;
import com.notecastai.integration.ai.provider.openrouter.dto.OpenRouterResponse;
import com.notecastai.integration.ai.validator.TranscriptResponseValidator;
import com.notecastai.notecast.domain.NoteCastStyle;
import com.notecastai.notecast.domain.TranscriptSize;
import io.github.resilience4j.retry.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OpenRouterTranscriptGenerator implements NoteCastTranscriptGenerator {

    private final OpenRouterClient openRouterClient;
    private final TranscriptResponseValidator validator;
    private final Retry noteAiRetry;

    @Override
    public String generateTranscript(String noteContent, NoteCastStyle style, TranscriptSize size) {
        NoteCastStyle targetStyle = style != null ? style : NoteCastStyle.DEFAULT;
        TranscriptSize targetSize = size != null ? size : TranscriptSize.MEDIUM;

        log.info("Generating TTS transcript - style: {}, size: {} ({}-{} words)",
                targetStyle.getLabel(),
                targetSize.getLabel(),
                targetSize.getMinWords(),
                targetSize.getMaxWords());

        if (noteContent == null || noteContent.isBlank()) {
            throw new IllegalArgumentException("Note content cannot be empty");
        }

        TranscriptPromptBuilder promptBuilder = TranscriptPromptBuilder.builder()
                .noteContent(noteContent)
                .style(targetStyle)
                .size(targetSize)
                .build();

        TranscriptAiResponse response = Retry.decorateSupplier(noteAiRetry, () -> {
            log.info("Calling OpenRouter AI for transcript generation (attempt)");

            OpenRouterResponse aiResponse = openRouterClient.chatCompletion(
                    OpenRouterModel.GROK_FAST_1,
                    promptBuilder.getSystemPrompt(),
                    promptBuilder.getUserPrompt(),
                    JsonSchemaBuilder.buildTranscriptSchema()
            );

            return validator.validateTranscriptResponse(aiResponse.getContent(), targetSize);
        }).get();

        log.info("Transcript generation successful - style: {}, size: {}, wordCount: {}, duration: {}",
                targetStyle.getLabel(),
                targetSize.getLabel(),
                response.getWordCount(),
                response.getEstimatedDuration());

        return response.getTranscript();
    }
}
