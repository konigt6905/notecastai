package com.notecastai.common.util;

import com.notecastai.common.exception.BusinessException;

/**
 * Thread-local holder for the current user's database ID.
 * Set by {@link com.notecastai.config.OwnerHibernateFilter} at the start of each request,
 * read by JPA auditing ({@link com.notecastai.config.JwtAuditorAware}) and
 * ownership verification ({@link OwnershipVerifier}).
 */
public final class UserContext {

    private static final ThreadLocal<Long> currentUserId = new ThreadLocal<>();

    private UserContext() {
    }

    public static void set(Long userId) {
        currentUserId.set(userId);
    }

    public static Long get() {
        return currentUserId.get();
    }

    public static Long require() {
        Long id = currentUserId.get();
        if (id == null) {
            throw BusinessException.of(BusinessException.BusinessCode.CLERK_USER_ID_MISSING);
        }
        return id;
    }

    public static void clear() {
        currentUserId.remove();
    }
}
