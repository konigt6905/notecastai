package com.notecastai.common;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class BaseRepository<T, ID, D extends JpaRepository<T, ID>> {

   protected final D dao;

   public T getById(ID id) {
        return dao.getReferenceById(id);
    }

    public Page<T> findAll(Pageable pageable) {
        return dao.findAll(pageable);
    }

    public Optional<T> findById(ID id) {
        return dao.findById(id);
    }

    public T save(T entity) {
        return dao.save(entity);
    }

    public T saveAndFlush(T entity) {
        return dao.saveAndFlush(entity);
    }

}