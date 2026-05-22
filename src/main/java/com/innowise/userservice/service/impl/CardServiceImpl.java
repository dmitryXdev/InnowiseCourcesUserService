package com.innowise.userservice.service.impl;

import com.innowise.userservice.dao.CardRepository;
import com.innowise.userservice.dao.UserRepository;
import com.innowise.userservice.dto.CardDto;
import com.innowise.userservice.dto.CreateCardDto;
import com.innowise.userservice.dto.UpdateCardDto;
import com.innowise.userservice.exception.AccountIsNotActivatedException;
import com.innowise.userservice.exception.BadIncomeDataException;
import com.innowise.userservice.exception.EntityNotFoundException;
import com.innowise.userservice.mapper.CardMapper;
import com.innowise.userservice.model.Card;
import com.innowise.userservice.model.User;
import com.innowise.userservice.service.CardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CardServiceImpl implements CardService {
    private final CardRepository cardRepository;
    private final UserRepository userRepository;
    private final CardMapper cardMapper;
    private static final String CARD_NOT_FOUND_MESSAGE = "Card not found";


    @Override
    @Transactional
    public CardDto createCard(CreateCardDto createCardDto) {
        User user = userRepository.findById(createCardDto.getUserId()).orElseThrow(() -> new EntityNotFoundException("User not found"));

        if(!user.isActive()) {
            throw new AccountIsNotActivatedException("User account is not activated");
        }

        if(user.getCards() != null && user.getCards().size() > 5) {
            throw new BadIncomeDataException("User can not have more than 5 cards");
        }

        Card card = new Card();
        card.setActive(true);
        card.setExpirationDate(createCardDto.getExpirationDate());
        card.setHolder(createCardDto.getHolder());
        card.setNumber(createCardDto.getNumber());
        card.setUser(user);

        return cardMapper.toDto(cardRepository.save(card));
    }

    @Override
    public CardDto getCardById(Long id) {
        Card card = cardRepository.findById(id).orElseThrow(() -> new EntityNotFoundException(CARD_NOT_FOUND_MESSAGE));
        return cardMapper.toDto(card);
    }

    @Override
    public List<CardDto> getAllByUserId(Long id) {
        List<Card> cards = userRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("No such user")).getCards();
        return cards.stream()
                .map(cardMapper::toDto)
                .toList();
    }

    @Override
    @Transactional
    public CardDto updateCardById(Long id, UpdateCardDto updateCardDto) {
        Card card = cardRepository.findById(id).orElseThrow(() -> new EntityNotFoundException(CARD_NOT_FOUND_MESSAGE));

        if(updateCardDto.getNumber() != null) {
            card.setNumber(updateCardDto.getNumber());
        }

        if(updateCardDto.getExpirationDate() != null) {
            card.setExpirationDate(updateCardDto.getExpirationDate());
        }

        return cardMapper.toDto(card);
    }

    @Override
    @Transactional
    public void deleteCard(Long id) {
        Card card = cardRepository.findById(id).orElseThrow(() -> new EntityNotFoundException(CARD_NOT_FOUND_MESSAGE));
        cardRepository.delete(card);
    }

    @Override
    @Transactional
    public void setCardStatus(Long id, boolean isActive) {
        Card card = cardRepository.findById(id).orElseThrow(() -> new EntityNotFoundException(CARD_NOT_FOUND_MESSAGE));
        card.setActive(isActive);
    }
}
