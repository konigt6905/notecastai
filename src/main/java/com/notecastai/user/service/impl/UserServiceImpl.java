package com.notecastai.user.service.impl;

import com.notecastai.common.exception.BusinessException;
import com.notecastai.common.util.SecurityUtils;
import com.notecastai.tag.service.impl.DefaultTagProvisioner;
import com.notecastai.user.api.dto.UserDTO;
import com.notecastai.user.service.command.CreateUserCommand;
import com.notecastai.user.service.command.UpdateUserCommand;
import com.notecastai.user.api.mapper.UserMapper;
import com.notecastai.user.domain.UserEntity;
import com.notecastai.user.infrastructure.repo.UserRepository;
import com.notecastai.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static com.notecastai.common.exception.BusinessException.BusinessCode.CONFLICT;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper mapper;
    private final DefaultTagProvisioner defaultTagProvisioner;

    @Override
    @Transactional(readOnly = true)
    public Long getCurrentUserId() {
        return userRepository.getByClerkUserId(SecurityUtils.getCurrentClerkUserIdOrThrow()).getId();
    }

    @Override
    @Transactional(readOnly = true)
    public UserEntity getCurrentUserReference() {
        return userRepository.getById(getCurrentUserId());
    }

    @Override
    @Transactional(readOnly = true)
    public UserEntity getUserReference(Long userId) {
        return userRepository.getById(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public UserDTO getCurrentUser() {
        return mapper.toDto(userRepository.getByClerkUserId(SecurityUtils.getCurrentClerkUserIdOrThrow()));
    }

    @Override
    @Transactional
    public UserDTO updateCurrentUser(UpdateUserCommand command) {
        UserEntity entity = userRepository.getByClerkUserId(SecurityUtils.getCurrentClerkUserIdOrThrow());

        if (command.getDefaultFormat() != null) {
            entity.setDefaultFormat(command.getDefaultFormat());
        }
        if (command.getDefaultVoice() != null) {
            entity.setDefaultVoice(command.getDefaultVoice());
        }
        if (command.getPreferredLanguage() != null) {
            entity.setPreferredLanguage(command.getPreferredLanguage());
        }

        return mapper.toDto(userRepository.save(entity));
    }

    @Override
    @Transactional
    public UserDTO create(CreateUserCommand command) {
        String clerkId = command.getClerkUserId().trim();

        if (userRepository.existsByClerkUserId(clerkId)) {
            throw BusinessException.of(CONFLICT.append(" user with clerkUserId already exists").append(" clerkUserId: %s".formatted(clerkId)));
        }

        UserEntity entity = UserEntity.builder()
                .clerkUserId(clerkId)
                .build();

        UserEntity saved = userRepository.save(entity);

        // Initialize default tags
        defaultTagProvisioner.provisionDefaultTags(saved.getId());

        return mapper.toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public UserDTO getByClerkUserId(String clerkUserId) {
        return mapper.toDto(userRepository.getByClerkUserId(clerkUserId));
    }

    @Override
    @Transactional(readOnly = true)
    public UserDTO getByClerkUserIdForCurrentUser(String clerkUserId) {
        String currentClerkId = SecurityUtils.getCurrentClerkUserIdOrThrow();
        if (!currentClerkId.equals(clerkUserId)) {
            throw BusinessException.of(BusinessException.BusinessCode.FORBIDDEN);
        }
        return mapper.toDto(userRepository.getByClerkUserId(clerkUserId));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UserDTO> findByClerkUserId(String clerkUserId) {
        return userRepository.findByClerkUserId(clerkUserId).map(mapper::toDto);
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

        // Save user - createdBy/updatedBy set by JPA auditing via UserContext
        // (null during auto-provisioning since UserContext is not yet set)
        UserEntity saved = userRepository.save(newUser);

        // Initialize default tags for new user
        defaultTagProvisioner.provisionDefaultTags(saved.getId());

        log.info("Created user: id={}, email={}, name={}, with default tags",
                saved.getId(), saved.getEmail(), saved.getFullName());

        return mapper.toDto(saved);
    }

}
