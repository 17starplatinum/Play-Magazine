package com.example.backend.controllers;

import com.example.backend.dto.data.card.CardDto;
import com.example.backend.dto.data.card.DepositRequest;
import com.example.backend.model.data.finances.Card;
import com.example.backend.services.data.CardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/cards")
@RequiredArgsConstructor
public class CardController {
    private final CardService cardService;

    @PostMapping
    public ResponseEntity<Card> addCard(@Valid @RequestBody CardDto cardDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(cardService.addCard(cardDto));
    }

    @PutMapping("/deposit")
    public ResponseEntity<String> depositCard(@Valid @RequestBody DepositRequest depositRequest) {
        cardService.depositInCard(depositRequest);
        return ResponseEntity.ok(String.format("Your balance is now %f rubles higher", depositRequest.getAmount()));
    }

    @GetMapping
    public ResponseEntity<List<Card>> getCards() {
        return ResponseEntity.ok(cardService.getUserCards());
    }

    @PutMapping("/{cardId}")
    public ResponseEntity<String> setDefaultCard(@PathVariable UUID cardId) {
        cardService.setDefaultCard(cardId);
        return ResponseEntity.ok("This card selected by default");
    }


    @DeleteMapping("/{cardId}")
    public ResponseEntity<Void> deleteCard(@PathVariable UUID cardId) {
        cardService.deleteCard(cardId);
        return ResponseEntity.noContent().build();
    }
}
