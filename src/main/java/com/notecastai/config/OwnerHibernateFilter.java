package com.notecastai.config;

import com.notecastai.common.util.SecurityUtils;
import com.notecastai.user.domain.UserEntity;
import com.notecastai.user.infrastructure.repo.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.hibernate.Session;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.io.IOException;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class OwnerHibernateFilter extends OncePerRequestFilter {

    @PersistenceContext
    private EntityManager em;

    private final UserRepository users;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        Optional<UserEntity> currentUser = Optional.ofNullable(SecurityUtils.getCurrentClerkUserId())
                .flatMap(users::findByClerkUserId);

        Session session = em.unwrap(Session.class);
        var filter = session.enableFilter("ownerFilter");
        if (currentUser.isPresent()) {
            filter.setParameter("currentUserId", currentUser.get().getId());
        } else {
            // bind an impossible id to ensure zero results for authenticated-required endpoints
            filter.setParameter("currentUserId", -1L);
        }

        try {
            chain.doFilter(request, response);
        } finally {
            session.disableFilter("ownerFilter");
        }
    }
}