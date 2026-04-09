package com.notecastai.notecast.infrastructure.repo;

import com.notecastai.common.exception.BusinessException;
import com.notecastai.common.query.CriteriaQueryBuilder;
import com.notecastai.notecast.api.dto.NoteCastQueryParam;
import com.notecastai.notecast.domain.NoteCastEntity;
import com.notecastai.user.service.UserService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;

import static com.notecastai.common.exception.BusinessException.BusinessCode.ENTITY_NOT_FOUND;

@Slf4j
@Repository
public class NoteCastRepository {

    private final NoteCastDao dao;
    @PersistenceContext
    private EntityManager entityManager;
    private final UserService userService;

    protected NoteCastRepository(NoteCastDao dao, EntityManager entityManager, UserService userService) {
        this.dao = dao;
        this.entityManager = entityManager;
        this.userService = userService;
    }

    public NoteCastEntity save(NoteCastEntity entity) {
        return dao.save(entity);
    }

    public void delete(NoteCastEntity entity) {
        dao.delete(entity);
    }

    public Optional<NoteCastEntity> findById(Long id) {
        return dao.findById(id);
    }

    public NoteCastEntity getById(Long id) {
        return dao.getReferenceById(id);
    }

    public NoteCastEntity getOrThrow(Long id) {
        return dao.findById(id).orElseThrow(() ->
                BusinessException.of(ENTITY_NOT_FOUND.append(" NoteCast with id %d not found".formatted(id)))
        );
    }

    public Page<NoteCastEntity> findAll(NoteCastQueryParam params, Pageable pageable) {
        return CriteriaQueryBuilder.forEntity(NoteCastEntity.class, entityManager)
                .where(b -> b
                        .equal("note.user.id", userService.getCurrentUserId())
                        .equal("note.id", params.getNoteId())
                        .joinIn("tags", "id", params.getTagIds())
                        .equal("status", params.getStatus())
                        .greaterThanOrEqual("createdDate", params.getFrom())
                        .lessThan("createdDate", params.getTo())
                )
                .distinct()
                .paginate(pageable);
    }

    public Long countByUserAndPeriod(Long userId, Instant fromDate, Instant toDate) {
        return CriteriaQueryBuilder.forEntity(NoteCastEntity.class, entityManager)
                .where(b -> b
                        .equal("note.user.id", userId)
                        .greaterThanOrEqual("createdDate", fromDate)
                        .lessThan("createdDate", toDate)
                )
                .count();
    }
}
