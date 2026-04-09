package com.notecastai.notecast.api.mapper;

import com.notecastai.notecast.api.dto.NoteCastCreateRequest;
import com.notecastai.notecast.service.command.CreateNoteCastCommand;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface NoteCastCommandMapper {

    CreateNoteCastCommand toCommand(NoteCastCreateRequest request);
}
