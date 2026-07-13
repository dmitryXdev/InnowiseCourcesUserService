package com.innowise.userservice.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.innowise.userservice.config.TestConfig;
import com.innowise.userservice.dao.CardRepository;
import com.innowise.userservice.dao.UserRepository;
import com.innowise.userservice.dto.CardDto;
import com.innowise.userservice.dto.CreateCardDto;
import com.innowise.userservice.dto.TokenValidationResponseDto;
import com.innowise.userservice.dto.UpdateCardDto;
import com.innowise.userservice.feign.AuthClient;
import com.innowise.userservice.security.filter.JwtAuthFilter;
import com.innowise.userservice.model.Card;
import com.innowise.userservice.model.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.mockito.Mockito.when;

@SpringBootTest
@Testcontainers
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestConfig.class)
@ExtendWith(MockitoExtension.class)
class CardControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthClient authClient;

    @Autowired
    private JwtAuthFilter jwtAuthFilter;

    @Autowired
    private CardRepository cardRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CacheManager redisTemplate;

    private static final String token = "Bearer asfdlkashjlfkasdlkas";

    @BeforeEach
    void setUpFeignClient() {
        TokenValidationResponseDto dto = TokenValidationResponseDto.builder()
                .role("ADMIN")
                .valid(true)
                .userId(0L)
                .build();

        when(authClient.validate(any())).thenReturn(dto);
    }

    private User addUserToDB() {
        User user = new User();
        user.setActive(true);
        user.setName("John");
        user.setSurname("Doe");
        user.setEmail("some@email.com");
        user.setBirthDate(LocalDate.of(1990, 1, 1));

        return userRepository.save(user);
    }

    private CreateCardDto getCreateCardDto(Long userId, String number) {
        CreateCardDto createCardDto = new CreateCardDto();
        createCardDto.setHolder("INSTANT CARD");
        createCardDto.setExpirationDate(LocalDate.now().plusDays(12L));
        createCardDto.setNumber(number);
        createCardDto.setUserId(userId);

        return createCardDto;
    }

    @AfterEach
    void clearDataBase() {
        userRepository.deleteAll();
        cardRepository.deleteAll();
    }

    @Test
    void createCard_shouldCreateAndReturnCard() throws Exception {
        User user = addUserToDB();
        CreateCardDto createCardDto = getCreateCardDto(user.getId(), "2342345342345435");

        CardDto cardDto = objectMapper.readValue(mockMvc.perform(post("/user-service/cards")
                        .header(HttpHeaders.AUTHORIZATION, token)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(createCardDto)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString(), CardDto.class);

        assertNotNull(cardDto);
        assertEquals(createCardDto.getNumber(), cardDto.getNumber());
    }

    @Test
    void createCard_shouldThrowExceptionOnAccessToForbiddenResource() throws Exception {
        User user = addUserToDB();

        CreateCardDto createCardDto = getCreateCardDto(user.getId(), "2342345342345435");

        TokenValidationResponseDto dto = TokenValidationResponseDto.builder()
                .role("USER")
                .valid(true)
                .userId(10L)
                .build();

        when(authClient.validate(any())).thenReturn(dto);

        mockMvc.perform(post("/user-service/cards")
                        .header(HttpHeaders.AUTHORIZATION, token)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(createCardDto)))
                .andExpect(status().isForbidden());
    }

    @Test
    void createUser_shouldThrowExceptionOnNotActivatedUser() throws Exception {
        User user = new User();
        user.setActive(false);
        user.setSurname("surname");
        user.setName("name");
        user.setBirthDate(LocalDate.now().minusDays(1L));
        user.setEmail("some@email.com");

        userRepository.save(user);

        CreateCardDto createCardDto = getCreateCardDto(user.getId(), "2342345342345435");

        mockMvc.perform(post("/user-service/cards")
                        .header(HttpHeaders.AUTHORIZATION, token)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(createCardDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getCardById_shouldReturnCardById() throws Exception {
        User user = addUserToDB();
        CreateCardDto createCardDto = getCreateCardDto(user.getId(), "2342345342345435");

        CardDto cardDto = objectMapper.readValue(mockMvc.perform(post("/user-service/cards")
                        .header(HttpHeaders.AUTHORIZATION, token)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(createCardDto)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString(), CardDto.class);

        assertNotNull(cardDto);
        assertEquals(createCardDto.getNumber(), cardDto.getNumber());

        cardDto = objectMapper.readValue(mockMvc.perform(get("/user-service/cards/" + cardDto.getId())
                        .header(HttpHeaders.AUTHORIZATION, token))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(), CardDto.class);

        assertNotNull(cardDto);
        assertEquals(createCardDto.getNumber(), cardDto.getNumber());
    }

    @Test
    void getAllByUserId_shouldReturnAllCardsByUserId() throws Exception {
        User user = addUserToDB();
        CreateCardDto card1 = getCreateCardDto(user.getId(), "2342345342345435");
        CreateCardDto card2 = getCreateCardDto(user.getId(), "2342341231332132");

        CardDto cardDto1 = objectMapper.readValue(mockMvc.perform(post("/user-service/cards")
                        .header(HttpHeaders.AUTHORIZATION, token)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(card1)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString(), CardDto.class);

        CardDto cardDto2 = objectMapper.readValue(mockMvc.perform(post("/user-service/cards")
                        .header(HttpHeaders.AUTHORIZATION, token)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(card2)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString(), CardDto.class);

        assertNotNull(cardDto1);
        assertNotNull(cardDto2);

        List<CardDto> usersCards = objectMapper.readValue(mockMvc.perform(get("/user-service/users/" + user.getId() + "/cards")
                        .header(HttpHeaders.AUTHORIZATION, token))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(), new TypeReference<>() {
        });

        assertNotNull(usersCards);
        assertEquals(2, usersCards.size());
    }

    @Test
    void updateCardById_shouldUpdateAndReturnCard() throws Exception {
        User user = addUserToDB();
        CreateCardDto createCardDto = getCreateCardDto(user.getId(), "2342341231332132");

        CardDto cardDto = objectMapper.readValue(mockMvc.perform(post("/user-service/cards")
                        .header(HttpHeaders.AUTHORIZATION, token)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(createCardDto)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString(), CardDto.class);

        UpdateCardDto updateCardDto = new UpdateCardDto();
        updateCardDto.setNumber("2342345342345435");

        cardDto = objectMapper.readValue(mockMvc.perform(put("/user-service/cards/" + cardDto.getId())
                        .header(HttpHeaders.AUTHORIZATION, token)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(updateCardDto)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(), CardDto.class);

        assertNotNull(cardDto);
        assertEquals(updateCardDto.getNumber(), cardDto.getNumber());
    }

    @Test
    void deleteCard_shouldDeleteCard() throws Exception {
        User user = addUserToDB();
        CreateCardDto createCardDto = getCreateCardDto(user.getId(), "2342341231332132");

        CardDto cardDto = objectMapper.readValue(mockMvc.perform(post("/user-service/cards")
                        .header(HttpHeaders.AUTHORIZATION, token)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(createCardDto)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString(), CardDto.class);

        mockMvc.perform(delete("/user-service/cards/" + cardDto.getId())
                        .header(HttpHeaders.AUTHORIZATION, token))
                .andExpect(status().isNoContent());

        Card card = cardRepository.findById(cardDto.getId()).orElse(null);

        assertNull(card);
    }

    @Test
    void activateCard_shouldChangeCardStatusToActive() throws Exception {
        User user = addUserToDB();
        CreateCardDto createCardDto = getCreateCardDto(user.getId(), "2342341231332132");

        CardDto cardDto = objectMapper.readValue(mockMvc.perform(post("/user-service/cards")
                        .header(HttpHeaders.AUTHORIZATION, token)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(createCardDto)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString(), CardDto.class);

        Card card = cardRepository.findById(cardDto.getId()).orElse(null);
        assertNotNull(card);
        card.setActive(false);
        cardRepository.save(card);

        mockMvc.perform(patch("/user-service/cards/" + cardDto.getId() + "/activate")
                        .header(HttpHeaders.AUTHORIZATION, token))
                .andExpect(status().isNoContent());

        cardDto = objectMapper.readValue(mockMvc.perform(get("/user-service/cards/" + cardDto.getId())
                        .header(HttpHeaders.AUTHORIZATION, token))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(), CardDto.class);

        assertTrue(cardDto.getActive());
    }

    @Test
    void deactivateCard_shouldChangeCardStatusToNotActive() throws Exception {
        User user = addUserToDB();
        CreateCardDto createCardDto = getCreateCardDto(user.getId(), "2342341231332132");

        CardDto cardDto = objectMapper.readValue(mockMvc.perform(post("/user-service/cards")
                        .header(HttpHeaders.AUTHORIZATION, token)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(createCardDto)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString(), CardDto.class);

        mockMvc.perform(patch("/user-service/cards/" + cardDto.getId() + "/deactivate")
                        .header(HttpHeaders.AUTHORIZATION, token))
                .andExpect(status().isNoContent());

        cardDto = objectMapper.readValue(mockMvc.perform(get("/user-service/cards/" + cardDto.getId())
                        .header(HttpHeaders.AUTHORIZATION, token))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(), CardDto.class);

        assertFalse(cardDto.getActive());
    }
}
