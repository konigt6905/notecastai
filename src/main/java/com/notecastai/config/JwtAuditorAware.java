package com.notecastai.config;

import com.notecastai.common.util.SecurityUtils;
import com.notecastai.user.infrastructure.repo.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.AuditorAware;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class JwtAuditorAware implements AuditorAware<Long> {

    private final UserRepository users;

    @Override
    public Optional<Long> getCurrentAuditor() {
        String clerkUserId = SecurityUtils.getCurrentClerkUserId();
        if (clerkUserId == null) return Optional.empty();
        return users.findIdByClerkUserId(clerkUserId);
    }
}