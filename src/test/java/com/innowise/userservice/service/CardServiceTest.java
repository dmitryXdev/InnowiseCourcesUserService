package com.innowise.userservice.service;

import com.innowise.userservice.dao.CardRepository;
import com.innowise.userservice.dao.UserRepository;
import com.innowise.userservice.dto.CardDto;
import com.innowise.userservice.dto.CreateCardDto;
import com.innowise.userservice.dto.UpdateCardDto;
import com.innowise.userservice.mapper.CardMapper;
import com.innowise.userservice.model.Card;
import com.innowise.userservice.model.User;
import com.innowise.userservice.service.impl.CardServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
 class CardServiceTest {
    @Mock
    private CardRepository cardRepository;

    @Mock
    private UserRepository userRepository;

    private final CardMapper cardMapper = Mappers.getMapper(CardMapper.class);

    private CardServiceImpl cardService;

    @BeforeEach
    void setUp() {
        cardService = new CardServiceImpl(cardRepository, userRepository, cardMapper);
    }

    private Card getCard(Long id) {
        Card card = new Card();
        card.setId(id);
        card.setNumber("234355533213456" + id);
        card.setActive(true);
        card.setHolder("INSTANT CARD");
        card.setExpirationDate(LocalDate.of(2027, 1,1));

        return card;
    }

    @Test
    void createCard_shouldCreateAndReturnCard() {
        Card card = getCard(0L);

        User user = new User();
        user.setId(0L);
        user.setActive(true);

        when(cardRepository.save(any(Card.class))).thenReturn(card);
        when(userRepository.findById(any())).thenReturn(Optional.of(user));

        CreateCardDto createCardDto = new CreateCardDto();
        createCardDto.setUserId(user.getId());
        createCardDto.setHolder(card.getHolder());
        createCardDto.setNumber(card.getNumber());
        createCardDto.setExpirationDate(card.getExpirationDate());

        CardDto cardDto = cardService.createCard(createCardDto);

        assertNotNull(cardDto);
        assertEquals(createCardDto.getNumber(), cardDto.getNumber());
    }

    @Test
    void getCardById_shouldReturnCardById() {
        Card card = getCard(0L);
        when(cardRepository.findById(0L)).thenReturn(Optional.of(card));

        CardDto cardDto = cardService.getCardById(card.getId());

        assertNotNull(cardDto);
        assertEquals(card.getNumber(), cardDto.getNumber());
    }

    @Test
    void getAllByUserId_shouldReturnAllCardsByUserId() {
        User user = new User();
        user.setId(0L);

        List<Card> cards = new ArrayList<>();
        cards.add(getCard(0L));
        cards.add(getCard(1L));

        user.setCards(cards);

        when(userRepository.findById(0L)).thenReturn(Optional.of(user));

        List<CardDto> cardDtos = cardService.getAllByUserId(user.getId());

        assertNotNull(cardDtos);
        assertEquals(2, cardDtos.size());
    }

    @Test
    void updateCardById_shouldUpdateAndReturnCardById() {
        Card card = getCard(0L);

        when(cardRepository.findById(0L)).thenReturn(Optional.of(card));

        UpdateCardDto updateCardDto = new UpdateCardDto();
        updateCardDto.setNumber("2333555332134561");

        CardDto cardDto = cardService.updateCardById(card.getId(), updateCardDto);

        assertNotNull(cardDto);
        assertEquals(updateCardDto.getNumber(), cardDto.getNumber());
    }

    @Test
    void deleteCard_shouldDeleteCardById() {
        Card card = getCard(0L);

        when(cardRepository.findById(0L)).thenReturn(Optional.of(card));

        cardService.deleteCard(card.getId());

        verify(cardRepository, times(1)).delete(any(Card.class));
    }

    @Test
    void setCardStatus_shouldChangeCardStatusByCardId() {
        Card card = getCard(0L);

        when(cardRepository.findById(0L)).thenReturn(Optional.of(card));

        cardService.setCardStatus(card.getId(), false);

        verify(cardRepository, times(1)).findById(any(Long.class));
    }
}
