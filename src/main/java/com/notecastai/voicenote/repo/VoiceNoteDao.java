package com.notecastai.voicenote.repo;

import com.notecastai.voicenote.domain.VoiceNoteEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface VoiceNoteDao extends JpaRepository<VoiceNoteEntity, Long>, JpaSpecificationExecutor<VoiceNoteEntity> {

}
