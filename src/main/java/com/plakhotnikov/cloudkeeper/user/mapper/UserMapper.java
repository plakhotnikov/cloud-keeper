package com.plakhotnikov.cloudkeeper.user.mapper;

import com.plakhotnikov.cloudkeeper.user.model.User;
import com.plakhotnikov.cloudkeeper.user.model.UserDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface UserMapper {

    @Mapping(target = "id", ignore = true)
    User mapToEntity(UserDto userDto);

}
