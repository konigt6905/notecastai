package com.notecastai.tag.api.mapper;

import com.notecastai.tag.api.dto.TagCreateRequest;
import com.notecastai.tag.api.dto.TagUpdateRequest;
import com.notecastai.tag.service.command.CreateTagCommand;
import com.notecastai.tag.service.command.UpdateTagCommand;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TagCommandMapper {

    CreateTagCommand toCommand(TagCreateRequest request);

    UpdateTagCommand toCommand(TagUpdateRequest request);
}
