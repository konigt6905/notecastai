package com.notecastai.tag.repo;

import com.notecastai.tag.domain.TagEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TagDao extends JpaRepository<TagEntity, Long>, JpaSpecificationExecutor<TagEntity> {

    boolean existsByUser_IdAndNameIgnoreCase(Long userId, String name);

    long countByUser_Id(Long userId);

    List<TagEntity> findAllByUser_IdOrderByNameAsc(Long userId);

    Optional<TagEntity> findByIdAndUser_Id(Long id, Long userId);

    TagEntity findByNameAndUser_Id(String trim, Long userId);

    @Query(value = """
        SELECT t.id as id, t.name as name, COUNT(nt.note_id) as usageCount
        FROM tag t
        INNER JOIN note_tag nt ON t.id = nt.tag_id
        INNER JOIN note n ON nt.note_id = n.id
        WHERE t.user_id = :userId AND n.user_id = :userId
        GROUP BY t.id, t.name
        ORDER BY COUNT(nt.note_id) DESC
        """, nativeQuery = true)
    List<TagUsageProjection> findTopTagsByUserId(@Param("userId") Long userId, Pageable pageable);

    /**
     * Projection interface for tag usage statistics.
     */
    interface TagUsageProjection {
        Long getId();
        String getName();
        Long getUsageCount();
    }

}
