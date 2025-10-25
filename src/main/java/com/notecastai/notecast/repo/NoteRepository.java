package com.notecastai.notecast.repo;

import com.notecastai.common.BaseRepository;
import com.notecastai.notecast.domain.NoteCastEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

@Repository
@Slf4j
public class NoteRepository extends BaseRepository<NoteCastEntity, Long, NoteDao> {

    protected NoteRepository(NoteDao dao) {
        super(dao);
    }
}