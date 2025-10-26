package com.notecastai.user.service;

import com.notecastai.user.api.dto.UserCreateRequest;
import com.notecastai.user.api.dto.UserDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserService {

    UserDTO create(UserCreateRequest request);

    UserDTO getById(Long id);

    UserDTO getByClerkUserId(String clerkUserId);

    Page<UserDTO> findAll(Pageable pageable);

    void deactivate(Long id);
}