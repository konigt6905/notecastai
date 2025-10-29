package com.notecastai.config;

import com.notecastai.user.infrastructure.repo.UserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@Configuration
@EnableJpaAuditing
public class JpaAuditConfig {

    @Bean
    public AuditorAware<Long> auditorAware(UserRepository userRepository) {
        return new JwtAuditorAware(userRepository);
    }
}