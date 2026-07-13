package com.innowise.userservice.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.innowise.userservice.config.TestConfig;
import com.innowise.userservice.dao.UserRepository;
import com.innowise.userservice.dto.CardDto;
import com.innowise.userservice.dto.CreateCardDto;
import com.innowise.userservice.dto.CreateUserDto;
import com.innowise.userservice.dto.TokenValidationResponseDto;
import com.innowise.userservice.dto.UpdateUserDto;
import com.innowise.userservice.dto.UserDto;
import com.innowise.userservice.dto.UserInfoDto;
import com.innowise.userservice.feign.AuthClient;
import com.innowise.userservice.model.PageResponse;
import com.innowise.userservice.model.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Testcontainers
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestConfig.class)
class UserControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @MockitoBean
    private AuthClient authClient;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CacheManager redisTemplate;

    private static final String token = "Bearer asfdlkashjlfkasdlkas";

    @Value("${internal.secret.header}")
    private String secretHeader;

    @Value("${internal.secret}")
    private String secret;

    @BeforeEach
    void setUpFeignClient() {
        TokenValidationResponseDto dto = TokenValidationResponseDto.builder()
                .role("ADMIN")
                .valid(true)
                .userId(0L)
                .build();

        when(authClient.validate(any())).thenReturn(dto);
    }

    private CreateUserDto getCreateUserDto() {
        LocalDate start = LocalDate.ofEpochDay(0L);
        LocalDate end = LocalDate.now();

        LocalDate birthDate = LocalDate
                .ofEpochDay(ThreadLocalRandom.current().nextLong(start.toEpochDay(), end.toEpochDay()) + 1);

        long epoch = birthDate.toEpochDay();

        CreateUserDto createUserDto = new CreateUserDto();
        createUserDto.setBirthDate(birthDate);
        createUserDto.setEmail("some" + epoch + "@email.com");
        createUserDto.setName("John" + (epoch & 4));
        createUserDto.setSurname("Doe" + (epoch & 4));
        return createUserDto;
    }

    @AfterEach
    void clearDataBase() {
        userRepository.deleteAll();
        redisTemplate.resetCaches();
    }

    @Test
    void saveUser_shouldSaveAndReturnUser() throws Exception {
        CreateUserDto createUserDto = getCreateUserDto();

        UserDto userDto = objectMapper.readValue(mockMvc.perform(post("/user-service/users")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(createUserDto)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString(), UserDto.class);

        List<User> users = userRepository.findAll();
        assertEquals(1, users.size());
        assertEquals(userDto.getName(), users.get(0).getName());

    }

    @Test
    void saveUser_shouldThrowExceptionOnUserAlreadyExists() throws Exception {
        CreateUserDto createUserDto = getCreateUserDto();

        mockMvc.perform(post("/user-service/users")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(createUserDto)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/user-service/users")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(createUserDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void gertUserById_shouldCacheAndReturnUserById() throws Exception {
        CreateUserDto createUserDto = getCreateUserDto();

        mockMvc.perform(post("/user-service/users")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(createUserDto)))
                .andExpect(status().isCreated());

        Long id = userRepository.findAll().get(0).getId();

        UserDto userDto = objectMapper.readValue(mockMvc.perform(get("/user-service/users/" + id)
                        .header(HttpHeaders.AUTHORIZATION, token))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(), UserDto.class);

        assertNotNull(userDto);
        assertEquals(userDto.getId(), id);

        userRepository.deleteById(id);

        UserDto cachedUserDto = objectMapper.readValue(mockMvc.perform(get("/user-service/users/" + id)
                        .header(HttpHeaders.AUTHORIZATION, token)
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(), UserDto.class);

        assertNotNull(cachedUserDto);
        assertEquals(cachedUserDto, userDto);
    }

    @Test
    void getUserById_shouldThrowExceptionOnNotExistingUser() throws Exception {
        mockMvc.perform(get("/user-service/users/0")
                        .header(HttpHeaders.AUTHORIZATION, token))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateUser_shouldUpdateAndReturnUser() throws Exception {
        CreateUserDto createUserDto = getCreateUserDto();

        UserDto userDto = objectMapper.readValue(mockMvc.perform(post("/user-service/users")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(createUserDto)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString(), UserDto.class);

        UpdateUserDto updateUserDto = new UpdateUserDto();
        updateUserDto.setName("Johny");
        updateUserDto.setSurname("Surname");
        updateUserDto.setBirthDate(LocalDate.now().minusDays(1L));

        UserDto updatedUserDto = objectMapper.readValue(mockMvc.perform(put("/user-service/users/" + userDto.getId())
                        .header(HttpHeaders.AUTHORIZATION, token)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(updateUserDto)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(), UserDto.class);

        User user = userRepository.findById(userDto.getId()).orElse(null);

        assertNotNull(user);
        assertEquals(user.getName(), updatedUserDto.getName());
        assertEquals(user.getSurname(), updatedUserDto.getSurname());
        assertEquals(user.getBirthDate(), updatedUserDto.getBirthDate());
    }

    @Test
    void deleteUser_shouldDeleteUser() throws Exception {
        CreateUserDto createUserDto = getCreateUserDto();

        UserDto userDto = objectMapper.readValue(mockMvc.perform(post("/user-service/users")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(createUserDto)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString(), UserDto.class);

        User user = userRepository.findById(userDto.getId()).orElse(null);

        assertNotNull(user);

        mockMvc.perform(delete("/user-service/users/" + userDto.getId())
                        .header(HttpHeaders.AUTHORIZATION, token))
                .andExpect(status().isNoContent());

        user = userRepository.findById(userDto.getId()).orElse(null);
        assertNull(user);
    }

    @Test
    void deleteUser_shouldEraseCache() throws Exception {
        CreateUserDto createUserDto = getCreateUserDto();

        UserDto userDto = objectMapper.readValue(mockMvc.perform(post("/user-service/users")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(createUserDto)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString(), UserDto.class);

        mockMvc.perform(get("/user-service/users/" + userDto.getId())
                        .header(HttpHeaders.AUTHORIZATION, token))
                .andExpect(status().isOk());

        UserDto cachedUser = (UserDto) redisTemplate.getCache("users").get(userDto.getId()).get();

        assertNotNull(cachedUser);

        mockMvc.perform(delete("/user-service/users/" + userDto.getId())
                        .header(HttpHeaders.AUTHORIZATION, token))
                .andExpect(status().isNoContent());

        assertNull(redisTemplate.getCache("users").get(userDto.getId()));
    }

    @Test
    void getAllCardsByUserId_shouldReturnAllCardsByUserId() throws Exception {
        CreateUserDto createUserDto = getCreateUserDto();

        UserDto userDto = objectMapper.readValue(mockMvc.perform(post("/user-service/users")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(createUserDto)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString(), UserDto.class);

        CreateCardDto card1 = new CreateCardDto();
        card1.setHolder("INSTANT CARD");
        card1.setExpirationDate(LocalDate.now().plusDays(12L));
        card1.setNumber("2342345342345435");
        card1.setUserId(userDto.getId());

        CreateCardDto card2 = new CreateCardDto();
        card2.setHolder("INSTANT CARD");
        card2.setExpirationDate(LocalDate.now().plusDays(12L));
        card2.setNumber("2342341231332132");
        card2.setUserId(userDto.getId());


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

        List<CardDto> usersCards = objectMapper.readValue(mockMvc.perform(get("/user-service/users/" + userDto.getId() + "/cards")
                        .header(HttpHeaders.AUTHORIZATION, token))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(), new TypeReference<>() {
        });

        assertNotNull(usersCards);
        assertEquals(2, usersCards.size());
    }

    @Test
    void activateAccount_shouldMakeAccountActive() throws Exception {
        CreateUserDto createUserDto = getCreateUserDto();

        UserDto userDto = objectMapper.readValue(mockMvc.perform(post("/user-service/users")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(createUserDto)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString(), UserDto.class);

        assertNotNull(userDto);
        assertTrue(userDto.getActive());

        mockMvc.perform(patch("/user-service/users/" + userDto.getId() + "/deactivate")
                        .header(HttpHeaders.AUTHORIZATION, token))
                .andExpect(status().isNoContent());

        userDto = objectMapper.readValue(mockMvc.perform(get("/user-service/users/" + userDto.getId())
                        .header(HttpHeaders.AUTHORIZATION, token))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(), UserDto.class);

        assertFalse(userDto.getActive());

        mockMvc.perform(patch("/user-service/users/" + userDto.getId() + "/activate")
                        .header(HttpHeaders.AUTHORIZATION, token))
                .andExpect(status().isNoContent());

        userDto = objectMapper.readValue(mockMvc.perform(get("/user-service/users/" + userDto.getId())
                        .header(HttpHeaders.AUTHORIZATION, token))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(), UserDto.class);

        assertTrue(userDto.getActive());
    }

    @Test
    void deactivateAccount_shouldMakeAccountNotActive() throws Exception {
        CreateUserDto createUserDto = getCreateUserDto();

        UserDto userDto = objectMapper.readValue(mockMvc.perform(post("/user-service/users")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(createUserDto)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString(), UserDto.class);

        assertNotNull(userDto);
        assertTrue(userDto.getActive());

        mockMvc.perform(patch("/user-service/users/" + userDto.getId() + "/deactivate")
                        .header(HttpHeaders.AUTHORIZATION, token))
                .andExpect(status().isNoContent());

        userDto = objectMapper.readValue(mockMvc.perform(get("/user-service/users/" + userDto.getId())
                        .header(HttpHeaders.AUTHORIZATION, token))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(), UserDto.class);

        assertFalse(userDto.getActive());
    }

    @Test
    void getAllBySpecification_shouldReturnUsersByFilter() throws Exception {
        List<CreateUserDto> createUserDtos = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            createUserDtos.add(getCreateUserDto());
        }

        for (CreateUserDto createUserDto : createUserDtos) {
            mockMvc.perform(post("/user-service/users")
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .content(objectMapper.writeValueAsString(createUserDto)))
                    .andExpect(status().isCreated())
                    .andReturn().getResponse().getContentAsString();
        }

        String name = userRepository.findAll().get(0).getName();

        JavaType type = objectMapper.getTypeFactory()
                .constructParametricType(PageResponse.class, UserDto.class);

        PageResponse<UserDto> page = objectMapper.readValue(mockMvc.perform(get("/user-service/users")
                        .header(HttpHeaders.AUTHORIZATION, token)
                        .param("name", name))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(), type);

        List<UserDto> userDtos = page.getContent();
        for (UserDto userDto : userDtos) {
            assertEquals(name, userDto.getName());
        }
    }

    @Test
    void getUserInfoById_shouldReturnUserInfoByUserId() throws Exception {
        CreateUserDto createUserDto = getCreateUserDto();

        UserDto userDto = objectMapper.readValue(mockMvc.perform(post("/user-service/users")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(createUserDto)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString(), UserDto.class);

        UserInfoDto userInfoDto = objectMapper.readValue(mockMvc.perform(get("/user-service/users/" + userDto.getId() + "/info")
                .header(secretHeader, secret))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(), UserInfoDto.class);

        assertNotNull(userInfoDto);
        assertNotNull(userInfoDto.getId());
        assertNotNull(userInfoDto.getName());
        assertNotNull(userInfoDto.getEmail());
        assertNotNull(userInfoDto.getSurname());
    }
}
