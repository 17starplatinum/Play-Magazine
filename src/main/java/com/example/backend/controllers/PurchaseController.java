package com.example.backend.controllers;

import com.example.backend.dto.data.purchase.PurchaseRequest;
import com.example.backend.model.data.finances.Purchase;
import com.example.backend.services.data.PurchaseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/purchases")
@RequiredArgsConstructor
public class PurchaseController {
    private final PurchaseService purchaseService;


    @PostMapping("/purchase")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Purchase> purchaseApp(@RequestBody PurchaseRequest purchaseRequest) {
        return ResponseEntity.ok(purchaseService.purchaseApp(purchaseRequest));
    }

    @GetMapping("/purchase-history")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<Purchase>> getUserPurchases() {
        return ResponseEntity.ok(purchaseService.getUserPurchases());
    }
}
