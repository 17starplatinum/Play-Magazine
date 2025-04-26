package com.example.backend.services.data;

import com.example.backend.dto.data.subscription.SubscriptionRequestDto;
import com.example.backend.dto.data.subscription.SubscriptionResponseDto;
import com.example.backend.exceptions.notfound.SubscriptionNotFoundException;
import com.example.backend.model.auth.User;
import com.example.backend.model.data.app.App;
import com.example.backend.model.data.finances.Card;
import com.example.backend.model.data.finances.Invoice;
import com.example.backend.model.data.subscriptions.Subscription;
import com.example.backend.model.data.subscriptions.SubscriptionInfo;
import com.example.backend.repositories.data.subscription.SubscriptionInfoRepository;
import com.example.backend.repositories.data.subscription.SubscriptionRepository;
import com.example.backend.services.auth.UserService;
import com.example.backend.services.util.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SubscriptionService {
    private static final String USER_NOT_FOUND = "Подписка не найдена";
    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionInfoRepository infoRepository;
    private final NotificationService notificationService;
    private final UserService userService;
    private final CardService cardService;
    private final AppService appService;

    public List<SubscriptionResponseDto> getAllSubscriptions() {
        User user = userService.getCurrentUser();
        List<Subscription> subscriptions = subscriptionRepository.findByUser(user);
        List<SubscriptionResponseDto> subscriptionResponseDtos = new ArrayList<>();
        for (Subscription subscription : subscriptions) {
            LocalDate startDate = subscription.getSubscriptionInfo().getStartDate();
            LocalDate endDate = subscription.getSubscriptionInfo().getEndDate();

            SubscriptionResponseDto responseDto = SubscriptionResponseDto.builder()
                    .id(subscription.getId())
                    .name(subscription.getName())
                    .appName(subscription.getSubscriptionInfo().getApp().getName())
                    .daysRemaining(endDate.getDayOfMonth() - startDate.getDayOfMonth())
                    .build();

            subscriptionResponseDtos.add(responseDto);
        }
        return subscriptionResponseDtos;
    }

    @Transactional
    public Subscription addSubscription(SubscriptionRequestDto subscriptionRequestDto) {
        User user = userService.getCurrentUser();
        Card card = cardService.getCardByIdAndUser(subscriptionRequestDto.getCardId(), user);
        App app = appService.getAppByName(subscriptionRequestDto.getAppName());
        Subscription subscription = Subscription.builder()
                .user(user)
                .card(card)
                .name(subscriptionRequestDto.getName())
                .subscriptionInfo(
                        SubscriptionInfo.builder()
                                .app(app)
                                .invoice(Invoice.builder().amount(subscriptionRequestDto.getFee()).build())
                                .startDate(LocalDate.now())
                                .endDate(LocalDate.now().plusDays(subscriptionRequestDto.getDays()))
                                .autoRenewal(subscriptionRequestDto.getAutoRenewal())
                                .active(true)
                                .build()
                )
                .build();

        return subscriptionRepository.save(subscription);
    }

    public SubscriptionResponseDto getSubscriptionInfo(UUID id) {
        Subscription subscription = subscriptionRepository.findById(id).orElseThrow(() -> new SubscriptionNotFoundException(USER_NOT_FOUND));

        LocalDate startDate = subscription.getSubscriptionInfo().getStartDate();
        LocalDate endDate = subscription.getSubscriptionInfo().getEndDate();

        return SubscriptionResponseDto.builder()
                .id(subscription.getId())
                .name(subscription.getName())
                .appName(subscription.getSubscriptionInfo().getApp().getName())
                .fee(subscription.getSubscriptionInfo().getInvoice().getAmount())
                .startDate(startDate)
                .endDate(endDate)
                .daysRemaining(endDate.getDayOfMonth() - startDate.getDayOfMonth())
                .autoRenewal(subscription.getSubscriptionInfo().getAutoRenewal())
                .active(subscription.getSubscriptionInfo().getActive())
                .build();
    }

    @Transactional
    public void cancelSubscription(UUID subscriptionId) {
        User user = userService.getCurrentUser();

        Subscription subscription = subscriptionRepository.findByIdAndUser(subscriptionId, user)
                .orElseThrow(() -> new SubscriptionNotFoundException(USER_NOT_FOUND));

        SubscriptionInfo subscriptionInfo = subscription.getSubscriptionInfo();

        subscriptionInfo.setActive(false);
        subscriptionInfo.setAutoRenewal(false);

        infoRepository.save(subscriptionInfo);
        notificationService.notifyUserAboutSubscriptionCancellation(user, subscription);
    }

    @Transactional
    public void cancelAutoRenewal(UUID subscriptionId) {
        User user = userService.getCurrentUser();

        Subscription subscription = subscriptionRepository.findByIdAndUser(subscriptionId, user)
                .orElseThrow(() -> new SubscriptionNotFoundException(USER_NOT_FOUND));

        SubscriptionInfo subscriptionInfo = subscription.getSubscriptionInfo();

        subscriptionInfo.setAutoRenewal(false);
        infoRepository.save(subscriptionInfo);
        notificationService.notifyUserAboutSubscriptionAutoRenewal(user, subscription);
    }

    @Scheduled(cron = "0 0 0 * * ?")
    public void processCancelledSubscriptions() {
        LocalDate today = LocalDate.now();
        List<SubscriptionInfo> expiredSubscriptionsInfo = infoRepository.findByActiveFalseAndEndDateBefore(today);

        subscriptionRepository.deleteBySubscriptionInfoIn(expiredSubscriptionsInfo);
    }
}
