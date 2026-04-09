package com.notecastai.config;

import com.notecastai.common.util.SecurityUtils;
import com.notecastai.common.util.UserContext;
import com.notecastai.user.api.dto.UserDTO;
import com.notecastai.user.service.UserService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.hibernate.Session;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class OwnerHibernateFilter extends OncePerRequestFilter {

    @PersistenceContext
    private EntityManager em;

    private final UserService userService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        String clerkUserId = SecurityUtils.getCurrentClerkUserId();
        Optional<UserDTO> currentUser = Optional.ofNullable(clerkUserId)
                .flatMap(userService::findByClerkUserId);

        // first request from a new Clerk user, create them on the fly
        if (clerkUserId != null && currentUser.isEmpty()) {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication instanceof JwtAuthenticationToken jwtAuth) {
                Jwt jwt = jwtAuth.getToken();
                UserDTO created = userService.ensureUserExists(clerkUserId, jwt);
                currentUser = Optional.of(created);
            }
        }

        Long userId = currentUser.map(UserDTO::getId).orElse(-1L);

        // JwtAuditorAware and OwnershipVerifier read this
        UserContext.set(userId);

        Session session = em.unwrap(Session.class);
        var filter = session.enableFilter("ownerFilter");
        filter.setParameter("currentUserId", userId);

        try {
            chain.doFilter(request, response);
        } finally {
            session.disableFilter("ownerFilter");
            UserContext.clear();
        }
    }
}