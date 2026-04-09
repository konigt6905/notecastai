package com.notecastai.tag.service.impl;

import com.notecastai.common.exception.BusinessException;
import com.notecastai.tag.api.mapper.TagMapper;
import com.notecastai.tag.api.dto.TagDTO;
import com.notecastai.tag.service.command.CreateTagCommand;
import com.notecastai.tag.service.command.UpdateTagCommand;
import com.notecastai.tag.domain.TagEntity;
import com.notecastai.tag.infrastructure.repo.TagRepository;
import com.notecastai.tag.service.TagService;
import com.notecastai.user.domain.UserEntity;
import com.notecastai.user.service.UserService;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.notecastai.common.exception.BusinessException.BusinessCode.*;

@Service
@RequiredArgsConstructor
public class TagServiceImpl implements TagService {

    public static final int MAX_TAGS_PER_USER = 50;

    private final TagRepository tagRepository;
    private final TagMapper mapper;
    private final UserService userService;

    @Override
    @Transactional
    public TagDTO create(CreateTagCommand command) {
        Long userId = userService.getCurrentUserId();
        String normalized = command.getName().trim();

        if (normalized.isEmpty()) {
            throw BusinessException.of(TAG_MUST_NOT_BE_BLANK);
        }
        if (tagRepository.existsByUserAndNameIgnoreCase(userId, normalized)) {
            throw BusinessException.of(TAG_ALREADY_EXIST);
        }

        long current = tagRepository.countByUserId(userId);

        if (current >= MAX_TAGS_PER_USER) {
            throw BusinessException.of(LIMIT_OF_TAGS_REACHED.append(" Limit: " + MAX_TAGS_PER_USER));
        }

        UserEntity user = userService.getCurrentUserReference();

        TagEntity entity = TagEntity.builder()
                .user(user)
                .name(normalized)
                .build();

        TagEntity saved = tagRepository.save(entity);
        return mapper.toDto(saved);
    }

    @Override
    @Transactional
    public TagDTO update(Long id, UpdateTagCommand command) {
        Long userId = userService.getCurrentUserId();
        String normalized = command.getName().trim();

        if (normalized.isEmpty()) {
            throw BusinessException.of(TAG_MUST_NOT_BE_BLANK);
        }

        TagEntity entity = tagRepository.findByIdAndUserOrThrow(id, userId);

        if (!entity.getName().equalsIgnoreCase(normalized) && tagRepository.existsByUserAndNameIgnoreCase(userId, normalized)) {
            throw BusinessException.of(TAG_ALREADY_EXIST);
        }
        entity.setName(normalized);
        return mapper.toDto(tagRepository.save(entity));
    }

    @Override
    @Transactional
    public void deleteForCurrentUser(Long id) {
        Long userId = userService.getCurrentUserId();
        TagEntity entity = tagRepository.findByIdAndUserOrThrow(id, userId);
        tagRepository.delete(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public TagDTO getForCurrentUser(Long id) {
        Long userId = userService.getCurrentUserId();
        TagEntity entity = tagRepository.findByIdAndUserOrThrow(id, userId);
        return mapper.toDto(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TagDTO> findAllByCurrentUser() {
        Long userId = userService.getCurrentUserId();
        return mapper.toDto(tagRepository.findAllByUserId(userId));
    }

}
