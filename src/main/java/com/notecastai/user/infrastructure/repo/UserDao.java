package com.notecastai.user.infrastructure.repo;

import com.notecastai.user.domain.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserDao extends JpaRepository<UserEntity, Long>, JpaSpecificationExecutor<UserEntity> {

    boolean existsByClerkUserId(String clerkUserId);

    Optional<UserEntity> findByClerkUserId(String clerkUserId);
}