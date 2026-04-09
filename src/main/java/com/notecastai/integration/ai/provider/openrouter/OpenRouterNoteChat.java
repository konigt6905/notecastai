package com.notecastai.integration.ai.provider.openrouter;

import com.notecastai.common.exception.TechnicalException;
import com.notecastai.integration.ai.NoteAiChat;
import com.notecastai.integration.ai.prompt.NoteChatPromptBuilder;
import com.notecastai.integration.ai.provider.openrouter.client.OpenRouterClient;
import com.notecastai.integration.ai.provider.openrouter.dto.OpenRouterChatMessage;
import com.notecastai.integration.ai.provider.openrouter.dto.OpenRouterModel;
import com.notecastai.integration.ai.provider.openrouter.dto.OpenRouterResponse;
import com.notecastai.note.api.dto.NoteQuestionResponse;
import com.notecastai.note.service.command.AskNoteQuestionCommand;
import com.notecastai.note.domain.NoteEntity;
import com.notecastai.note.infrastructure.repo.NoteRepository;
import io.github.resilience4j.retry.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class OpenRouterNoteChat implements NoteAiChat {

    private final OpenRouterClient openRouterClient;
    private final NoteRepository noteRepository;
    private final Retry noteAiRetry;

    @Override
    public NoteQuestionResponse askQuestion(Long noteId, AskNoteQuestionCommand command) {
        if (command.getQuestion() == null || command.getQuestion().isBlank()) {
            throw new IllegalArgumentException("Question cannot be empty");
        }

        NoteEntity note = noteRepository.getOrThrow(noteId);

        log.info("Processing question for note ID: {}, question length: {}, history messages: {}",
                noteId, command.getQuestion().length(),
                command.getHistory() != null ? command.getHistory().size() : 0);

        NoteChatPromptBuilder promptBuilder = NoteChatPromptBuilder.builder()
                .note(note)
                .userQuestion(command.getQuestion())
                .build();

        List<OpenRouterChatMessage> messageHistory = convertChatHistory(command.getHistory());

        String responseText = Retry.decorateSupplier(noteAiRetry, () -> {
            log.info("Calling OpenRouter AI for note chat (attempt)");

            OpenRouterResponse response = openRouterClient.chatCompletionMarkdown(
                    OpenRouterModel.GROK_FAST_1,
                    promptBuilder.getSystemPrompt(),
                    promptBuilder.getUserPrompt(),
                    messageHistory
            );

            if (response.getContent() == null || response.getContent().isBlank()) {
                throw TechnicalException.of(TechnicalException.Code.AI_SERVICE_ERROR)
                        .with("noteId", noteId)
                        .with("error", "Empty response from AI")
                        .build();
            }

            return response.getContent();
        }).get();

        log.info("Note chat response generated successfully for note ID: {}, response length: {} chars",
                noteId, responseText.length());

        return NoteQuestionResponse.builder()
                .responseText(responseText)
                .build();
    }

    private List<OpenRouterChatMessage> convertChatHistory(List<AskNoteQuestionCommand.ChatMessageCommand> history) {
        if (history == null || history.isEmpty()) {
            return List.of();
        }

        List<OpenRouterChatMessage> converted = new ArrayList<>();

        for (AskNoteQuestionCommand.ChatMessageCommand msg : history) {
            if (msg.getRole() == null || msg.getRole().isBlank()) {
                log.warn("Skipping message with null/empty role");
                continue;
            }
            if (msg.getContent() == null || msg.getContent().isBlank()) {
                log.warn("Skipping message with null/empty content");
                continue;
            }

            String role = msg.getRole().toLowerCase();
            if (!role.equals("user") && !role.equals("assistant")) {
                log.warn("Invalid role '{}', skipping message", msg.getRole());
                continue;
            }

            converted.add(OpenRouterChatMessage.builder()
                    .role(role)
                    .content(msg.getContent())
                    .build());
        }

        log.debug("Converted {} history messages to OpenRouter format", converted.size());
        return converted;
    }
}
