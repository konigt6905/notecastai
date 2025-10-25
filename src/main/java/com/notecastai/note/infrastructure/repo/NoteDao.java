package com.notecastai.note.infrastructure.repo;

import com.notecastai.note.domain.NoteEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface NoteDao extends JpaRepository<NoteEntity, Long>, JpaSpecificationExecutor<NoteEntity> {

}
