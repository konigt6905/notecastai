package com.notecastai.gamenote.infrastructure.repo;

import com.notecastai.common.BaseRepository;
import com.notecastai.common.exeption.BusinessException;
import com.notecastai.common.query.CriteriaQueryBuilder;
import com.notecastai.common.util.SecurityUtils;
import com.notecastai.gamenote.api.dto.GameNoteQueryParam;
import com.notecastai.gamenote.domain.GameNoteEntity;
import com.notecastai.user.infrastructure.repo.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import static com.notecastai.common.exeption.BusinessException.BusinessCode.ENTITY_NOT_FOUND;

@Slf4j
@Repository
public class GameNoteRepository extends BaseRepository<GameNoteEntity, Long, GameNoteDao> {

    @PersistenceContext
    private EntityManager entityManager;
    private final UserRepository userRepository;

    protected GameNoteRepository(EntityManager entityManager, UserRepository userRepository, GameNoteDao gameNoteDao) {
        super(gameNoteDao);
        this.entityManager = entityManager;
        this.userRepository = userRepository;
    }

    public GameNoteEntity getOrThrow(Long id) {
        return findById(id).orElseThrow(() ->
                BusinessException.of(ENTITY_NOT_FOUND.append(" GameNote with id %d not found".formatted(id)))
        );
    }

    public Page<GameNoteEntity> findAll(GameNoteQueryParam params, Pageable pageable) {
        SecurityUtils.getCurrentClerkUserIdOrThrow();
        return CriteriaQueryBuilder.forEntity(GameNoteEntity.class, entityManager)
                .where(b -> b
                        .equal("user.id", userRepository.getByClerkUserId(SecurityUtils.getCurrentClerkUserIdOrThrow()).getId())
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
