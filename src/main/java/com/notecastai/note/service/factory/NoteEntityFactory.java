package com.notecastai.note.service.factory;

import com.notecastai.integration.ai.dto.NewNoteAiResponse;
import com.notecastai.integration.ai.prompt.AiAction;
import com.notecastai.note.domain.NoteEntity;
import com.notecastai.note.service.command.CreateNoteCommand;
import com.notecastai.tag.domain.TagEntity;
import com.notecastai.tag.infrastructure.repo.TagRepository;
import com.notecastai.user.domain.UserEntity;
import com.notecastai.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Builds NoteEntity from commands + AI responses.
 * Here so tag resolution and AI action mapping don't get copy-pasted
 * around NoteServiceImpl.
 */
@Component
@RequiredArgsConstructor
public class NoteEntityFactory {

    private final UserService userService;
    private final TagRepository tagRepository;

    public NoteEntity createFromAiResponse(CreateNoteCommand command, NewNoteAiResponse aiResponse) {
        UserEntity user = userService.getCurrentUserReference();

        NoteEntity entity = NoteEntity.builder()
                .user(user)
                .title(resolveTitle(command, aiResponse))
                .knowledgeBase(command.getKnowledgeBase())
                .formattedNote(aiResponse.getFormattedNote())
                .tags(resolveTags(command, aiResponse))
                .proposedAiActions(mapAiActions(aiResponse.getProposedAiActions()))
                .build();

        if (command.getFormatType() != null) {
            entity.setCurrentFormat(command.getFormatType());
        }
        if (command.getType() != null) {
            entity.setType(command.getType());
        }

        return entity;
    }

    public List<NoteEntity.AiAction> mapAiActions(List<AiAction> actions) {
        return actions.stream()
                .map(action -> NoteEntity.AiAction.builder()
                        .name(action.getName())
                        .prompt(action.getPrompt())
                        .build())
                .collect(Collectors.toList());
    }

    public Set<TagEntity> resolveTagsByIds(List<Long> tagIds) {
        if (tagIds == null || tagIds.isEmpty()) return Set.of();
        Long currentUserId = userService.getCurrentUserId();
        return tagIds.stream()
                .map(tagId -> tagRepository.findByIdAndUserOrThrow(tagId, currentUserId))
                .collect(Collectors.toSet());
    }

    private String resolveTitle(CreateNoteCommand command, NewNoteAiResponse aiResponse) {
        if (command.getTitle() != null && !command.getTitle().isBlank() && !command.isAdjustTitleWithAi()) {
            return command.getTitle();
        }
        return aiResponse.getAdjustedTitle();
    }

    private Set<TagEntity> resolveTags(CreateNoteCommand command, NewNoteAiResponse aiResponse) {
        if (command.isAdjustTagsWithAi() || command.getTagIds() == null || command.getTagIds().isEmpty()) {
            return resolveTagsByIds(aiResponse.getTagIds());
        }
        return resolveTagsByIds(command.getTagIds());
    }
}
