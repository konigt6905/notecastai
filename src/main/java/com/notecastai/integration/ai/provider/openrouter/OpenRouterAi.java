package com.notecastai.integration.ai.provider.openrouter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.notecastai.common.exeption.BusinessException;
import com.notecastai.common.exeption.TechnicalException;
import com.notecastai.common.util.SecurityUtils;
import com.notecastai.integration.ai.dto.GameNoteAiRequest;
import com.notecastai.integration.ai.dto.GameNoteAiResponse;
import com.notecastai.integration.ai.NoteAiChat;
import com.notecastai.integration.ai.NoteAiEditor;
import com.notecastai.integration.ai.NoteCastTranscriptGenerator;
import com.notecastai.integration.ai.provider.openrouter.dto.FormatNoteAiResponse;
import com.notecastai.integration.ai.provider.openrouter.dto.NewNoteAiResponse;
import com.notecastai.integration.ai.prompt.*;
import com.notecastai.integration.ai.provider.openrouter.client.JsonSchemaBuilder;
import com.notecastai.integration.ai.provider.openrouter.client.OpenRouterClient;
import com.notecastai.integration.ai.provider.openrouter.dto.*;
import com.notecastai.integration.ai.validator.AiNoteResponseValidator;
import com.notecastai.integration.ai.validator.GameNoteResponseValidator;
import com.notecastai.integration.ai.validator.TranscriptResponseValidator;
import com.notecastai.note.api.dto.*;
import com.notecastai.note.domain.FormateType;
import com.notecastai.note.domain.NoteEntity;
import com.notecastai.note.infrastructure.repo.NoteRepository;
import com.notecastai.notecast.domain.NoteCastStyle;
import com.notecastai.notecast.domain.TranscriptSize;
import com.notecastai.tag.domain.TagEntity;
import com.notecastai.tag.repo.TagRepository;
import com.notecastai.user.infrastructure.repo.UserRepository;
import io.github.resilience4j.retry.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


@Slf4j
@Component
@RequiredArgsConstructor
public class OpenRouterAi implements NoteAiEditor, NoteCastTranscriptGenerator, NoteAiChat {

    private final OpenRouterClient openRouterClient;
    private final TagRepository tagRepository;
    private final NoteRepository noteRepository;
    private final UserRepository userRepository;
    private final AiNoteResponseValidator noteValidator;
    private final TranscriptResponseValidator validator;
    private final GameNoteResponseValidator gameNoteValidator;
    private final Retry noteAiRetry;
    private final ObjectMapper objectMapper;

