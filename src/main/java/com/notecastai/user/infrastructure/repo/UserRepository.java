package com.notecastai.user.infrastructure.repo;

import com.notecastai.common.exception.BusinessException;
import com.notecastai.user.domain.UserEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.Optional;

import static com.notecastai.common.exception.BusinessException.BusinessCode.ENTITY_NOT_FOUND;

@Slf4j
@Repository
@RequiredArgsConstructor
public class UserRepository {

    private final UserDao userDao;

    public UserEntity save(UserEntity entity) {
        return userDao.save(entity);
    }

    public void delete(UserEntity entity) {
        userDao.delete(entity);
    }

    public Optional<UserEntity> findById(Long id) {
        return userDao.findById(id);
    }

    public UserEntity getById(Long id) {
        return userDao.getReferenceById(id);
    }

    public boolean existsByClerkUserId(String clerkUserId) {
        return userDao.existsByClerkUserId(clerkUserId);
    }

    public UserEntity getByClerkUserId(String clerkUserId) {
        return userDao.findByClerkUserId(clerkUserId).orElseThrow(() -> BusinessException.of(BusinessException.BusinessCode.ENTITY_NOT_FOUND
                .append(" User with clerkUserId '%s' not found".formatted(clerkUserId))));
    }

    public Optional<UserEntity> findByClerkUserId(String clerkUserId) {
        return userDao.findByClerkUserId(clerkUserId);
    }

    public UserEntity getOrThrow(Long id) {
        return userDao.findById(id).orElseThrow(() ->
                BusinessException.of(ENTITY_NOT_FOUND.append(" User with id %d not found".formatted(id))));
    }

    public Page<UserEntity> findAllPaged(Pageable pageable) {
        return userDao.findAll(pageable);
    }
}
