package com.notecastai.config;

import com.notecastai.common.util.SecurityUtils;
import com.notecastai.user.domain.UserEntity;
import com.notecastai.user.infrastructure.repo.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.AuditorAware;

import java.util.Optional;

@RequiredArgsConstructor
public class JwtAuditorAware implements AuditorAware<Long> {

    private final UserRepository userRepository;

    @Override
    public Optional<Long> getCurrentAuditor() {
        String clerkUserId = SecurityUtils.getCurrentClerkUserIdOrThrow();
        UserEntity user = userRepository.getByClerkUserId(clerkUserId);
        return Optional.of(user.getId());
    }
}