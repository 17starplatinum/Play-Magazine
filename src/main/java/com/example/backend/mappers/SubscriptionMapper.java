package com.example.backend.mappers;

import com.example.backend.dto.data.subscription.SubscriptionRequestDto;
import com.example.backend.dto.data.subscription.SubscriptionResponseDto;
import com.example.backend.model.auth.User;
import com.example.backend.model.data.app.App;
import com.example.backend.model.data.finances.Card;
import com.example.backend.model.data.finances.Invoice;
import com.example.backend.model.data.subscriptions.Subscription;
import com.example.backend.model.data.subscriptions.SubscriptionInfo;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class SubscriptionMapper {

    public Subscription mapToModel(User user, Card card, App app, SubscriptionRequestDto requestDto) {
        return Subscription.builder()
                .user(user)
                .card(card)
                .name(requestDto.getName())
                .subscriptionInfo(
                        mapToInfoModel(app, requestDto)
                )
                .build();
    }

    public SubscriptionInfo mapToInfoModel(App app, SubscriptionRequestDto requestDto) {
        return SubscriptionInfo.builder()
                .app(app)
                .invoice(Invoice.builder().amount(requestDto.getFee()).build())
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(requestDto.getDays()))
                .autoRenewal(requestDto.getAutoRenewal())
                .active(true)
                .build();
    }

    public Subscription mapViaInfo(User user, Card card, String name, SubscriptionInfo subscriptionInfo) {
        return Subscription.builder()
                .user(user)
                .card(card)
                .name(name)
                .subscriptionInfo(subscriptionInfo)
                .build();
    }

    public SubscriptionResponseDto mapToDtoFull(Subscription subscription, LocalDate startDate, LocalDate endDate) {
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

    public SubscriptionResponseDto mapToDtoPartial(Subscription subscription, LocalDate startDate, LocalDate endDate) {
        return SubscriptionResponseDto.builder()
                .id(subscription.getId())
                .name(subscription.getName())
                .appName(subscription.getSubscriptionInfo().getApp().getName())
                .daysRemaining(endDate.getDayOfMonth() - startDate.getDayOfMonth())
                .build();
    }
}
