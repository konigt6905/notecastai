package com.notecastai.common.util;

import com.notecastai.common.exeption.BusinessException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import static com.notecastai.common.exeption.BusinessException.BusinessCode.CLERK_USER_ID_MISSING;

public final class SecurityUtils {
    private SecurityUtils() {}

    /** Returns Clerk user id (JWT sub) or null. */
    public static String getCurrentClerkUserId() {
        Authentication a = SecurityContextHolder.getContext().getAuthentication();
        if (a instanceof JwtAuthenticationToken t) {
            return t.getToken().getSubject(); // Clerk "sub", e.g. user_abc123
        }
        return null;
    }

    public static String getCurrentClerkUserIdOrThrow() {
        String id = getCurrentClerkUserId();
        if (id == null) throw BusinessException.of(CLERK_USER_ID_MISSING);
        return id;
    }

}