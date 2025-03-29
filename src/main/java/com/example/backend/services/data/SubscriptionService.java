package com.example.backend.services.data;


import com.example.backend.exceptions.notfound.SubscriptionNotFoundException;
import com.example.backend.exceptions.notfound.UserNotFoundException;
import com.example.backend.model.auth.User;
import com.example.backend.model.data.Subscription;
import com.example.backend.repositories.AppRepository;
import com.example.backend.repositories.SubscriptionRepository;
import com.example.backend.repositories.UserRepository;
import com.example.backend.services.util.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SubscriptionService {
    private static final String USER_NOT_FOUND_MESSAGE = "User not found"; 
    private final SubscriptionRepository subscriptionRepository;
    private final UserRepository userRepository;
    private final AppRepository appRepository;
    private final NotificationService notificationService;

    public List<Subscription> getAllSubscriptions(UserDetails currentUser) {
        User user = userRepository.findByEmail(currentUser.getUsername())
                .orElseThrow(() -> new UserNotFoundException(USER_NOT_FOUND_MESSAGE, new RuntimeException()));

        return subscriptionRepository.findByUser(user);
    }

    public void cancelSubscription(UUID subscriptionId, UserDetails currentUser) {
        User user = userRepository.findByEmail(currentUser.getUsername())
                .orElseThrow(() -> new UserNotFoundException(USER_NOT_FOUND_MESSAGE, new RuntimeException()));

        Subscription subscription = subscriptionRepository.findByIdAndUser(subscriptionId, user)
                .orElseThrow(() -> new SubscriptionNotFoundException("Подписка не найдена", new RuntimeException()));

        subscription.setActive(false);
        subscription.setAutoRenewal(false);
        subscriptionRepository.save(subscription);
        notificationService.notifyUserAboutSubscriptionCancellation(user, subscription);
    }

    public void cancelAutoRenewal(UUID subscriptionId, UserDetails currentUser) {
        User user = userRepository.findByEmail(currentUser.getUsername())
                .orElseThrow(() -> new UserNotFoundException(USER_NOT_FOUND_MESSAGE, new RuntimeException()));

        Subscription subscription = subscriptionRepository.findByIdAndUser(subscriptionId, user)
                .orElseThrow(() -> new SubscriptionNotFoundException("Подписка не найдена", new RuntimeException()));

        subscription.setAutoRenewal(false);
        subscriptionRepository.save(subscription);
        notificationService.notifyUserAboutSubscriptionAutoRenewal(user, subscription);
    }

    @Scheduled(cron = "0 0 0 * * ?")
    public void processCancelledSubscriptions() {
        LocalDate today = LocalDate.now();
        List<Subscription> expiredSubscriptions = subscriptionRepository.findByActiveFalseAndEndDateAfter(today);
        subscriptionRepository.deleteAll(expiredSubscriptions);
    }
}
