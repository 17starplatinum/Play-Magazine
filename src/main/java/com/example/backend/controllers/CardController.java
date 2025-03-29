package com.example.backend.controllers;

import com.example.backend.dto.data.CardDto;
import com.example.backend.model.data.Card;
import com.example.backend.services.data.CardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/cards")
@RequiredArgsConstructor
public class CardController {
    private final CardService cardService;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Card> addCard(
            @Valid @RequestBody CardDto cardDto,
            @AuthenticationPrincipal UserDetails currentUser) {
        return ResponseEntity.status(HttpStatus.CREATED).body(cardService.addCard(cardDto, currentUser));
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<Card>> getCards(@AuthenticationPrincipal UserDetails currentUser) {
        return ResponseEntity.ok(cardService.getUserCards(currentUser));
    }

    @DeleteMapping("/{cardId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deleteCard(
            @PathVariable UUID cardId,
            @AuthenticationPrincipal UserDetails currentUser) {
        cardService.deleteCard(cardId, currentUser);
        return ResponseEntity.noContent().build();
    }
}
