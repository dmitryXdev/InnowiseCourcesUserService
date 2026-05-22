package com.innowise.userservice.service;

import com.innowise.userservice.dto.CreateUserDto;
import com.innowise.userservice.dto.UpdateUserDto;
import com.innowise.userservice.dto.UserDto;
import com.innowise.userservice.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

public interface UserService {
    Page<UserDto> getAllBySpecification(Specification<User> specification, Pageable pageable);
    UserDto saveUser(CreateUserDto createUserDto);
    UserDto getUserById(Long id);
    UserDto updateUserById(Long id, UpdateUserDto updateUserDto);
    void setUserAccountStatus(Long id, boolean isActive);
    void deleteUser(Long id);
}
