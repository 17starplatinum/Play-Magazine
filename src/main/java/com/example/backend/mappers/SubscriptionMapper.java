package com.example.backend.mappers;

import com.example.backend.dto.data.subscription.SubscriptionCreationDto;
import com.example.backend.dto.data.subscription.SubscriptionResponseDto;
import com.example.backend.model.data.finances.Invoice;
import com.example.backend.model.data.subscriptions.Subscription;
import com.example.backend.model.data.subscriptions.UserSubscription;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class SubscriptionMapper {

    public UserSubscription mapToUserModel(Subscription subscription, Invoice invoice, SubscriptionCreationDto subscriptionCreationDto) {
        return UserSubscription.builder()
                .subscription(subscription)
                .invoice(invoice)
                .days(subscriptionCreationDto.getSubscriptionDays())
                .autoRenewal(subscriptionCreationDto.getAutoRenewal())
                .build();
    }

    public SubscriptionResponseDto mapToDtoFull(UserSubscription userSubscription, LocalDate startDate, LocalDate endDate) {
        Subscription subscription = userSubscription.getSubscription();
        return SubscriptionResponseDto.builder()
                .id(subscription.getId())
                .name(subscription.getName())
                .appName(subscription.getApp().getName())
                .fee(userSubscription.getInvoice().getAmount())
                .startDate(startDate)
                .endDate(endDate)
                .daysRemaining(endDate.getDayOfMonth() - startDate.getDayOfMonth())
                .autoRenewal(userSubscription.getAutoRenewal())
                .active(userSubscription.getActive())
                .build();
    }

    public SubscriptionResponseDto mapToDtoPartial(Subscription subscription, LocalDate startDate, LocalDate endDate, double fee) {
        return SubscriptionResponseDto.builder()
                .id(subscription.getId())
                .name(subscription.getName())
                .appName(subscription.getName())
                .fee(fee)
                .daysRemaining(endDate.getDayOfMonth() - startDate.getDayOfMonth())
                .build();
    }

    public SubscriptionResponseDto mapToDtoShort(Subscription subscription, double fee, int days) {
        return SubscriptionResponseDto.builder()
                .name(subscription.getName())
                .fee(fee)
                .days(days)
                .build();
    }
}