    @Override
    public NewNoteAiResponse adjustNote(CreateNoteRequest request) {
        Long userId = userRepository.getByClerkUserId(SecurityUtils.getCurrentClerkUserIdOrThrow()).getId();

        // Get user's available tags
        List<String> availableTags = tagRepository.findAllByUserId(userId)
                .stream()
                .map(TagEntity::getName)
                .collect(Collectors.toList());

        // Build prompts
        NewNotePromptBuilder promptBuilder = NewNotePromptBuilder.builder()
                .title(request.getTitle())
                .knowledgeBase(request.getKnowledgeBase())
                .formateType(request.getFormateType() != null ? request.getFormateType() : FormateType.DEFAULT)
                .availableTags(availableTags)
                .userInstructions(request.getInstructions())
                .build();

        // Execute with retry
        return Retry.decorateSupplier(noteAiRetry, () -> {
            log.info("Calling OpenRouter AI for new note adjustment (attempt)");

            OpenRouterResponse response = openRouterClient.chatCompletion(
                    OpenRouterModel.GROK_FAST_1,
                    promptBuilder.getSystemPrompt(),
                    promptBuilder.getUserPrompt(),
                    JsonSchemaBuilder.buildNewNoteSchema()
            );

            // Validate and parse response
            NewNoteAiResponse aiResponse = noteValidator.validateNewNoteResponse(response.getContent(), userId);

            // Filter and map valid tags
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
    public FormatNoteAiResponse formatNoteKnowledgeBase(Long noteId, NoteKnowledgeFormatRequest request) {
        NoteEntity note = noteRepository.getOrThrow(noteId);
        Long userId = note.getUser().getId();

        List<String> availableTags = tagRepository.findAllByUserId(userId)
                .stream()
                .map(TagEntity::getName)
                .collect(Collectors.toList());

        FormatNoteKnowledgeBasePromptBuilder promptBuilder = FormatNoteKnowledgeBasePromptBuilder.builder()
                .currentTitle(note.getTitle())
                .currentKnowledgeBase(note.getKnowledgeBase())
                .formateType(request.getFormateType() != null ? request.getFormateType() : FormateType.DEFAULT)
                .availableTags(availableTags)
                .userInstructions(request.getInstructions())
                .build();

        // Execute with retry
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

    @Override
    public String generateTranscript(String noteContent, NoteCastStyle style, TranscriptSize size) {
        // Default values
        NoteCastStyle targetStyle = style != null ? style : NoteCastStyle.DEFAULT;
        TranscriptSize targetSize = size != null ? size : TranscriptSize.MEDIUM;

        log.info("Generating TTS transcript - style: {}, size: {} ({}-{} words)",
                targetStyle.getLabel(),
                targetSize.getLabel(),
                targetSize.getMinWords(),
                targetSize.getMaxWords());

        // Validate input
        if (noteContent == null || noteContent.isBlank()) {
            throw new IllegalArgumentException("Note content cannot be empty");
        }

        // Build prompts
        TranscriptPromptBuilder promptBuilder = TranscriptPromptBuilder.builder()
                .noteContent(noteContent)
                .style(targetStyle)
                .size(targetSize)
                .build();

        // Execute with retry
        TranscriptAiResponse response = Retry.decorateSupplier(noteAiRetry, () -> {
            log.info("Calling OpenRouter AI for transcript generation (attempt)");

            OpenRouterResponse aiResponse = openRouterClient.chatCompletion(
                    OpenRouterModel.GROK_FAST_1,
                    promptBuilder.getSystemPrompt(),
                    promptBuilder.getUserPrompt(),
                    JsonSchemaBuilder.buildTranscriptSchema()
            );

            // Validate and parse response with size context
            return validator.validateTranscriptResponse(aiResponse.getContent(), targetSize);
        }).get();

        log.info("Transcript generation successful - style: {}, size: {}, wordCount: {}, duration: {}",
                targetStyle.getLabel(),
                targetSize.getLabel(),
                response.getWordCount(),
                response.getEstimatedDuration());

        return response.getTranscript();
    }

    @Override
    public NoteQuestionResponse askQuestion(Long noteId, NoteQuestionRequest request) {
        // Validate input
        if (request.getQuestion() == null || request.getQuestion().isBlank()) {
            throw new IllegalArgumentException("Question cannot be empty");
        }

        // Get note
        NoteEntity note = noteRepository.getOrThrow(noteId);

        log.info("Processing question for note ID: {}, question length: {}, history messages: {}",
                noteId, request.getQuestion().length(),
                request.getHistory() != null ? request.getHistory().size() : 0);

        // Build prompts
        NoteChatPromptBuilder promptBuilder = NoteChatPromptBuilder.builder()
                .note(note)
                .userQuestion(request.getQuestion())
                .build();

        // Convert chat history to OpenRouter format
        List<OpenRouterChatMessage> messageHistory = convertChatHistory(request.getHistory());

        // Execute with retry
        String responseText = Retry.decorateSupplier(noteAiRetry, () -> {
            log.info("Calling OpenRouter AI for note chat (attempt)");

            OpenRouterResponse response = openRouterClient.chatCompletionMarkdown(
                    OpenRouterModel.GROK_FAST_1,
                    promptBuilder.getSystemPrompt(),
                    promptBuilder.getUserPrompt(),
                    messageHistory
            );

            // Validate response
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

    private List<OpenRouterChatMessage> convertChatHistory(List<ChatMessage> history) {
        if (history == null || history.isEmpty()) {
            return List.of();
        }

        List<OpenRouterChatMessage> converted = new ArrayList<>();

        for (ChatMessage msg : history) {
            // Validate message
            if (msg.getRole() == null || msg.getRole().isBlank()) {
                log.warn("Skipping message with null/empty role");
                continue;
            }
            if (msg.getContent() == null || msg.getContent().isBlank()) {
                log.warn("Skipping message with null/empty content");
                continue;
            }

            // Validate role
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

    @Override
    public GameNoteAiResponse generateGameQuestions(GameNoteAiRequest request) {
        log.info("Generating game questions: type={}, difficulty={}, count={}",
                request.getQuestionType(), request.getDifficulty(), request.getNumberOfQuestions());

        // Select appropriate prompt builder based on question type
        String systemPrompt;
        String userPrompt;
        OpenRouterJsonSchema jsonSchema;

        switch (request.getQuestionType()) {
            case FLASHCARD -> {
                GameNoteFlashcardPromptBuilder builder = GameNoteFlashcardPromptBuilder.builder()
                        .noteTitle(request.getNoteTitle())
                        .noteContent(request.getNoteContent())
                        .numberOfQuestions(request.getNumberOfQuestions())
                        .questionLength(request.getQuestionLength())
                        .answerLength(request.getAnswerLength())
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
                        .questionLength(request.getQuestionLength())
                        .answerLength(request.getAnswerLength())
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
                        .questionLength(request.getQuestionLength())
                        .answerLength(request.getAnswerLength())
                        .difficulty(request.getDifficulty())
                        .customInstructions(request.getCustomInstructions())
                        .build();
                systemPrompt = builder.getSystemPrompt();
                userPrompt = builder.getUserPrompt();
                jsonSchema = JsonSchemaBuilder.buildGameNoteTrueFalseSchema();
            }
            case OPEN_QUESTION -> {
                GameNoteOpenQuestionPromptBuilder builder = GameNoteOpenQuestionPromptBuilder.builder()
                        .noteTitle(request.getNoteTitle())
                        .noteContent(request.getNoteContent())
                        .numberOfQuestions(request.getNumberOfQuestions())
                        .questionLength(request.getQuestionLength())
                        .answerLength(request.getAnswerLength())
                        .difficulty(request.getDifficulty())
                        .customInstructions(request.getCustomInstructions())
                        .build();
                systemPrompt = builder.getSystemPrompt();
                userPrompt = builder.getUserPrompt();
                jsonSchema = JsonSchemaBuilder.buildGameNoteOpenQuestionSchema();
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

            // Validate and parse with validator
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
