package com.notecastai.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.oauth2.server.resource.web.BearerTokenAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@Slf4j
public class SecurityConfig {

    @Value("${clerk.issuer}")
    private String issuer;
    @Value("${clerk.jwks}")
    private String jwks;
    @Value("${clerk.audience:}")
    private String audience;

    private final Environment environment;
    private final RateLimitFilter rateLimitFilter;

    public SecurityConfig(Environment environment, RateLimitFilter rateLimitFilter) {
        this.environment = environment;
        this.rateLimitFilter = rateLimitFilter;
    }

    private boolean isProduction() {
        return Arrays.asList(environment.getActiveProfiles()).contains("prod");
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource(
            @Value("${application.domain:http://localhost:3000}") String feOrigin) {
        CorsConfiguration cfg = new CorsConfiguration();
        List<String> origins = Arrays.stream(feOrigin.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
        cfg.setAllowedOrigins(origins);
        cfg.setAllowedMethods(List.of("GET","POST","PUT","DELETE","PATCH","OPTIONS"));
        cfg.setAllowedHeaders(List.of("Authorization","Content-Type","Accept"));
        cfg.setAllowCredentials(true);
        cfg.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", cfg);
        return source;
    }

    @Bean
    SecurityFilterChain security(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable);
        http.authorizeHttpRequests(auth -> {
            auth.requestMatchers("/health", "/actuator/health").permitAll();

            if (!isProduction()) {
                // actuator + swagger only outside prod
                auth.requestMatchers("/actuator/**").permitAll();
                auth.requestMatchers(
                        "/v3/api-docs/**",
                        "/swagger-ui/**",
                        "/swagger-ui.html",
                        "/swagger-resources/**",
                        "/webjars/**"
                ).permitAll();
            }

            auth.anyRequest().authenticated();
        });
        http.oauth2ResourceServer(o -> o.jwt(Customizer.withDefaults()));
        http.cors(Customizer.withDefaults());
        http.addFilterAfter(rateLimitFilter, BearerTokenAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    JwtDecoder jwtDecoder() {
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withJwkSetUri(jwks).build();
        OAuth2TokenValidator<Jwt> withIssuer = JwtValidators.createDefaultWithIssuer(issuer);
        OAuth2TokenValidator<Jwt> withAudience = new JwtClaimValidator<>("aud", aud -> {
            if (aud instanceof java.util.Collection<?> c) {
                return c.contains(audience);
            }
            if (aud instanceof String s) {
                return s.equals(audience);
            }
            return false;
        });
        decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(withIssuer, withAudience));
        return decoder;
    }
}
