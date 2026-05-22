package com.innowise.userservice.controller;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.innowise.userservice.config.TestConfig;
import com.innowise.userservice.dao.UserRepository;
import com.innowise.userservice.dto.CreateUserDto;
import com.innowise.userservice.dto.UpdateUserDto;
import com.innowise.userservice.dto.UserDto;
import com.innowise.userservice.model.PageResponse;
import com.innowise.userservice.model.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
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

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CacheManager redisTemplate;

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
     void saveUser_shouldSaveAndReturnUser() {
        CreateUserDto createUserDto = getCreateUserDto();

        try {
            UserDto userDto = objectMapper.readValue(mockMvc.perform(post("/users")
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .content(objectMapper.writeValueAsString(createUserDto)))
                    .andExpect(status().isCreated())
                    .andReturn().getResponse().getContentAsString(), UserDto.class);

            List<User> users = userRepository.findAll();
            assertEquals(1, users.size());
            assertEquals(userDto.getName(), users.get(0).getName());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
     void gertUserById_shouldCacheAndReturnUserById() {
        CreateUserDto createUserDto = getCreateUserDto();
        try {
            mockMvc.perform(post("/users")
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .content(objectMapper.writeValueAsString(createUserDto)))
                    .andExpect(status().isCreated());

            Long id = userRepository.findAll().get(0).getId();

            UserDto userDto = objectMapper.readValue(mockMvc.perform(get("/users/" + id))
                    .andExpect(status().isOk())
                    .andReturn().getResponse().getContentAsString(), UserDto.class);

            assertNotNull(userDto);
            assertEquals(userDto.getId(), id);

            userRepository.deleteById(id);

            UserDto cachedUserDto = objectMapper.readValue(mockMvc.perform(get("/users/" + id)
                            .contentType(MediaType.APPLICATION_JSON_VALUE))
                    .andExpect(status().isOk())
                    .andReturn().getResponse().getContentAsString(), UserDto.class);

            assertNotNull(cachedUserDto);
            assertEquals(cachedUserDto, userDto);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
     void updateUser_shouldUpdateAndReturnUser() {
        CreateUserDto createUserDto = getCreateUserDto();
        try {
            UserDto userDto = objectMapper.readValue(mockMvc.perform(post("/users")
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .content(objectMapper.writeValueAsString(createUserDto)))
                    .andExpect(status().isCreated())
                    .andReturn().getResponse().getContentAsString(), UserDto.class);

            UpdateUserDto updateUserDto = new UpdateUserDto();
            updateUserDto.setName("Johny");

            UserDto updatedUserDto = objectMapper.readValue(mockMvc.perform(put("/users/" + userDto.getId())
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .content(objectMapper.writeValueAsString(updateUserDto)))
                    .andExpect(status().isOk())
                    .andReturn().getResponse().getContentAsString(), UserDto.class);

            User user = userRepository.findById(userDto.getId()).orElse(null);

            assertNotNull(user);
            assertEquals("Johny", user.getName());
            assertEquals("Johny", updatedUserDto.getName());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
     void deleteUser_shouldDeleteUser() {
        CreateUserDto createUserDto = getCreateUserDto();
        try {
            UserDto userDto = objectMapper.readValue(mockMvc.perform(post("/users")
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .content(objectMapper.writeValueAsString(createUserDto)))
                    .andExpect(status().isCreated())
                    .andReturn().getResponse().getContentAsString(), UserDto.class);

            User user = userRepository.findById(userDto.getId()).orElse(null);

            assertNotNull(user);

            mockMvc.perform(delete("/users/" + userDto.getId()))
                    .andExpect(status().isOk());

            user = userRepository.findById(userDto.getId()).orElse(null);
            assertNull(user);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
     void deleteUser_shouldEraseCache() {
        CreateUserDto createUserDto = getCreateUserDto();
        try {
            UserDto userDto = objectMapper.readValue(mockMvc.perform(post("/users")
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .content(objectMapper.writeValueAsString(createUserDto)))
                    .andExpect(status().isCreated())
                    .andReturn().getResponse().getContentAsString(), UserDto.class);

            mockMvc.perform(get("/users/" + userDto.getId()))
                    .andExpect(status().isOk());

            UserDto cachedUser = (UserDto) redisTemplate.getCache("users").get(userDto.getId()).get();

            assertNotNull(cachedUser);

            mockMvc.perform(delete("/users/" + userDto.getId()))
                    .andExpect(status().isOk());

            assertNull(redisTemplate.getCache("users").get(userDto.getId()));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
     void activateAccount_shouldMakeAccountActive() {
        CreateUserDto createUserDto = getCreateUserDto();
        try {
            UserDto userDto = objectMapper.readValue(mockMvc.perform(post("/users")
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .content(objectMapper.writeValueAsString(createUserDto)))
                    .andExpect(status().isCreated())
                    .andReturn().getResponse().getContentAsString(), UserDto.class);

            assertNotNull(userDto);
            assertTrue(userDto.getActive());

            mockMvc.perform(patch("/users/" + userDto.getId() + "/deactivate"))
                    .andExpect(status().isOk());

            userDto = objectMapper.readValue(mockMvc.perform(get("/users/" + userDto.getId()))
                    .andExpect(status().isOk())
                    .andReturn().getResponse().getContentAsString(), UserDto.class);

            assertFalse(userDto.getActive());

            mockMvc.perform(patch("/users/" + userDto.getId() + "/activate"))
                    .andExpect(status().isOk());

            userDto = objectMapper.readValue(mockMvc.perform(get("/users/" + userDto.getId()))
                    .andExpect(status().isOk())
                    .andReturn().getResponse().getContentAsString(), UserDto.class);

            assertTrue(userDto.getActive());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
     void deactivateAccount_shouldMakeAccountNotActive() {
        CreateUserDto createUserDto = getCreateUserDto();
        try {
            UserDto userDto = objectMapper.readValue(mockMvc.perform(post("/users")
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .content(objectMapper.writeValueAsString(createUserDto)))
                    .andExpect(status().isCreated())
                    .andReturn().getResponse().getContentAsString(), UserDto.class);

            assertNotNull(userDto);
            assertTrue(userDto.getActive());

            mockMvc.perform(patch("/users/" + userDto.getId() + "/deactivate"))
                    .andExpect(status().isOk());

            userDto = objectMapper.readValue(mockMvc.perform(get("/users/" + userDto.getId()))
                    .andExpect(status().isOk())
                    .andReturn().getResponse().getContentAsString(), UserDto.class);

            assertFalse(userDto.getActive());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
     void getAllBySpecification_shouldReturnUsersByFilter() {
        List<CreateUserDto> createUserDtos = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            createUserDtos.add(getCreateUserDto());
        }

        try {
            for (CreateUserDto createUserDto : createUserDtos) {
                mockMvc.perform(post("/users")
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .content(objectMapper.writeValueAsString(createUserDto)))
                        .andExpect(status().isCreated())
                        .andReturn().getResponse().getContentAsString();
            }

            String name = userRepository.findAll().get(0).getName();

            JavaType type = objectMapper.getTypeFactory()
                    .constructParametricType(PageResponse.class, UserDto.class);

            PageResponse<UserDto> page = objectMapper.readValue(mockMvc.perform(get("/users")
                            .param("name", name))
                    .andExpect(status().isOk())
                    .andReturn().getResponse().getContentAsString(), type);

            List<UserDto> userDtos = page.getContent();
            for (UserDto userDto : userDtos) {
                assertEquals(name, userDto.getName());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
