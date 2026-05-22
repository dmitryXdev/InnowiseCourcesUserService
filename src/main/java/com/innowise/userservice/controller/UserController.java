package com.innowise.userservice.controller;

import com.innowise.userservice.dto.CreateUserDto;
import com.innowise.userservice.dto.UpdateUserDto;
import com.innowise.userservice.dto.UserDto;
import com.innowise.userservice.model.User;
import com.innowise.userservice.service.UserService;
import com.innowise.userservice.specification.UserSpecification;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping
    public ResponseEntity<Page<UserDto>> getAllBySpecification(@RequestParam(required = false) String name,
                                                @RequestParam(required = false) String surname,
                                                @RequestParam(defaultValue = "0") int page,
                                                @RequestParam(defaultValue = "10") int size,
                                                @RequestParam(defaultValue = "name", required = false) String sortBy) {
        Specification<User> specification = null;

        if(name != null) {
            specification = Specification.where(UserSpecification.hasName(name));
        }
        if(surname != null) {
            if(specification == null) {
                specification = Specification.where(UserSpecification.hasSurname(surname));
            } else {
                specification = specification.and(UserSpecification.hasSurname(surname));
            }
        }

        Page<UserDto> userDtoPage = userService.getAllBySpecification(specification, PageRequest.of(page, size, Sort.by(sortBy)));
        return ResponseEntity.ok(userDtoPage);
    }

    @PostMapping
    public ResponseEntity<UserDto> createUser(@RequestBody @Valid CreateUserDto createUserDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.saveUser(createUserDto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDto> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserDto> updateUser(@PathVariable Long id, @RequestBody @Valid UpdateUserDto updateUserDto) {
        return ResponseEntity.ok(userService.updateUserById(id, updateUserDto));
    }

    @PatchMapping("/{id}/activate")
    public ResponseEntity<String> activateAccount(@PathVariable Long id) {
        userService.setUserAccountStatus(id, true);
        return ResponseEntity.ok("Account activated");
    }

    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<String> deactivateAccount(@PathVariable Long id) {
        userService.setUserAccountStatus(id, false);
        return ResponseEntity.ok("Account deactivated");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteUserById(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok("User successfully deleted");
    }
}
