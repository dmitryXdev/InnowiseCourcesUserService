package com.innowise.userservice.service;

import com.innowise.userservice.dto.CreateCardDto;
import com.innowise.userservice.dto.CardDto;
import com.innowise.userservice.dto.UpdateCardDto;
import com.innowise.userservice.security.UserPrincipal;

import java.util.List;

public interface CardService {
    CardDto createCard(CreateCardDto cardDto, UserPrincipal principal);

    CardDto getCardById(Long id, UserPrincipal principal);

    List<CardDto> getAllByUserId(Long id);

    CardDto updateCardById(Long id, UpdateCardDto updateCardDto);

    void setCardStatus(Long id, boolean isActive);

    void deleteCard(Long id, UserPrincipal principal);
}
