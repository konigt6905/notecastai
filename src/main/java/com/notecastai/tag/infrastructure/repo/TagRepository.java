package com.notecastai.tag.infrastructure.repo;

import com.notecastai.common.exception.BusinessException;
import com.notecastai.tag.domain.TagEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static com.notecastai.common.exception.BusinessException.BusinessCode.ENTITY_NOT_FOUND;

@Slf4j
@Repository
@RequiredArgsConstructor
public class TagRepository {

    private final TagDao dao;

    public TagEntity save(TagEntity entity) {
        return dao.save(entity);
    }

    public void delete(TagEntity entity) {
        dao.delete(entity);
    }

    public Optional<TagEntity> findById(Long id) {
        return dao.findById(id);
    }

    public TagEntity getById(Long id) {
        return dao.getReferenceById(id);
    }

    public boolean existsByUserAndNameIgnoreCase(Long userId, String name) {
        return dao.existsByUser_IdAndNameIgnoreCase(userId, name);
    }

    public long countByUserId(Long userId) {
        return dao.countByUser_Id(userId);
    }

    public List<TagEntity> findAllByUserId(Long userId) {
        return dao.findAllByUser_IdOrderByNameAsc(userId);
    }

    public TagEntity findByIdAndUserOrThrow(Long id, Long userId) {
        return dao.findByIdAndUser_Id(id, userId)
                .orElseThrow(() -> BusinessException.of(ENTITY_NOT_FOUND.append(" Tag with id %d not found for user %d".formatted(id, userId))));
    }

    /**
     * Resolves a list of tag IDs into entities, validating each belongs to the given user.
     * Skips null IDs. Returns empty set if tagIds is null or empty.
     */
    public Set<TagEntity> resolveAndValidateForUser(List<Long> tagIds, Long userId) {
        if (tagIds == null || tagIds.isEmpty()) return Set.of();
        Set<TagEntity> result = new HashSet<>();
        for (Long tagId : tagIds) {
            if (tagId == null) continue;
            result.add(findByIdAndUserOrThrow(tagId, userId));
        }
        return result;
    }

    public TagEntity findByNameAndUserId(String trim, Long userId) {
        return dao.findByNameAndUser_Id(trim, userId);
    }

    public List<TagDao.TagUsageProjection> findTopTagsByUserId(Long userId, int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return dao.findTopTagsByUserId(userId, pageable);
    }
}
