package com.notecastai.notecast.infrastructure.repo;

import com.notecastai.common.BaseRepository;
import com.notecastai.common.exeption.BusinessException;
import com.notecastai.common.query.CriteriaQueryBuilder;
import com.notecastai.common.util.SecurityUtils;
import com.notecastai.notecast.api.dto.NoteCastQueryParam;
import com.notecastai.notecast.domain.NoteCastEntity;
import com.notecastai.user.infrastructure.repo.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.Optional;

import static com.notecastai.common.exeption.BusinessException.BusinessCode.ENTITY_NOT_FOUND;

@Slf4j
@Repository
public class NoteCastRepository extends BaseRepository<NoteCastEntity, Long, NoteCastDao> {

    @PersistenceContext
    private EntityManager entityManager;
    private final UserRepository userRepository;

    protected NoteCastRepository(EntityManager entityManager, UserRepository userRepository, NoteCastDao noteCastDao) {
        super(noteCastDao);
        this.entityManager = entityManager;
        this.userRepository = userRepository;
    }

    public NoteCastEntity getOrThrow(Long id) {
        return findById(id).orElseThrow(() ->
                BusinessException.of(ENTITY_NOT_FOUND.append(" NoteCast with id %d not found".formatted(id)))
        );
    }

    public Page<NoteCastEntity> findAll(NoteCastQueryParam params, Pageable pageable) {
        SecurityUtils.getCurrentClerkUserIdOrThrow();
        return CriteriaQueryBuilder.forEntity(NoteCastEntity.class, entityManager)
                .where(b -> b
                        .equal("note.user.id", userRepository.getByClerkUserId(SecurityUtils.getCurrentClerkUserIdOrThrow()).getId())
                        .equal("note.id", params.getNoteId())
                        .joinIn("note.tags", "id", params.getTagIds())
                        .equal("status", params.getStatus())
                        .greaterThanOrEqual("createdDate", params.getFrom())
                        .lessThan("createdDate", params.getTo())
                )
                .distinct()
                .paginate(pageable);
    }

    @Override
    public Optional<NoteCastEntity> findById(Long id) {
        return super.findById(id);
    }

    public Long countByUserAndPeriod(com.notecastai.user.domain.UserEntity user, java.time.Instant fromDate, java.time.Instant toDate) {
        return CriteriaQueryBuilder.forEntity(NoteCastEntity.class, entityManager)
                .where(b -> b
                        .equal("note.user.id", user.getId())
                        .greaterThanOrEqual("createdDate", fromDate)
                        .lessThan("createdDate", toDate)
                )
                .count();
    }
}