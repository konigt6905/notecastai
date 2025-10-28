package com.notecastai.notecast.api.mapper;

import com.notecastai.common.EntityMapper;
import com.notecastai.notecast.api.dto.NoteCastResponseDTO;
import com.notecastai.notecast.domain.NoteCastEntity;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class NoteCastMapper implements EntityMapper<NoteCastEntity, NoteCastResponseDTO> {

}