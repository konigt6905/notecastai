package com.notecastai.notecast.service.factory;

import com.notecastai.common.exception.BusinessException;
import com.notecastai.common.util.OwnershipVerifier;
import com.notecastai.config.TtsVoiceProperties;
import com.notecastai.note.domain.NoteEntity;
import com.notecastai.note.infrastructure.repo.NoteRepository;
import com.notecastai.notecast.domain.NoteCastEntity;
import com.notecastai.notecast.domain.NoteCastStatus;
import com.notecastai.notecast.domain.TtsVoice;
import com.notecastai.notecast.domain.TtsVoiceProvider;
import com.notecastai.notecast.service.command.CreateNoteCastCommand;
import com.notecastai.tag.domain.TagEntity;
import com.notecastai.tag.infrastructure.repo.TagRepository;
import com.notecastai.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

import static com.notecastai.common.exception.BusinessException.BusinessCode.INVALID_REQUEST;

/**
 * Builds {@link NoteCastEntity} instances, resolving the source note,
 * voice provider, and tags from cross-module repositories.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NoteCastEntityFactory {

    private final NoteRepository noteRepository;
    private final TagRepository tagRepository;
    private final UserService userService;
    private final TtsVoiceProperties ttsVoiceProperties;

    /**
     * Creates a NoteCastEntity from a command. Validates the source note,
     * resolves voice and tags.
     *
     * @return a pair of the entity and source note (needed for event publishing)
     */
    public NoteCastWithSource createFromCommand(CreateNoteCastCommand command) {
        NoteEntity note = noteRepository.getOrThrow(command.getNoteId());
        OwnershipVerifier.verify(note.getUser().getId());

        if (note.getFormattedNote() == null || note.getFormattedNote().isBlank()) {
            throw BusinessException.of(INVALID_REQUEST.append(" Note has no formatted content"));
        }

        TtsVoice resolvedVoice = resolveVoice(command.getVoice(), ttsVoiceProperties.getVoiceProvider());

        String title = command.getTitle();
        if (title == null || title.isBlank()) {
            title = note.getTitle();
        }

        NoteCastEntity entity = NoteCastEntity.builder()
                .note(note)
                .title(title)
                .status(NoteCastStatus.WAITING_FOR_TRANSCRIPT)
                .style(command.getStyle())
                .size(command.getSize())
                .voice(resolvedVoice)
                .build();

        resolveTags(entity, command, note);

        return new NoteCastWithSource(entity, note);
    }

    private void resolveTags(NoteCastEntity entity, CreateNoteCastCommand command, NoteEntity note) {
        if (Boolean.TRUE.equals(command.getTakeTagsFromNote())) {
            log.info("Copying tags from note {} to notecast", note.getId());
            entity.setTags(new HashSet<>(note.getTags()));
        } else if (command.getTagIds() != null && !command.getTagIds().isEmpty()) {
            Long userId = userService.getCurrentUserId();
            Set<TagEntity> tags = tagRepository.resolveAndValidateForUser(command.getTagIds(), userId);
            entity.setTags(tags);
        }
    }

    private TtsVoice resolveVoice(TtsVoice requested, TtsVoiceProvider provider) {
        if (requested != null && requested.supportsProvider(provider)) {
            return requested;
        }
        return getUserDefaultVoice(provider);
    }

    private TtsVoice getUserDefaultVoice(TtsVoiceProvider provider) {
        TtsVoice userVoice = userService.getCurrentUser().getDefaultVoice();
        if (userVoice != null && userVoice.supportsProvider(provider)) {
            return userVoice;
        }
        return TtsVoice.getDefault(provider);
    }

    public record NoteCastWithSource(NoteCastEntity entity, NoteEntity sourceNote) {}
}
