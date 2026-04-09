package com.notecastai.integration.ai.provider.openrouter;

import com.notecastai.integration.ai.NoteAiEditor;
import com.notecastai.integration.ai.dto.FormatNoteAiResponse;
import com.notecastai.integration.ai.dto.NewNoteAiResponse;
import com.notecastai.integration.ai.prompt.FormatNoteKnowledgeBasePromptBuilder;
import com.notecastai.integration.ai.prompt.NewNotePromptBuilder;
import com.notecastai.integration.ai.provider.openrouter.client.JsonSchemaBuilder;
import com.notecastai.integration.ai.provider.openrouter.client.OpenRouterClient;
import com.notecastai.integration.ai.provider.openrouter.dto.OpenRouterModel;
import com.notecastai.integration.ai.provider.openrouter.dto.OpenRouterResponse;
import com.notecastai.integration.ai.validator.AiNoteResponseValidator;
import com.notecastai.note.service.command.CreateNoteCommand;
import com.notecastai.note.service.command.FormatNoteKnowledgeCommand;
import com.notecastai.note.domain.FormatType;
import com.notecastai.note.domain.NoteEntity;
import com.notecastai.note.infrastructure.repo.NoteRepository;
import com.notecastai.tag.domain.TagEntity;
import com.notecastai.tag.infrastructure.repo.TagRepository;
import com.notecastai.user.service.UserService;
import io.github.resilience4j.retry.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class OpenRouterNoteEditor implements NoteAiEditor {

    private final OpenRouterClient openRouterClient;
    private final TagRepository tagRepository;
    private final NoteRepository noteRepository;
    private final UserService userService;
    private final AiNoteResponseValidator noteValidator;
    private final Retry noteAiRetry;

    @Override
    public NewNoteAiResponse adjustNote(CreateNoteCommand request) {
        Long userId = userService.getCurrentUserId();

        List<String> availableTags = tagRepository.findAllByUserId(userId)
                .stream()
                .map(TagEntity::getName)
                .collect(Collectors.toList());

        NewNotePromptBuilder promptBuilder = NewNotePromptBuilder.builder()
                .title(request.getTitle())
                .knowledgeBase(request.getKnowledgeBase())
                .formatType(request.getFormatType() != null ? request.getFormatType() : FormatType.DEFAULT)
                .availableTags(availableTags)
                .userInstructions(request.getInstructions())
                .build();

        return Retry.decorateSupplier(noteAiRetry, () -> {
            log.info("Calling OpenRouter AI for new note adjustment (attempt)");

            OpenRouterResponse response = openRouterClient.chatCompletion(
                    OpenRouterModel.GROK_FAST_1,
                    promptBuilder.getSystemPrompt(),
                    promptBuilder.getUserPrompt(),
                    JsonSchemaBuilder.buildNewNoteSchema()
            );

            NewNoteAiResponse aiResponse = noteValidator.validateNewNoteResponse(response.getContent(), userId);

            List<Long> validTagIds = noteValidator.filterAndMapTagsToIds(aiResponse.getProposedTags(), userId);
            aiResponse.setTagIds(validTagIds);

            log.info("AI adjustment successful: title={}, validTags={}/{}, actions={}",
                    aiResponse.getAdjustedTitle(),
                    validTagIds.size(),
                    aiResponse.getProposedTags().size(),
                    aiResponse.getProposedAiActions().size());

            return aiResponse;
        }).get();
    }

    @Override
    public FormatNoteAiResponse formatNoteKnowledgeBase(Long noteId, FormatNoteKnowledgeCommand request) {
        NoteEntity note = noteRepository.getOrThrow(noteId);
        Long userId = note.getUser().getId();

        List<String> availableTags = tagRepository.findAllByUserId(userId)
                .stream()
                .map(TagEntity::getName)
                .collect(Collectors.toList());

        FormatNoteKnowledgeBasePromptBuilder promptBuilder = FormatNoteKnowledgeBasePromptBuilder.builder()
                .currentTitle(note.getTitle())
                .currentKnowledgeBase(note.getKnowledgeBase())
                .formatType(request.getFormatType() != null ? request.getFormatType() : FormatType.DEFAULT)
                .availableTags(availableTags)
                .userInstructions(request.getInstructions())
                .build();

        return Retry.decorateSupplier(noteAiRetry, () -> {
            log.info("Calling OpenRouter AI for note formatting (attempt)");

            OpenRouterResponse response = openRouterClient.chatCompletion(
                    OpenRouterModel.GROK_FAST_1,
                    promptBuilder.getSystemPrompt(),
                    promptBuilder.getUserPrompt(),
                    JsonSchemaBuilder.buildFormatNoteSchema()
            );

            FormatNoteAiResponse aiResponse = noteValidator.validateFormatNoteResponse(response.getContent(), userId);

            List<Long> validTagIds = noteValidator.filterAndMapTagsToIds(aiResponse.getProposedTags(), userId);
            aiResponse.setTagIds(validTagIds);

            log.info("AI formatting successful: title={}, validTags={}/{}, actions={}",
                    aiResponse.getAdjustedTitle(),
                    validTagIds.size(),
                    aiResponse.getProposedTags().size(),
                    aiResponse.getProposedAiActions().size());

            return aiResponse;
        }).get();
    }
}
