package com.innowise.userservice.service;

import com.innowise.userservice.dto.CreateCardDto;
import com.innowise.userservice.dto.CardDto;
import com.innowise.userservice.dto.UpdateCardDto;

import java.util.List;

public interface CardService {
    CardDto createCard(CreateCardDto cardDto);
    CardDto getCardById(Long id);
    List<CardDto> getAllByUserId(Long id);
    CardDto updateCardById(Long id, UpdateCardDto updateCardDto);
    void setCardStatus(Long id, boolean isActive);
    void deleteCard(Long id);
}
