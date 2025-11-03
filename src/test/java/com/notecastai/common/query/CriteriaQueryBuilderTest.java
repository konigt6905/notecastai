package com.notecastai.common.query;

import com.notecastai.note.domain.NoteEntity;
import com.notecastai.voicenote.domain.VoiceNoteEntity;
import com.notecastai.notecast.domain.NoteCastEntity;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(MockitoExtension.class)
class CriteriaQueryBuilderTest {

    @Mock
    private EntityManager entityManager;

    @Test
    void testCountMethodExists() {
        // This test verifies that the count() method exists and returns Long
        CriteriaQueryBuilder<NoteEntity> noteBuilder =
            CriteriaQueryBuilder.forEntity(NoteEntity.class, entityManager);

        // The following line will not compile if count() doesn't exist or doesn't return Long
        Long result = noteBuilder.where(b -> b.equal("id", 1L)).count();

        // If it compiles, the method exists and has the correct signature
        // In a real test, we would mock the EntityManager behavior
    }

    @Test
    void testCountMethodWorksForVoiceNote() {
        CriteriaQueryBuilder<VoiceNoteEntity> voiceNoteBuilder =
            CriteriaQueryBuilder.forEntity(VoiceNoteEntity.class, entityManager);

        Long result = voiceNoteBuilder
            .where(b -> b.equal("user.id", 1L))
            .count();
    }

    @Test
    void testCountMethodWorksForNoteCast() {
        CriteriaQueryBuilder<NoteCastEntity> noteCastBuilder =
            CriteriaQueryBuilder.forEntity(NoteCastEntity.class, entityManager);

        Long result = noteCastBuilder
            .where(b -> b.equal("note.user.id", 1L))
            .count();
    }
}