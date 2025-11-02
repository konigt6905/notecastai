package com.notecastai.config;

import com.notecastai.common.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.AuditorAware;

import java.util.Optional;

@RequiredArgsConstructor
@Slf4j
public class JwtAuditorAware implements AuditorAware<Long> {

    /**
     * SYSTEM user ID used for:
     * - Auto-provisioning new users from Clerk
     * - System-initiated operations
     */
    public static final Long SYSTEM_USER_ID = -1L;

    @Override
    public Optional<Long> getCurrentAuditor() {
        // For authenticated users, we use SYSTEM_USER_ID for auditing
        // The actual user tracking is done via the Clerk user ID (clerkUserId field)
        // This avoids database queries during entity persistence which could cause infinite recursion
        String clerkUserId = SecurityUtils.getCurrentClerkUserId();

        if (clerkUserId != null) {
            // User is authenticated - use SYSTEM for auditing
            return Optional.of(SYSTEM_USER_ID);
        }

        // No authenticated user
        return Optional.empty();
    }
}