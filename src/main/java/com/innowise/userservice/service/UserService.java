package com.innowise.userservice.service;

import com.innowise.userservice.dto.CreateUserDto;
import com.innowise.userservice.dto.UpdateUserDto;
import com.innowise.userservice.dto.UserDto;
import org.springframework.data.domain.Page;

public interface UserService {
    Page<UserDto> getAllBySpecification(String name, String surname, int page, int size, String sortBy);
    UserDto saveUser(CreateUserDto createUserDto);
    UserDto getUserById(Long id);
    UserDto updateUserById(Long id, UpdateUserDto updateUserDto);
    void setUserAccountStatus(Long id, boolean isActive);
    void deleteUser(Long id);
}
