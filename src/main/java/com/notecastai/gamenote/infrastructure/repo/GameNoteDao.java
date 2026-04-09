package com.notecastai.gamenote.infrastructure.repo;

import com.notecastai.gamenote.domain.GameNoteEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface GameNoteDao extends JpaRepository<GameNoteEntity, Long>, JpaSpecificationExecutor<GameNoteEntity> {

}
