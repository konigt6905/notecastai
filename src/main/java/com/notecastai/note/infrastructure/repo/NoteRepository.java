package com.notecastai.note.infrastructure.repo;

import com.notecastai.common.exception.BusinessException;
import com.notecastai.common.query.CriteriaQueryBuilder;
import com.notecastai.note.api.dto.NotesQueryParam;
import com.notecastai.note.domain.NoteEntity;
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
public class NoteRepository {

    private final NoteDao dao;
    @PersistenceContext
    private EntityManager entityManager;
    private final UserService userService;

    protected NoteRepository(NoteDao dao, EntityManager entityManager, UserService userService) {
        this.dao = dao;
        this.entityManager = entityManager;
        this.userService = userService;
    }

    public NoteEntity save(NoteEntity entity) {
        return dao.save(entity);
    }

    public void delete(NoteEntity entity) {
        dao.delete(entity);
    }

    public Optional<NoteEntity> findById(Long id) {
        return dao.findById(id);
    }

    public NoteEntity getById(Long id) {
        return dao.getReferenceById(id);
    }

    public NoteEntity getOrThrow(Long id) {
        return dao.findById(id).orElseThrow(() ->
                BusinessException.of(ENTITY_NOT_FOUND.append(" Note with id %d not found".formatted(id)))
        );
    }

    public Page<NoteEntity> findAll(NotesQueryParam params, Pageable pageable) {
        return CriteriaQueryBuilder.forEntity(NoteEntity.class, entityManager)
                .where(b -> b
                        .equal("user.id", userService.getCurrentUserId())
                        .likeIgnoreCaseMultiple(params.getSearch(), "title", "knowledgeBase")
                        .joinIn("tags", "id", params.getTagIds())
                        .equal("type", params.getType())
                        .equal("currentFormat", params.getCurrentFormat())
                        .greaterThanOrEqual("createdDate", params.getFrom())
                        .lessThan("createdDate", params.getTo())
                )
                .distinct()
                .paginate(pageable);
    }

    public Long countByUserAndPeriod(Long userId, Instant fromDate, Instant toDate) {
        return CriteriaQueryBuilder.forEntity(NoteEntity.class, entityManager)
                .where(b -> b
                        .equal("user.id", userId)
                        .greaterThanOrEqual("createdDate", fromDate)
                        .lessThan("createdDate", toDate)
                )
                .count();
    }
}
