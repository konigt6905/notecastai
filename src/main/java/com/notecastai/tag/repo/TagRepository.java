package com.notecastai.tag.repo;

import com.notecastai.common.BaseRepository;
import com.notecastai.common.exeption.BusinessException;
import com.notecastai.tag.domain.TagEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.notecastai.common.exeption.BusinessException.BusinessCode.ENTITY_NOT_FOUND;

@Repository
@Slf4j
public class TagRepository extends BaseRepository<TagEntity, Long, TagDao> {

    protected TagRepository(TagDao dao) {
        super(dao);
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

    public TagEntity findByNameAndUserId(String trim, Long userId) {
        return dao.findByNameAndUser_Id(trim, userId);
    }

    public List<TagDao.TagUsageProjection> findTopTagsByUserId(Long userId, int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return dao.findTopTagsByUserId(userId, pageable);
    }
}