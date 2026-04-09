package com.notecastai.user.service;

import com.notecastai.user.api.dto.UserDTO;
import com.notecastai.user.domain.UserEntity;
import com.notecastai.user.service.command.CreateUserCommand;
import com.notecastai.user.service.command.UpdateUserCommand;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Optional;

public interface UserService {

    Long getCurrentUserId();

    /**
     * JPA proxy for the current user. No DB hit, only useful for setting FKs on new entities.
     */
    UserEntity getCurrentUserReference();

    /**
     * JPA proxy for a user by id. No DB hit, only useful for setting FKs.
     */
    UserEntity getUserReference(Long userId);

    UserDTO getCurrentUser();

    UserDTO updateCurrentUser(UpdateUserCommand command);

    UserDTO create(CreateUserCommand command);

    UserDTO getByClerkUserId(String clerkUserId);

    /**
     * Returns user by Clerk ID only if it matches the currently authenticated user.
     */
    UserDTO getByClerkUserIdForCurrentUser(String clerkUserId);

    Optional<UserDTO> findByClerkUserId(String clerkUserId);

    UserDTO ensureUserExists(String clerkUserId, Jwt jwt);

}
