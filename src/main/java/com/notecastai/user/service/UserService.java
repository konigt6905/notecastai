package com.notecastai.user.service;

import com.notecastai.user.api.dto.UserCreateRequest;
import com.notecastai.user.api.dto.UserDTO;
import com.notecastai.user.api.dto.UserUpdateRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface UserService {

    UserDTO create(UserCreateRequest request);

    UserDTO update(Long id, UserUpdateRequest request);

    UserDTO getById(Long id);

    UserDTO getByClerkUserId(String clerkUserId);

    Optional<UserDTO> findByClerkUserId(String clerkUserId);

    Page<UserDTO> findAll(Pageable pageable);

    void delete(Long id);

    UserDTO ensureUserExists(String clerkUserId);

}