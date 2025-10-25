package com.notecastai.common.util;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

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
}