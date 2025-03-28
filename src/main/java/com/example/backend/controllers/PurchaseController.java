package com.example.backend.controllers;

import com.example.backend.model.data.Purchase;
import com.example.backend.services.data.PurchaseService;
import com.sun.security.auth.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController("/purchases")
@RequiredArgsConstructor
public class PurchaseController {
    private final PurchaseService purchaseService;
    @PostMapping("/purchase/{appId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Purchase> purchaseApp(
            @PathVariable UUID appId,
            @RequestParam UUID cardId,
            UserPrincipal currentUser) {
        return ResponseEntity.ok(purchaseService.purchaseApp(appId, cardId, currentUser));
    }

    public ResponseEntity<List<Purchase>> getUserPurchases(UserPrincipal currentUser) {
        return ResponseEntity.ok(purchaseService.getUserPurchases(currentUser));
    }
}
