package com.notecastai.tag.service.impl;

import com.notecastai.tag.domain.DefaultTag;
import com.notecastai.tag.domain.TagEntity;
import com.notecastai.tag.infrastructure.repo.TagRepository;
import com.notecastai.user.domain.UserEntity;
import com.notecastai.user.infrastructure.repo.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Seeds the default tags for a new user.
 *
 * Kept out of TagServiceImpl to avoid a UserService <-> TagService cycle
 * (TagService needs the current user, UserService needs to seed tags on signup).
 * This class only touches repositories so there's no cycle and no need for @Lazy.
 */
@Component
@RequiredArgsConstructor
public class DefaultTagProvisioner {

    private final TagRepository tagRepository;
    private final UserRepository userRepository;

    @Transactional
    public void provisionDefaultTags(Long userId) {
        UserEntity user = userRepository.getById(userId);

        for (String tagName : DefaultTag.getAllTagNames()) {
            tagRepository.save(TagEntity.builder()
                    .user(user)
                    .name(tagName)
                    .build());
        }
    }
}
