package com.notecastai.config;

import com.notecastai.common.util.UserContext;
import org.springframework.core.task.TaskDecorator;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Propagates per-request context from the submitting thread to the async
 * worker thread:
 *
 * <ul>
 *   <li>{@link UserContext} — tenant id used by Hibernate filters.</li>
 *   <li>{@link SecurityContextHolder} — Spring Security authentication, so
 *       downstream services that call {@code SecurityUtils.getCurrentClerkUserIdOrThrow()}
 *       still see the authenticated user inside {@code @Async} methods.</li>
 * </ul>
 *
 * Both are ThreadLocal and must be cleared after execution to avoid leaks
 * in pooled worker threads.
 */
public class UserContextTaskDecorator implements TaskDecorator {

    @Override
    public Runnable decorate(Runnable runnable) {
        // Capture context from the submitting (usually request) thread.
        Long userId = UserContext.get();
        SecurityContext securityContext = SecurityContextHolder.getContext();

        return () -> {
            try {
                if (userId != null) {
                    UserContext.set(userId);
                }
                if (securityContext != null) {
                    SecurityContextHolder.setContext(securityContext);
                }
                runnable.run();
            } finally {
                // Always clean up to prevent ThreadLocal leaks in pooled threads.
                UserContext.clear();
                SecurityContextHolder.clearContext();
            }
        };
    }
}
