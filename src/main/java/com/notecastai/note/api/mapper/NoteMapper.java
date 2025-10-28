package com.notecastai.note.api.mapper;

import com.notecastai.common.EntityMapper;
import com.notecastai.note.api.dto.NoteDTO;
import com.notecastai.note.domain.NoteEntity;
import com.notecastai.tag.api.mapper.TagMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        uses = { TagMapper.class, AiActionMapper.class }
)
public interface NoteMapper extends EntityMapper<NoteEntity, NoteDTO> {

    @Override
    @Mapping(source = "user.id", target = "userId")
    NoteDTO toDto(NoteEntity entity);
}
