package com.example.pmweb.controllers;

import com.example.pmweb.dto.data.app.AppIdDto;
import com.example.pmweb.dto.data.subscription.SubscriptionCreationDto;
import com.example.pmweb.dto.data.subscription.SubscriptionMessageDto;
import com.example.pmweb.dto.data.subscription.SubscriptionRequestDto;
import com.example.pmweb.dto.data.subscription.SubscriptionResponseDto;
import com.example.pmweb.model.auth.User;
import com.example.pmweb.services.auth.UserService;
import com.example.pmweb.services.data.PurchaseService;
import com.example.pmweb.services.data.SubscriptionService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;


@RestController
@RequestMapping("/api/v1/subscriptions")
@AllArgsConstructor
public class SubscriptionController {
    private final UserService userService;
    private SubscriptionService subscriptionService;
    private PurchaseService purchaseService;

    @GetMapping
    public ResponseEntity<List<SubscriptionResponseDto>> getAllSubscriptions(@RequestParam(value = "appId", required = false) UUID appId) {
        return ResponseEntity.ok(subscriptionService.getSubscriptions(appId));
    }

    @GetMapping("/app")
    public ResponseEntity<List<SubscriptionResponseDto>> getAllAppSubscriptions(@RequestParam(value = "appId") UUID appId) {
        return ResponseEntity.ok(subscriptionService.getAppSubscriptions(appId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SubscriptionResponseDto> getSubscriptionById(@PathVariable UUID id) {
        return ResponseEntity.ok(subscriptionService.getSubscriptionInfo(id));
    }

    @PostMapping
    public ResponseEntity<AppIdDto> createSubscription(@RequestBody SubscriptionCreationDto subscriptionCreationDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(new AppIdDto(subscriptionService.createSubscription(subscriptionCreationDto).getId()));
    }

    @PostMapping("/buy")
    public ResponseEntity<SubscriptionMessageDto> buySubscription(@RequestBody SubscriptionRequestDto subscriptionRequestDto) {
        User user = userService.getCurrentUser();
        purchaseService.processSubscriptionPurchase(user, subscriptionRequestDto);
        SubscriptionMessageDto messageDto = new SubscriptionMessageDto("Подписка успешно приобретена");
        return ResponseEntity.status(HttpStatus.CREATED).body(messageDto);
    }

    @PutMapping("/cancel/{id}")
    public ResponseEntity<SubscriptionMessageDto> cancelSubscription(@PathVariable UUID id) {
        subscriptionService.cancelSubscription(id);
        SubscriptionMessageDto messageDto = new SubscriptionMessageDto("Subscription successfully canceled");
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(messageDto);
    }

    @PutMapping("/auto-renewal/{id}")
    public ResponseEntity<SubscriptionMessageDto> cancelAutoRenewalById(@PathVariable UUID id) {
        subscriptionService.toggleAutoRenewal(id);
        SubscriptionMessageDto messageDto = new SubscriptionMessageDto("Отключение авто-обновления подписки прошла успешно");
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(messageDto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<SubscriptionMessageDto> deleteSubscription(@PathVariable UUID id) {
        subscriptionService.deleteSubscription(id);
        SubscriptionMessageDto messageDto = new SubscriptionMessageDto("Subscription successfully removed");
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(messageDto);
    }
}
