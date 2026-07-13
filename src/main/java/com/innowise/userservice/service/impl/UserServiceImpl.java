package com.innowise.userservice.service.impl;

import com.innowise.userservice.dao.UserRepository;
import com.innowise.userservice.dto.CreateUserDto;
import com.innowise.userservice.dto.UpdateUserDto;
import com.innowise.userservice.dto.UserDto;
import com.innowise.userservice.dto.UserInfoDto;
import com.innowise.userservice.exception.BadIncomeDataException;
import com.innowise.userservice.exception.EntityNotFoundException;
import com.innowise.userservice.mapper.UserMapper;
import com.innowise.userservice.model.User;
import com.innowise.userservice.service.UserService;
import com.innowise.userservice.specification.UserSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    private static final String USER_NOT_FOUND_MESSAGE = "User not found";

    @Override
    public Page<UserDto> getAllBySpecification(String name, String surname, int page, int size, String sortBy) {
        Specification<User> specification = null;

        if (name != null) {
            specification = Specification.where(UserSpecification.hasName(name));
        }
        if (surname != null) {
            if (specification == null) {
                specification = Specification.where(UserSpecification.hasSurname(surname));
            } else {
                specification = specification.and(UserSpecification.hasSurname(surname));
            }
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy));

        return userRepository.findAll(specification, pageable).map(userMapper::toDto);
    }

    @Override
    @Transactional
    public UserDto saveUser(CreateUserDto createUserDto) {
        User user = userRepository.findUserByEmail(createUserDto.getEmail()).orElse(null);
        if (user != null) {
            throw new BadIncomeDataException("User already exists");
        }

        user = new User();
        user.setEmail(createUserDto.getEmail());
        user.setName(createUserDto.getName());
        user.setSurname(createUserDto.getSurname());
        user.setBirthDate(createUserDto.getBirthDate());
        user.setActive(true);

        return userMapper.toDto(userRepository.save(user));
    }

    @Override
    public UserInfoDto getUserInfoById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(USER_NOT_FOUND_MESSAGE));

        return UserInfoDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .surname(user.getSurname())
                .build();
    }

    @Override
    @Cacheable(value = "users", key = "#id")
    public UserDto getUserById(Long id) {
        return userMapper.toDto(userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(USER_NOT_FOUND_MESSAGE)));
    }

    @Override
    @Transactional
    @CacheEvict(value = "users", key = "#id")
    public UserDto updateUserById(Long id, UpdateUserDto updateUserDto) {
        if (updateUserDto == null) {
            throw new BadIncomeDataException("No data presented");
        }

        User user = userRepository.findById(id).orElseThrow(() -> new EntityNotFoundException(USER_NOT_FOUND_MESSAGE));

        if (updateUserDto.getName() != null) {
            user.setName(updateUserDto.getName());
        }
        if (updateUserDto.getBirthDate() != null) {
            user.setBirthDate(updateUserDto.getBirthDate());
        }
        if (updateUserDto.getSurname() != null) {
            user.setSurname(updateUserDto.getSurname());
        }

        return userMapper.toDto(user);
    }

    @Override
    @Transactional
    @CacheEvict(value = "users", key = "#id")
    public void setUserAccountStatus(Long id, boolean isActive) {
        User user = userRepository.findById(id).orElseThrow(() -> new EntityNotFoundException(USER_NOT_FOUND_MESSAGE));
        user.setActive(isActive);
    }

    @Override
    @Transactional
    @CacheEvict(value = "users", key = "#id")
    public void deleteUser(Long id) {
        User user = userRepository.findById(id).orElseThrow(() -> new EntityNotFoundException(USER_NOT_FOUND_MESSAGE));
        userRepository.delete(user);
    }
}
