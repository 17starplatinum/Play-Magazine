package com.example.pmcore.controllers;

import com.example.pmcore.dto.data.purchase.PurchaseHistoryDto;
import com.example.pmcore.services.data.PurchaseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/purchases")
@RequiredArgsConstructor
public class PurchaseController {
    private final PurchaseService purchaseService;

    @GetMapping("/purchase-history")
    public ResponseEntity<List<PurchaseHistoryDto>> getUserPurchases() {
        return ResponseEntity.ok(purchaseService.getUserPurchases());
    }
}
