package com.smartinventorysystem.modules.auth.mapper;

import com.smartinventorysystem.modules.auth.dto.request.SignupRequest;
import com.smartinventorysystem.modules.auth.dto.response.AuthResponse;
import com.smartinventorysystem.modules.user.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AuthUserMapper {

    // Map request DTO to entity
    @Mapping(target = "userID", ignore = true)
    @Mapping(target = "passwordHash", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "activationToken", ignore = true)
    @Mapping(target = "tokenExpiry", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    User toEntity(SignupRequest request);

    @Mapping(source = "userID", target = "userId")
    @Mapping(target = "message", ignore = true)
    @Mapping(target = "token", ignore = true)
    AuthResponse toResponse(User user);
}
