package com.notecastai.config;

import com.notecastai.common.util.UserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.AuditorAware;

import java.util.Optional;

@RequiredArgsConstructor
@Slf4j
public class JwtAuditorAware implements AuditorAware<Long> {

    public static final Long SYSTEM_USER_ID = -1L;

    @Override
    public Optional<Long> getCurrentAuditor() {
        // userId is already resolved by OwnerHibernateFilter, just read it from the thread local
        Long userId = UserContext.get();

        if (userId != null && userId > 0) {
            return Optional.of(userId);
        }

        return Optional.empty();
    }
}