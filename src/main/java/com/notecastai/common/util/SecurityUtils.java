package com.notecastai.common.util;

import com.notecastai.common.exeption.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import static com.notecastai.common.exeption.BusinessException.BusinessCode.CLERK_USER_ID_MISSING;

@Slf4j
public final class SecurityUtils {
    private SecurityUtils() {
    }

    public static String getCurrentClerkUserId() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication instanceof JwtAuthenticationToken token) {
            return token.getToken().getSubject();
        }
        return null;
    }

    public static String getCurrentClerkUserIdOrThrow() {
        String clerkUserId = getCurrentClerkUserId();

        if (clerkUserId == null) {
            throw BusinessException.of(CLERK_USER_ID_MISSING);
        }

        return clerkUserId;
    }

}