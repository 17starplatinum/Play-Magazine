package com.example.backend.controllers;

import com.example.backend.dto.data.subscription.SubscriptionCreationDto;
import com.example.backend.dto.data.subscription.SubscriptionMessageDto;
import com.example.backend.dto.data.subscription.SubscriptionRequestDto;
import com.example.backend.dto.data.subscription.SubscriptionResponseDto;
import com.example.backend.model.auth.User;
import com.example.backend.model.data.subscriptions.Subscription;
import com.example.backend.repositories.data.subscription.UserSubscriptionRepository;
import com.example.backend.services.auth.UserService;
import com.example.backend.services.data.PurchaseService;
import com.example.backend.services.data.SubscriptionService;
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
    private final UserSubscriptionRepository userSubscriptionRepository;
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
    public ResponseEntity<Subscription> createSubscription(@RequestBody SubscriptionCreationDto subscriptionCreationDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(subscriptionService.createSubscription(subscriptionCreationDto));
    }

    @PostMapping("/{id}")
    public ResponseEntity<SubscriptionMessageDto> buySubscription(@PathVariable UUID id, @RequestBody SubscriptionRequestDto subscriptionRequestDto) {
        User user = userService.getCurrentUser();
        purchaseService.processSubscriptionPurchase(user, subscriptionRequestDto.getAppId(), subscriptionRequestDto.getCardId(), id);
        SubscriptionMessageDto messageDto = new SubscriptionMessageDto("Подписка успешно приобретена");
        return ResponseEntity.status(HttpStatus.CREATED).body(messageDto);
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<SubscriptionMessageDto> cancelSubscription(@PathVariable UUID id) {
        subscriptionService.cancelSubscription(id);
        SubscriptionMessageDto messageDto = new SubscriptionMessageDto("Подписка успешно отменена");
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(messageDto);
    }

    @PutMapping("/{id}/auto-renewal")
    public ResponseEntity<SubscriptionMessageDto> cancelAutoRenewalById(@PathVariable UUID id) {
        subscriptionService.cancelAutoRenewal(id);
        SubscriptionMessageDto messageDto = new SubscriptionMessageDto("Отключение авто-обновления подписки прошла успешно");
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(messageDto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<SubscriptionMessageDto> deleteSubscription(@PathVariable UUID id) {
        subscriptionService.deleteSubscription(id);
        SubscriptionMessageDto messageDto = new SubscriptionMessageDto("Подписка успешно удалена");
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(messageDto);
    }
}
