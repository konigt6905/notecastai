package com.notecastai.user.infrastructure.repo;

import com.notecastai.common.BaseRepository;
import com.notecastai.note.domain.NoteEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

@Repository
@Slf4j
public class UserRepository extends BaseRepository<NoteEntity, Long, UserDao> {

    protected UserRepository(UserDao dao) {
        super(dao);
    }
}