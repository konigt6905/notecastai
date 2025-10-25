package com.notecastai.voicenote.repo;

import com.notecastai.voicenote.domain.VoiceNoteEntity;
import com.notecastai.common.BaseRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

@Repository
@Slf4j
public class NoteRepository extends BaseRepository<VoiceNoteEntity, Long, NoteDao> {

    protected NoteRepository(NoteDao dao) {
        super(dao);
    }
}