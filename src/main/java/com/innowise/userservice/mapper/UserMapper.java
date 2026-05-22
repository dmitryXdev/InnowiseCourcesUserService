package com.innowise.userservice.mapper;

import com.innowise.userservice.dto.CardDto;
import com.innowise.userservice.dto.UserDto;
import com.innowise.userservice.model.Card;
import com.innowise.userservice.model.User;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserDto toDto(User user);

    @InheritInverseConfiguration
    User toEntity(UserDto userDto);

    CardDto toDto(Card card);

    @InheritInverseConfiguration
    Card toEntity(CardDto cardDto);
}
