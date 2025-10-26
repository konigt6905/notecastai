package com.notecastai.voicenote.infrastructure.repo;

import com.notecastai.common.BaseRepository;
import com.notecastai.common.exeption.BusinessException;
import com.notecastai.common.query.CriteriaQueryBuilder;
import com.notecastai.common.util.SecurityUtils;
import com.notecastai.user.infrastructure.repo.UserRepository;
import com.notecastai.voicenote.api.dto.VoiceNoteQueryParam;
import com.notecastai.voicenote.domain.VoiceNoteEntity;
import com.notecastai.voicenote.repo.VoiceNoteDao;
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
                        .equal("status", params.getStatus())
                        .greaterThanOrEqual("createdDate", params.getFrom())
                        .lessThan("createdDate", params.getTo())
                )
                .paginate(pageable);
    }

    @Override
    public Optional<VoiceNoteEntity> findById(Long id) {
        return super.findById(id);
    }
}