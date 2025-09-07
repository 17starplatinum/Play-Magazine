package com.example.pmweb.controllers;

import com.example.pmcommon.model.Operation;
import com.example.pmweb.dto.data.purchase.PurchaseHistoryDto;
import com.example.pmcommon.mappers.MessageMapper;
import com.example.pmcommon.dto.async.PurchaseMessage;
import com.example.pmcommon.model.OperationStatus;
import com.example.pmcommon.model.OperationType;
import com.example.pmweb.services.data.OperationService;
import com.example.pmweb.services.data.PurchaseService;
import com.example.pmweb.dto.data.ResponseDto;
import com.example.pmweb.dto.data.purchase.PurchaseRequest;
import com.example.pmweb.services.publisher.MqttMessagePublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/purchases")
@RequiredArgsConstructor
public class PurchaseController {
    private final PurchaseService purchaseService;
    private final OperationService operationService;
    private final MqttMessagePublisher mqttMessagePublisher;

    @PostMapping("/buy")
    public ResponseEntity<ResponseDto> purchaseApp(@RequestBody PurchaseRequest request) {
        Operation operation = operationService.createOperation(OperationType.PURCHASE, OperationStatus.IN_PROGRESS);
        PurchaseMessage message = new PurchaseMessage(
                UUID.randomUUID().toString(),
                request.getAppID(),
                request.getCardId()
        );


        mqttMessagePublisher.publish("purchases", MessageMapper.toJson(message));
    }

    @GetMapping("/purchase-history")
    public ResponseEntity<List<PurchaseHistoryDto>> getUserPurchases() {
        return ResponseEntity.ok(purchaseService.getUserPurchases());
    }
}
