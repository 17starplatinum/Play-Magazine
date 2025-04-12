package com.example.backend.controllers;

import com.example.backend.dto.data.card.CardDto;
import com.example.backend.dto.data.card.DepositRequest;
import com.example.backend.model.data.finances.Card;
import com.example.backend.services.data.CardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
    public ResponseEntity<Card> addCard(@Valid @RequestBody CardDto cardDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(cardService.addCard(cardDto));
    }

    @PutMapping("/deposit/")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<String> depositCard(@Valid @RequestBody DepositRequest depositRequest) {
        cardService.depositInCard(depositRequest);
        return ResponseEntity.ok(String.format("Начислено %f рублей", depositRequest.getAmount()));
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<Card>> getCards() {
        return ResponseEntity.ok(cardService.getUserCards());
    }

    @PatchMapping("/{cardId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<String> setDefaultCard(@PathVariable UUID cardId) {
        cardService.setDefaultCard(cardId);
        return ResponseEntity.ok("Теперь эта карта применяется по умолчанию");
    }


    @DeleteMapping("/{cardId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deleteCard(@PathVariable UUID cardId) {
        cardService.deleteCard(cardId);
        return ResponseEntity.noContent().build();
    }
}
