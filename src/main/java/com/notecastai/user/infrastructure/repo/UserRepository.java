package com.notecastai.user.infrastructure.repo;

import com.notecastai.common.BaseRepository;
import com.notecastai.common.exeption.BusinessException;
import com.notecastai.user.domain.UserEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.Optional;

import static com.notecastai.common.exeption.BusinessException.BusinessCode.ENTITY_NOT_FOUND;

@Repository
@Slf4j
public class UserRepository extends BaseRepository<UserEntity, Long, UserDao> {

    private final UserDao userDao;

    protected UserRepository(UserDao dao) {
        super(dao);
        this.userDao = dao;
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
        return findById(id).orElseThrow(() ->
                BusinessException.of(ENTITY_NOT_FOUND.append(" User with id %d not found".formatted(id))));
    }

    public Page<UserEntity> findAllPaged(Pageable pageable) {
        return findAll(pageable);
    }
}