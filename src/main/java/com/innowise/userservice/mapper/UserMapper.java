package com.innowise.userservice.mapper;

import com.innowise.userservice.dto.UserDto;
import com.innowise.userservice.model.User;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring",
        uses = CardMapper.class
)
public interface UserMapper {
    UserDto toDto(User user);

    @InheritInverseConfiguration
    User toEntity(UserDto userDto);
}
