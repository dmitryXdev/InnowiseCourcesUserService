package com.innowise.userservice.service;

import com.innowise.userservice.dao.UserRepository;
import com.innowise.userservice.dto.CreateUserDto;
import com.innowise.userservice.dto.UpdateUserDto;
import com.innowise.userservice.dto.UserDto;
import com.innowise.userservice.mapper.UserMapper;
import com.innowise.userservice.model.User;
import com.innowise.userservice.service.impl.UserServiceImpl;
import com.innowise.userservice.specification.UserSpecification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

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
class UserServiceTest {
    @Mock
    private UserRepository userRepository;

    private final UserMapper userMapper = Mappers.getMapper(UserMapper.class);

    private UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        userService = new UserServiceImpl(userRepository, userMapper);
    }


    private User getUser(Long id) {
        User user = new User();
        user.setActive(true);
        user.setEmail("some" + id + "@email.com");
        user.setName("John");
        user.setSurname("Doe");
        user.setBirthDate(LocalDate.of(1999, 1 , 1));
        user.setId(id);

        return user;
    }

    @Test
    void getAllBySpecification_shouldReturnAllByFilter() {
        List<User> users = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            users.add(getUser(Long.valueOf(String.valueOf(i))));
        }
        Page<User> page = new PageImpl<>(users);

        when(userRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);

        Page<UserDto> result = userService.getAllBySpecification("John", null,0, 10, "name");

        assertNotNull(result);
        assertNotNull(result.getContent());
        assertEquals(5, result.getContent().size());

        for (UserDto userDto : result.getContent()) {
            assertEquals("John", userDto.getName());
        }
    }

    @Test
    void saveUser_shouldSaveAndReturnUser() {
        User user = getUser(0L);

        when(userRepository.findUserByEmail(any(String.class))).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenReturn(user);

        CreateUserDto createUserDto = new CreateUserDto();
        createUserDto.setName(user.getName());
        createUserDto.setSurname(user.getSurname());
        createUserDto.setEmail(user.getEmail());
        createUserDto.setBirthDate(user.getBirthDate());

        UserDto userDto = userService.saveUser(createUserDto);

        assertNotNull(user);
        assertEquals(user.getName(), userDto.getName());
    }

    @Test
    void getUserById_shouldReturnUserById() {
        User user = getUser(0L);
        when(userRepository.findById(0L)).thenReturn(Optional.of(user));

        UserDto userDto = userService.getUserById(0L);

        verify(userRepository, times(1)).findById(any());
        assertNotNull(userDto);
        assertEquals(user.getName(), userDto.getName());
    }

    @Test
    void updateUserById_shouldUpdateUserById() {
        User user = getUser(0L);
        when(userRepository.findById(0L)).thenReturn(Optional.of(user));

        UpdateUserDto updateUserDto = new UpdateUserDto();
        updateUserDto.setName("Johnny");

        UserDto userDto = userService.updateUserById(user.getId(), updateUserDto);

        verify(userRepository, times(1)).findById(any());
        assertNotNull(userDto);
        assertEquals(updateUserDto.getName(), userDto.getName());
    }

    @Test
    void setAccountStatus_shouldCacheAccountStatus() {
        User user = getUser(0L);
        when(userRepository.findById(0L)).thenReturn(Optional.of(user));

        userService.setUserAccountStatus(user.getId(), false);

        verify(userRepository, times(1)).findById(any());
    }

    @Test
    void deleteUser_shouldDeleteUser() {
        User user = getUser(0L);
        when(userRepository.findById(0L)).thenReturn(Optional.of(user));

        userService.deleteUser(user.getId());

        verify(userRepository, times(1)).findById(any());
        verify(userRepository, times(1)).delete(any(User.class));
    }
}
