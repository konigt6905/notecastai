package com.notecastai.tag.api.mapper;


import com.notecastai.common.EntityMapper;
import com.notecastai.tag.api.dto.TagDTO;
import com.notecastai.tag.domain.TagEntity;
import com.notecastai.user.api.mapper.UserMapper;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        uses = {UserMapper.class}
)
public interface TagMapper extends EntityMapper<TagEntity, TagDTO> {

    @Override
    TagDTO toDto(TagEntity entity);

}