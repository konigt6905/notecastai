package com.notecastai.voicenote.repo;

import com.notecastai.common.BaseRepository;
import com.notecastai.common.exeption.BusinessException;
import com.notecastai.common.query.CriteriaQueryBuilder;
import com.notecastai.common.util.SecurityUtils;
import com.notecastai.user.infrastructure.repo.UserRepository;
import com.notecastai.voicenote.api.dto.VoiceNoteQueryParam;
import com.notecastai.voicenote.domain.VoiceNoteEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import static com.notecastai.common.exeption.BusinessException.BusinessCode.ENTITY_NOT_FOUND;

@Slf4j
@Repository
public class VoiceNoteRepository extends BaseRepository<VoiceNoteEntity, Long, VoiceNoteDao> {

    @PersistenceContext
    private EntityManager entityManager;
    private final UserRepository userRepository;

    protected VoiceNoteRepository(EntityManager entityManager, UserRepository userRepository, VoiceNoteDao voiceNoteDao) {
        super(voiceNoteDao);
        this.entityManager = entityManager;
        this.userRepository = userRepository;
    }

    public VoiceNoteEntity getOrThrow(Long id) {
        return findById(id).orElseThrow(() ->
                BusinessException.of(ENTITY_NOT_FOUND.append(" VoiceNote with id %d not found".formatted(id)))
        );
    }

    public Page<VoiceNoteEntity> findAll(VoiceNoteQueryParam params, Pageable pageable) {
        SecurityUtils.getCurrentClerkUserIdOrThrow();
        return CriteriaQueryBuilder.forEntity(VoiceNoteEntity.class, entityManager)
                .where(b -> b
                        .equal("user.id", userRepository.getByClerkUserId(SecurityUtils.getCurrentClerkUserIdOrThrow()).getId())
                        .likeIgnoreCaseMultiple(params.getSearch(), "originalFilename", "transcript")
                        .equal("status", params.getStatus())
                        .joinIn("tags", "id", params.getTagIds())
                        .greaterThanOrEqual("createdDate", params.getFrom())
                        .lessThan("createdDate", params.getTo())
                )
                .distinct()
                .paginate(pageable);
    }

    public Long countProcessedByUserAndPeriod(com.notecastai.user.domain.UserEntity user, java.time.Instant fromDate, java.time.Instant toDate) {
        return CriteriaQueryBuilder.forEntity(VoiceNoteEntity.class, entityManager)
                .where(b -> b
                        .equal("user.id", user.getId())
                        .equal("status", com.notecastai.voicenote.domain.VoiceNoteStatus.PROCESSED)
                        .greaterThanOrEqual("createdDate", fromDate)
                        .lessThan("createdDate", toDate)
                )
                .count();
    }
}