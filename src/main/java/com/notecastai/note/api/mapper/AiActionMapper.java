package com.notecastai.note.api.mapper;

import com.notecastai.common.EntityMapper;
import com.notecastai.note.api.dto.AiActionDto;
import com.notecastai.note.domain.NoteEntity;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AiActionMapper extends EntityMapper<NoteEntity.AiAction, AiActionDto> {

}