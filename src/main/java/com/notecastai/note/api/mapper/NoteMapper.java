package com.notecastai.note.api.mapper;

import com.notecastai.common.EntityMapper;
import com.notecastai.note.api.dto.NoteDTO;
import com.notecastai.note.domain.NoteEntity;
import com.notecastai.tag.api.mapper.TagMapper;
import com.notecastai.user.api.mapper.UserMapper;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        uses = { TagMapper.class, AiActionMapper.class, UserMapper.class }
)
public interface NoteMapper extends EntityMapper<NoteEntity, NoteDTO> {

    @Override
    NoteDTO toDto(NoteEntity entity);
}
