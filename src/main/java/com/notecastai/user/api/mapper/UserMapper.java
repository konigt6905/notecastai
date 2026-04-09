package com.notecastai.user.api.mapper;

import com.notecastai.common.EntityMapper;
import com.notecastai.user.api.dto.UserDTO;
import com.notecastai.user.domain.UserEntity;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper extends EntityMapper<UserEntity, UserDTO> {
}
