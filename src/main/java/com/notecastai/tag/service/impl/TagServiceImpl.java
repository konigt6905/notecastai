package com.notecastai.tag.service.impl;

import com.notecastai.common.exeption.BusinessException;
import com.notecastai.tag.api.mapper.TagMapper;
import com.notecastai.tag.api.dto.TagCreateRequest;
import com.notecastai.tag.api.dto.TagDTO;
import com.notecastai.tag.api.dto.TagUpdateRequest;
import com.notecastai.tag.domain.DefaultTag;
import com.notecastai.tag.domain.TagEntity;
import com.notecastai.tag.repo.TagRepository;
import com.notecastai.tag.service.TagService;
import com.notecastai.user.domain.UserEntity;
import com.notecastai.user.infrastructure.repo.UserRepository;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import static com.notecastai.common.exeption.BusinessException.BusinessCode.*;

@Service
@RequiredArgsConstructor
public class TagServiceImpl implements TagService {

    public static final int MAX_TAGS_PER_USER = 50;

    private final TagRepository tagRepository;
    private final TagMapper mapper;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public TagDTO create(TagCreateRequest request) {
        Long userId = request.getUserId();
        String normalized = request.getName().trim();

        if (normalized.isEmpty()) {
            throw BusinessException.of(TAG_MUST_NOT_BE_BLANK);
        }
        if (tagRepository.existsByUserAndNameIgnoreCase(userId, normalized)) {
            throw BusinessException.of(TAG_ALREADY_EXIST);
        }

        long current = tagRepository.countByUserId(userId);

        if (current >= MAX_TAGS_PER_USER) {
            throw BusinessException.of(LIMIT_OF_TAGS_REECHOED.append(" Limit: " + MAX_TAGS_PER_USER));
        }

        UserEntity user = userRepository.getOrThrow(userId);

        TagEntity entity = TagEntity.builder()
                .user(user)
                .name(normalized)
                .build();

        TagEntity saved = tagRepository.save(entity);
        return mapper.toDto(saved);
    }

    @Override
    @Transactional
    public TagDTO update(Long id, TagUpdateRequest request) {
        Long userId = request.getUserId();
        String normalized = request.getName().trim();

        if (normalized.isEmpty()) {
            throw BusinessException.of(TAG_MUST_NOT_BE_BLANK);
        }

        TagEntity entity = tagRepository.getById(id);

        if (!entity.getName().equalsIgnoreCase(normalized) && tagRepository.existsByUserAndNameIgnoreCase(userId, normalized)) {
            throw BusinessException.of(TAG_ALREADY_EXIST);
        }
        entity.setName(normalized);
        return mapper.toDto(tagRepository.save(entity));
    }

    @Override
    @Transactional
    public void deleteForUser(Long id, Long userId) {
        TagEntity entity = tagRepository.findByIdAndUserOrThrow(id, userId);
        tagRepository.delete(entity);
    }

    @Override
    @Transactional
    public TagDTO getForUser(Long id, Long userId) {
        TagEntity entity = tagRepository.findByIdAndUserOrThrow(id, userId);
        return mapper.toDto(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TagDTO> findAllByUser(Long userId) {
        return mapper.toDto(tagRepository.findAllByUserId(userId));
    }

    @Transactional
    public List<TagDTO> createDefaultTagsForUser(Long userId) {
        UserEntity user = userRepository.getOrThrow(userId);

        List<TagEntity> tags = new ArrayList<>();
        for (String tagName : DefaultTag.getAllTagNames()) {
            TagEntity e = TagEntity.builder()
                    .user(user)
                    .name(tagName)
                    .build();
            tags.add(tagRepository.save(e));
        }
        return mapper.toDto(tagRepository.findAllByUserId(userId));
    }
}
