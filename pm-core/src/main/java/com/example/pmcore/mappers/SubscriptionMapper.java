package com.example.pmcore.mappers;

import com.example.backend.dto.data.subscription.SubscriptionResponseDto;
import com.example.backend.model.data.subscriptions.Subscription;
import com.example.pmweb.model.data.subscriptions.UserSubscription;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.Period;

@Component
public class SubscriptionMapper {

    public SubscriptionResponseDto mapToDtoFull(UserSubscription userSubscription, LocalDate startDate, LocalDate endDate) {
        Subscription subscription = userSubscription.getSubscription();
        return SubscriptionResponseDto.builder()
                .id(subscription.getId())
                .name(subscription.getName())
                .appName(subscription.getApp().getName())
                .fee(userSubscription.getInvoice().getAmount())
                .startDate(startDate)
                .endDate(endDate)
                .daysRemaining(Period.between(startDate, endDate).getDays())
                .autoRenewal(userSubscription.getAutoRenewal())
                .build();
    }

    public SubscriptionResponseDto mapToDtoPartial(Subscription subscription, LocalDate startDate, LocalDate endDate, double fee) {
        return SubscriptionResponseDto.builder()
                .id(subscription.getId())
                .name(subscription.getName())
                .appName(subscription.getApp().getName())
                .fee(fee)
                .daysRemaining(Period.between(startDate, endDate).getDays())
                .build();
    }

    public SubscriptionResponseDto mapToDtoShort(Subscription subscription) {
        return SubscriptionResponseDto.builder()
                .id(subscription.getId())
                .name(subscription.getName())
                .days(subscription.getDays())
                .fee(subscription.getPrice())
                .build();
    }
}
