package com.notecastai.user.service.impl;

import com.notecastai.common.exeption.BusinessException;
import com.notecastai.tag.service.TagService;
import com.notecastai.user.api.dto.UserCreateRequest;
import com.notecastai.user.api.dto.UserDTO;
import com.notecastai.user.api.dto.UserUpdateRequest;
import com.notecastai.user.api.mapper.UserMapper;
import com.notecastai.user.domain.UserEntity;
import com.notecastai.user.infrastructure.repo.UserRepository;
import com.notecastai.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static com.notecastai.common.exeption.BusinessException.BusinessCode.CONFLICT;

@Service
@RequiredArgsConstructor
@Slf4j
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
    @Transactional
    public UserDTO update(Long id, UserUpdateRequest request) {
        UserEntity entity = userRepository.getOrThrow(id);

        if (request.getDefaultFormate() != null) {
            entity.setDefaultFormate(request.getDefaultFormate());
        }

        if (request.getDefaultVoice() != null) {
            entity.setDefaultVoice(request.getDefaultVoice());
        }

        if (request.getPreferredLanguage() != null) {
            entity.setPreferredLanguage(request.getPreferredLanguage());
        }

        return mapper.toDto(userRepository.save(entity));
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
    public Optional<UserDTO> findByClerkUserId(String clerkUserId) {
        return userRepository.findByClerkUserId(clerkUserId).map(mapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserDTO> findAll(Pageable pageable) {
        return userRepository.findAllPaged(pageable).map(mapper::toDto);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        UserEntity e = userRepository.getOrThrow(id);
        userRepository.delete(e);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public UserDTO ensureUserExists(String clerkUserId) {
        var user = userRepository.findByClerkUserId(clerkUserId);

        if (user.isPresent()) {
            return mapper.toDto(user.get());
        }

        try {
            UserEntity saved = userRepository.save(UserEntity.builder()
                    .clerkUserId(clerkUserId)
                    .build());

            // Initialize default tags for new user
            tagService.createDefaultTagsForUser(saved.getId());

            return mapper.toDto(saved);
        } catch (DataIntegrityViolationException e) {
            // If two parallel first requests race, unique constraint wins; just re-read.
            return mapper.toDto(userRepository.getByClerkUserId(clerkUserId));
        }
    }

    @Transactional
    public UserDTO ensureUserExists(String clerkUserId, Jwt jwt) {
        var user = userRepository.findByClerkUserId(clerkUserId);

        if (user.isPresent()) {
            return mapper.toDto(user.get());
        }

        try {
            return createUserFromJwt(clerkUserId, jwt);
        } catch (DataIntegrityViolationException e) {
            // If two parallel first requests race, unique constraint wins; just re-read.
            log.debug("Concurrent user creation detected for Clerk ID: {}, re-reading from database", clerkUserId);
            return mapper.toDto(userRepository.getByClerkUserId(clerkUserId));
        }
    }

    private UserDTO createUserFromJwt(String clerkUserId, Jwt jwt) {
        log.info("Creating new user for Clerk ID: {}", clerkUserId);

        UserEntity newUser = UserEntity.builder()
                .clerkUserId(clerkUserId)
                .email(jwt.getClaimAsString("email"))
                .emailVerified(jwt.getClaimAsBoolean("email_verified"))
                .fullName(jwt.getClaimAsString("name"))
                .givenName(jwt.getClaimAsString("given_name"))
                .familyName(jwt.getClaimAsString("family_name"))
                .pictureUrl(jwt.getClaimAsString("picture"))
                .build();

        // Save user - createdBy and updatedBy will be set by JPA auditing to SYSTEM_USER_ID (-1)
        // indicating this user was auto-provisioned
        UserEntity saved = userRepository.save(newUser);

        // Initialize default tags for new user
        tagService.createDefaultTagsForUser(saved.getId());

        log.info("Created user: id={}, email={}, name={}, with default tags",
                saved.getId(), saved.getEmail(), saved.getFullName());

        return mapper.toDto(saved);
    }

}
