package com.notecastai.user.api.mapper;

import com.notecastai.user.api.dto.UserCreateRequest;
import com.notecastai.user.api.dto.UserUpdateRequest;
import com.notecastai.user.service.command.CreateUserCommand;
import com.notecastai.user.service.command.UpdateUserCommand;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserCommandMapper {

    CreateUserCommand toCommand(UserCreateRequest request);

    UpdateUserCommand toCommand(UserUpdateRequest request);
}
