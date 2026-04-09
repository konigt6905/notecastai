package com.notecastai.gamenote.infrastructure.repo;

import com.notecastai.common.exception.BusinessException;
import com.notecastai.common.query.CriteriaQueryBuilder;
import com.notecastai.gamenote.api.dto.GameNoteQueryParam;
import com.notecastai.gamenote.domain.GameNoteEntity;
import com.notecastai.user.service.UserService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.Optional;

import static com.notecastai.common.exception.BusinessException.BusinessCode.ENTITY_NOT_FOUND;

@Slf4j
@Repository
public class GameNoteRepository {

    private final GameNoteDao dao;
    @PersistenceContext
    private EntityManager entityManager;
    private final UserService userService;

    protected GameNoteRepository(GameNoteDao dao, EntityManager entityManager, UserService userService) {
        this.dao = dao;
        this.entityManager = entityManager;
        this.userService = userService;
    }

    public GameNoteEntity save(GameNoteEntity entity) {
        return dao.save(entity);
    }

    public void delete(GameNoteEntity entity) {
        dao.delete(entity);
    }

    public Optional<GameNoteEntity> findById(Long id) {
        return dao.findById(id);
    }

    public GameNoteEntity getById(Long id) {
        return dao.getReferenceById(id);
    }

    public Page<GameNoteEntity> findAll(Pageable pageable) {
        return dao.findAll(pageable);
    }

    public GameNoteEntity getOrThrow(Long id) {
        return dao.findById(id).orElseThrow(() ->
                BusinessException.of(ENTITY_NOT_FOUND.append(" GameNote with id %d not found".formatted(id)))
        );
    }

    public Page<GameNoteEntity> findAll(GameNoteQueryParam params, Pageable pageable) {
        return CriteriaQueryBuilder.forEntity(GameNoteEntity.class, entityManager)
                .where(b -> b
                        .equal("user.id", userService.getCurrentUserId())
                        .likeIgnoreCase("title", params.getSearch())
                        .equal("status", params.getStatus())
                        .equal("questionType", params.getQuestionType())
                        .equal("difficulty", params.getDifficulty())
                        .joinIn("tags", "id", params.getTagIds())
                        .greaterThanOrEqual("createdDate", params.getFrom())
                        .lessThan("createdDate", params.getTo())
                )
                .distinct()
                .paginate(pageable);
    }

    public long countByUserId(Long userId) {
        return dao.count((root, query, cb) ->
                cb.equal(root.get("user").get("id"), userId)
        );
    }
}
