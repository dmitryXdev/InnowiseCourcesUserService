package com.innowise.userservice.controller;

import com.innowise.userservice.dto.CardDto;
import com.innowise.userservice.dto.CreateUserDto;
import com.innowise.userservice.dto.UpdateUserDto;
import com.innowise.userservice.dto.UserDto;
import com.innowise.userservice.service.CardService;
import com.innowise.userservice.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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

import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final CardService cardService;

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<Page<UserDto>> getAllBySpecification(@RequestParam(required = false) String name,
                                                @RequestParam(required = false) String surname,
                                                @RequestParam(defaultValue = "0") int page,
                                                @RequestParam(defaultValue = "10") int size,
                                                @RequestParam(defaultValue = "name", required = false) String sortBy) {

        return ResponseEntity.ok(userService.getAllBySpecification(name, surname, page, size, sortBy));
    }

    @PostMapping
    public ResponseEntity<UserDto> createUser(@RequestBody @Valid CreateUserDto createUserDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.saveUser(createUserDto));
    }

    @PreAuthorize("hasRole('ADMIN') || #userId == authentication.principal.id")
    @GetMapping("/{id}/cards")
    public ResponseEntity<List<CardDto>> getAllCardsByUserId(@PathVariable Long id) {
        return ResponseEntity.ok(cardService.getAllByUserId(id));
    }

    @PreAuthorize("hasRole('ADMIN') || #id == authentication.principal.id")
    @GetMapping("/{id}")
    public ResponseEntity<UserDto> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @PreAuthorize("hasRole('ADMIN') || #id == authentication.principal.id")
    @PutMapping("/{id}")
    public ResponseEntity<UserDto> updateUser(@PathVariable Long id, @RequestBody @Valid UpdateUserDto updateUserDto) {
        return ResponseEntity.ok(userService.updateUserById(id, updateUserDto));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}/activate")
    public ResponseEntity<String> activateAccount(@PathVariable Long id) {
        userService.setUserAccountStatus(id, true);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<String> deactivateAccount(@PathVariable Long id) {
        userService.setUserAccountStatus(id, false);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUserById(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}
