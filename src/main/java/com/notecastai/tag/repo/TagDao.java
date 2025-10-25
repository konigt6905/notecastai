package com.notecastai.tag.repo;

import com.notecastai.tag.domain.TagEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TagDao extends JpaRepository<TagEntity, Long>, JpaSpecificationExecutor<TagEntity> {

    boolean existsByUser_IdAndNameIgnoreCase(Long userId, String name);

    long countByUser_Id(Long userId);

    List<TagEntity> findAllByUser_IdOrderByNameAsc(Long userId);

    Optional<TagEntity> findByIdAndUser_Id(Long id, Long userId);

}
