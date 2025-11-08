package com.notecastai.note.infrastructure.repo;

import com.notecastai.common.BaseRepository;
import com.notecastai.common.exeption.BusinessException;
import com.notecastai.common.query.CriteriaQueryBuilder;
import com.notecastai.common.util.SecurityUtils;
import com.notecastai.note.api.dto.NotesQueryParam;
import com.notecastai.note.domain.NoteEntity;
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
public class NoteRepository extends BaseRepository<NoteEntity, Long, NoteDao> {

    @PersistenceContext
    private EntityManager entityManager;
    private UserRepository userRepository;

    protected NoteRepository(EntityManager noteDaoentityManager, UserRepository userRepository, NoteDao noteDao) {
        super(noteDao);
        this.entityManager = noteDaoentityManager;
        this.userRepository = userRepository;
    }

    public NoteEntity getOrThrow(Long id) {
        return findById(id).orElseThrow(() ->
                BusinessException.of(ENTITY_NOT_FOUND.append(" Note with id %d not found".formatted(id)))
        );
    }

    public Page<NoteEntity> findAll(NotesQueryParam params, Pageable pageable) {
        SecurityUtils.getCurrentClerkUserIdOrThrow();
        return CriteriaQueryBuilder.forEntity(NoteEntity.class, entityManager)
                .where(b -> b
                        .equal("user.id", userRepository.getByClerkUserId(SecurityUtils.getCurrentClerkUserIdOrThrow()).getId())
                        .likeIgnoreCaseMultiple(params.getSearch(), "title", "knowledgeBase")
                        .joinIn("tags", "id", params.getTagIds())
                        .equal("type", params.getType())
                        .equal("currentFormate", params.getCurrentFormate())
                        .greaterThanOrEqual("createdDate", params.getFrom())
                        .lessThan("createdDate", params.getTo())
                )
                .distinct()
                .paginate(pageable);
    }

    @Override
    public Optional<NoteEntity> findById(Long id) {
        return super.findById(id);
    }

    public Long countByUserAndPeriod(com.notecastai.user.domain.UserEntity user, java.time.Instant fromDate, java.time.Instant toDate) {
        return CriteriaQueryBuilder.forEntity(NoteEntity.class, entityManager)
                .where(b -> b
                        .equal("user.id", user.getId())
                        .greaterThanOrEqual("createdDate", fromDate)
                        .lessThan("createdDate", toDate)
                )
                .count();
    }
}