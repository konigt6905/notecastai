package com.notecastai.config;

import com.notecastai.common.util.UserContext;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.hibernate.Session;
import org.springframework.stereotype.Component;

/**
 * Re-enables the Hibernate ownerFilter on async event handler threads.
 * OwnerHibernateFilter sets it on the request thread, but @TransactionalEventListener
 * methods run on a separate thread and would otherwise query without tenant scoping.
 * UserContextTaskDecorator is what gets the userId across; this aspect uses it.
 */
@Aspect
@Component
@Slf4j
public class AsyncOwnerFilterAspect {

    @PersistenceContext
    private EntityManager em;

    @Around("@annotation(org.springframework.transaction.event.TransactionalEventListener)")
    public Object enableOwnerFilter(ProceedingJoinPoint joinPoint) throws Throwable {
        Long userId = UserContext.get();

        if (userId != null && userId > 0) {
            Session session = em.unwrap(Session.class);
            session.enableFilter("ownerFilter")
                    .setParameter("currentUserId", userId);
            log.debug("ownerFilter enabled on async thread, userId={}", userId);

            try {
                return joinPoint.proceed();
            } finally {
                session.disableFilter("ownerFilter");
            }
        }

        // If we hit this, UserContextTaskDecorator didn't propagate the context. Investigate.
        log.warn("async event handler running without UserContext, ownerFilter NOT enabled");
        return joinPoint.proceed();
    }
}
