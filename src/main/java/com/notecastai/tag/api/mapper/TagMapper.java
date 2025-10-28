package com.notecastai.tag.api.mapper;


import com.notecastai.common.EntityMapper;
import com.notecastai.tag.api.dto.TagDTO;
import com.notecastai.tag.domain.TagEntity;
import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TagMapper extends EntityMapper<TagEntity, TagDTO> {

    @Override
    @Mapping(source = "user.id", target = "userId")
    TagDTO toDto(TagEntity entity);

}