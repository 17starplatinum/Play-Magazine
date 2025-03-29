package com.example.backend.controllers;

import com.example.backend.model.data.Purchase;
import com.example.backend.services.data.PurchaseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/purchases")
@RequiredArgsConstructor
public class PurchaseController {
    private final PurchaseService purchaseService;
    @PostMapping("/purchase/{appId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Purchase> purchaseApp(
            @PathVariable UUID appId,
            @RequestParam UUID cardId,
            @AuthenticationPrincipal UserDetails currentUser) {
        return ResponseEntity.ok(purchaseService.purchaseApp(appId, cardId, currentUser));
    }

    public ResponseEntity<List<Purchase>> getUserPurchases(@AuthenticationPrincipal UserDetails currentUser) {
        return ResponseEntity.ok(purchaseService.getUserPurchases(currentUser));
    }
}
