package com.innowise.userservice.controller;

import com.innowise.userservice.dto.CardDto;
import com.innowise.userservice.dto.CreateCardDto;
import com.innowise.userservice.dto.UpdateCardDto;
import com.innowise.userservice.security.UserPrincipal;
import com.innowise.userservice.service.CardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user-service/cards")
@RequiredArgsConstructor
public class CardController {
    private final CardService cardService;

    @PostMapping
    public ResponseEntity<CardDto> createCard(@RequestBody @Valid CreateCardDto createCardDto,
                                              @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.status(HttpStatus.CREATED).body(cardService.createCard(createCardDto, principal));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CardDto> getCardById(@PathVariable Long id, @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(cardService.getCardById(id, principal));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<CardDto> updateCardById(@PathVariable Long id, @RequestBody @Valid UpdateCardDto updateCardDto) {
        return ResponseEntity.ok(cardService.updateCardById(id, updateCardDto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCard(@PathVariable Long id, @AuthenticationPrincipal UserPrincipal principal) {
        cardService.deleteCard(id, principal);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}/activate")
    public ResponseEntity<String> activateCard(@PathVariable Long id) {
        cardService.setCardStatus(id, true);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<String> deactivateCard(@PathVariable Long id) {
        cardService.setCardStatus(id, false);
        return ResponseEntity.noContent().build();
    }
}
