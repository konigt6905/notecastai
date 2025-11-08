package com.notecastai.notecast.api.mapper;

import com.notecastai.common.EntityMapper;
import com.notecastai.notecast.api.dto.NoteCastResponseDTO;
import com.notecastai.notecast.domain.NoteCastEntity;
import com.notecastai.tag.api.mapper.TagMapper;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        uses = {TagMapper.class}
)
public abstract class NoteCastMapper implements EntityMapper<NoteCastEntity, NoteCastResponseDTO> {

}