package com.notecastai.user.service.impl;

import com.notecastai.common.exeption.BusinessException;
import com.notecastai.tag.service.TagService;
import com.notecastai.user.api.UserMapper;
import com.notecastai.user.api.dto.UserCreateRequest;
import com.notecastai.user.api.dto.UserDTO;
import com.notecastai.user.domain.UserEntity;
import com.notecastai.user.infrastructure.repo.UserRepository;
import com.notecastai.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.notecastai.common.exeption.BusinessException.BusinessCode.CONFLICT;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper mapper;
    private final TagService tagService;

    @Override
    @Transactional
    public UserDTO create(UserCreateRequest request) {
        String clerkId = request.getClerkUserId().trim();

        if (userRepository.existsByClerkUserId(clerkId)) {
            throw BusinessException.of(CONFLICT.append(" user with clerkUserId already exists").append(" clerkUserId: %s".formatted(clerkId)));
        }

        UserEntity entity = UserEntity.builder()
                .clerkUserId(clerkId)
                .build();

        UserEntity saved = userRepository.save(entity);

        // Initialize default tags
        tagService.createDefaultTagsForUser(saved.getId());

        return mapper.toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public UserDTO getById(Long id) {
        return mapper.toDto(userRepository.getOrThrow(id));
    }

    @Override
    @Transactional(readOnly = true)
    public UserDTO getByClerkUserId(String clerkUserId) {
        return mapper.toDto(userRepository.getByClerkUserId(clerkUserId));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserDTO> findAll(Pageable pageable) {
        return userRepository.findAllPaged(pageable).map(mapper::toDto);
    }

    @Override
    @Transactional
    public void deactivate(Long id) {
        UserEntity e = userRepository.getOrThrow(id);
        e.deactivate();
        userRepository.save(e);
    }
}
