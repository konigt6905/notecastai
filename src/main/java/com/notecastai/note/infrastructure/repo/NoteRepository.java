package com.notecastai.note.infrastructure.repo;

import com.notecastai.common.BaseRepository;
import com.notecastai.note.domain.NoteEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

@Repository
@Slf4j
public class NoteRepository extends BaseRepository<NoteEntity, Long, NoteDao> {

    protected NoteRepository(NoteDao dao) {
        super(dao);
    }
}