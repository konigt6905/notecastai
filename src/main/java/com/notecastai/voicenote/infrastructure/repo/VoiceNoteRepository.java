package com.notecastai.voicenote.infrastructure.repo;

import com.notecastai.common.exception.BusinessException;
import com.notecastai.common.query.CriteriaQueryBuilder;
import com.notecastai.user.service.UserService;
import com.notecastai.voicenote.api.dto.VoiceNoteQueryParam;
import com.notecastai.voicenote.domain.VoiceNoteEntity;
import com.notecastai.voicenote.domain.VoiceNoteStatus;
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
public class VoiceNoteRepository {

    private final VoiceNoteDao dao;
    @PersistenceContext
    private EntityManager entityManager;
    private final UserService userService;

    protected VoiceNoteRepository(VoiceNoteDao dao, EntityManager entityManager, UserService userService) {
        this.dao = dao;
        this.entityManager = entityManager;
        this.userService = userService;
    }

    public VoiceNoteEntity save(VoiceNoteEntity entity) {
        return dao.save(entity);
    }

    public VoiceNoteEntity saveAndFlush(VoiceNoteEntity entity) {
        return dao.saveAndFlush(entity);
    }

    public void delete(VoiceNoteEntity entity) {
        dao.delete(entity);
    }

    public Optional<VoiceNoteEntity> findById(Long id) {
        return dao.findById(id);
    }

    public VoiceNoteEntity getById(Long id) {
        return dao.getReferenceById(id);
    }

    public Page<VoiceNoteEntity> findAll(Pageable pageable) {
        return dao.findAll(pageable);
    }

    public VoiceNoteEntity getOrThrow(Long id) {
        return dao.findById(id).orElseThrow(() ->
                BusinessException.of(ENTITY_NOT_FOUND.append(" VoiceNote with id %d not found".formatted(id)))
        );
    }

    public Page<VoiceNoteEntity> findAll(VoiceNoteQueryParam params, Pageable pageable) {
        return CriteriaQueryBuilder.forEntity(VoiceNoteEntity.class, entityManager)
                .where(b -> b
                        .equal("user.id", userService.getCurrentUserId())
                        .likeIgnoreCaseMultiple(params.getSearch(), "originalFilename", "transcript")
                        .equal("status", params.getStatus())
                        .joinIn("tags", "id", params.getTagIds())
                        .greaterThanOrEqual("createdDate", params.getFrom())
                        .lessThan("createdDate", params.getTo())
                )
                .distinct()
                .paginate(pageable);
    }

    public Long countProcessedByUserAndPeriod(Long userId, Instant fromDate, Instant toDate) {
        return CriteriaQueryBuilder.forEntity(VoiceNoteEntity.class, entityManager)
                .where(b -> b
                        .equal("user.id", userId)
                        .equal("status", VoiceNoteStatus.PROCESSED)
                        .greaterThanOrEqual("createdDate", fromDate)
                        .lessThan("createdDate", toDate)
                )
                .count();
    }
}
