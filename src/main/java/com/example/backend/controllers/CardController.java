package com.example.backend.controllers;

import com.example.backend.dto.data.CardDto;
import com.example.backend.model.data.Card;
import com.example.backend.services.data.CardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.nio.file.attribute.UserPrincipal;

@RestController("/cards")
@RequiredArgsConstructor
public class CardController {
    private final CardService cardService;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Card> addCard(
            @Valid @RequestBody CardDto cardDto,
            UserPrincipal currentUser) {
        return ResponseEntity.status(HttpStatus.CREATED).body(cardService.addCard(cardDto, currentUser));
    }
}
