package com.example.pmweb.schedulers;

import com.example.pmweb.model.data.subscriptions.UserSubscription;
import com.example.pmweb.repositories.data.subscription.UserSubscriptionRepository;
import com.example.pmweb.services.auth.UserService;
import com.example.pmweb.services.util.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class SubscriptionScheduler {
    private final UserSubscriptionRepository userSubscriptionRepository;
    private final NotificationService notificationService;
    private final UserService userService;

    @Scheduled(cron = "0 0 0 * * ?")
    public void checkExpiredSubscriptions() {
        log.info("Checking for expired subscriptions...");
        List<UserSubscription> expiredSubscriptions = userSubscriptionRepository
                .findByEndDateBeforeAndActive(LocalDate.now(), true);

        for (UserSubscription subscription : expiredSubscriptions) {
            notificationService.notifyUserAboutSubscriptionExpiration(userService.getCurrentUser(), subscription);

            subscription.setActive(false);
            userSubscriptionRepository.save(subscription);
            log.info("Subscription {} for user {} has been expired", subscription.getId(), subscription.getUser().getId());
        }
    }
}
