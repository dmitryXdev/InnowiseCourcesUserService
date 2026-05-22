package com.innowise.userservice.mapper;

import com.innowise.userservice.dto.CardDto;
import com.innowise.userservice.model.Card;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CardMapper {
    @Mapping(target = "userId", source = "user.id")
    CardDto toDto(Card card);

    @InheritInverseConfiguration
    Card toEntity(CardDto cardDto);
}
