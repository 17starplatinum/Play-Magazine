package com.example.pmweb.controllers;

import com.example.pmweb.dto.async.OperationStatusResponse;
import com.example.pmcommon.model.Operation;
import com.example.pmweb.services.data.OperationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/operations")
@RequiredArgsConstructor
public class OperationController {
    private final OperationService operationService;

    @GetMapping("/status/{operationId}")
    public ResponseEntity<OperationStatusResponse> getOperationById(@PathVariable("operationId") UUID operationId) {
        Operation operation = operationService.getOperationById(operationId);
        OperationStatusResponse response = OperationStatusResponse.builder()
                .status(operation.getStatus())
                .lastUpdated(operation.getUpdatedAt())
                .details(operation.getDetails())
                .build();
        return ResponseEntity.ok(response);
    }
}
