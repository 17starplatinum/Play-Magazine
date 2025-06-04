package com.example.backend.services.data;

import com.example.backend.dto.data.subscription.SubscriptionCreationDto;
import com.example.backend.dto.data.subscription.SubscriptionRequestDto;
import com.example.backend.dto.data.subscription.SubscriptionResponseDto;
import com.example.backend.exceptions.notfound.AppNotFoundException;
import com.example.backend.exceptions.notfound.SubscriptionNotFoundException;
import com.example.backend.mappers.SubscriptionMapper;
import com.example.backend.model.auth.Role;
import com.example.backend.model.auth.User;
import com.example.backend.model.data.app.App;
import com.example.backend.model.data.finances.Card;
import com.example.backend.model.data.finances.Invoice;
import com.example.backend.model.data.subscriptions.Subscription;
import com.example.backend.model.data.subscriptions.UserSubscription;
import com.example.backend.model.data.subscriptions.UserSubscriptionId;
import com.example.backend.repositories.data.app.AppRepository;
import com.example.backend.repositories.data.finances.InvoiceRepository;
import com.example.backend.repositories.data.subscription.SubscriptionRepository;
import com.example.backend.repositories.data.subscription.UserSubscriptionRepository;
import com.example.backend.services.auth.UserService;
import com.example.backend.services.util.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.time.LocalDate;
import java.util.*;

import static com.example.backend.services.data.AppService.APP_NOT_FOUND_MESSAGE;

@Service
@RequiredArgsConstructor
public class SubscriptionService {
    private static final String SUBSCRIPTION_NOT_FOUND = "Подписка не найдена";
    private final SubscriptionRepository subscriptionRepository;
    private final UserSubscriptionRepository userSubscriptionRepository;
    private final NotificationService notificationService;
    private final UserService userService;
    private final CardService cardService;
    private final AppRepository appRepository;
    private final SubscriptionMapper subscriptionMapper;
    private final InvoiceRepository invoiceRepository;
    private final PlatformTransactionManager transactionManager;
    private final DefaultTransactionDefinition definition;

    public Subscription getSubscriptionById(UUID id) {
        return subscriptionRepository.findById(id).orElseThrow(() -> new SubscriptionNotFoundException(SUBSCRIPTION_NOT_FOUND));
    }

    public List<SubscriptionResponseDto> getSubscriptions(UUID appId) {
        User user = userService.getCurrentUser();
        List<Subscription> subscriptions = (appId == null)
                ? userSubscriptionRepository.findByUserId(user.getId())
                : appRepository.findById(appId)
                .map(app -> userSubscriptionRepository.findByUserAndApp(user.getId(), app.getId()))
                .orElseThrow(() -> new AppNotFoundException(APP_NOT_FOUND_MESSAGE));

        return subscriptions.stream()
                .map(subscription -> {
                    UserSubscription userSubscription = userSubscriptionRepository
                            .findBySubscriptionId(subscription.getId());
                    return subscriptionMapper.mapToDtoPartial(
                            subscription,
                            userSubscription.getStartDate(),
                            userSubscription.getEndDate(),
                            subscription.getPrice()
                    );
                })
                .toList();
    }

    public List<SubscriptionResponseDto> getAppSubscriptions(UUID appId) {
        App app = appRepository.findById(appId)
                .orElseThrow(() -> new AppNotFoundException(APP_NOT_FOUND_MESSAGE));
        return app.getSubscriptions().stream()
                .map(subscriptionMapper::mapToDtoShort)
                .toList();
    }

    public Subscription createSubscription(SubscriptionCreationDto subscriptionCreationDto) {
        App app = appRepository.findById(subscriptionCreationDto.getAppId())
                .orElseThrow(() -> new AppNotFoundException(APP_NOT_FOUND_MESSAGE));
        Subscription subscription = Subscription.builder()
                .app(app)
                .name(subscriptionCreationDto.getName())
                .price(subscriptionCreationDto.getSubscriptionPrice())
                .days(subscriptionCreationDto.getSubscriptionDays())
                .build();
        return subscriptionRepository.save(subscription);
    }

    public SubscriptionResponseDto getSubscriptionInfo(UUID id) {
        Subscription subscription = subscriptionRepository
                .findById(id).orElseThrow(() -> new SubscriptionNotFoundException(SUBSCRIPTION_NOT_FOUND));
        UserSubscription userSubscription = userSubscriptionRepository.findBySubscriptionId(subscription.getId());

        LocalDate startDate = userSubscription.getStartDate();
        LocalDate endDate = userSubscription.getEndDate();

        return subscriptionMapper.mapToDtoFull(userSubscription, startDate, endDate);
    }

    public UserSubscription buySubscription(SubscriptionRequestDto subscriptionRequestDto) {
        User user = userService.getCurrentUser();
        Card card = cardService.getCardByIdAndUser(subscriptionRequestDto.getCardId(), user);

        Invoice invoice = invoiceRepository.save(Invoice.builder().amount(subscriptionRequestDto.getFee()).build());

        Subscription subscription = subscriptionRepository.findById(subscriptionRequestDto.getId())
                .orElseThrow(() -> new SubscriptionNotFoundException(SUBSCRIPTION_NOT_FOUND));
        UserSubscription userSubscription = UserSubscription.builder()
                .id(new UserSubscriptionId(user.getId(), subscriptionRequestDto.getId()))
                .user(user)
                .card(card)
                .startDate(LocalDate.now())
                .invoice(invoice)
                .endDate(LocalDate.now().plusDays(subscriptionRequestDto.getDays()))
                .active(true)
                .subscription(subscription)
                .build();

        return userSubscriptionRepository.save(userSubscription);
    }

    public void cancelSubscription(UUID subscriptionId) {
        User user = userService.getCurrentUser();

        Subscription subscription = userSubscriptionRepository
                .findByIdAndUser(subscriptionId, user.getId())
                .orElseThrow(() -> new SubscriptionNotFoundException("You haven't gotten this subscription yet"));

        UserSubscription userSubscription = userSubscriptionRepository.findBySubscriptionId(subscriptionId);

        userSubscription.setActive(false);
        userSubscription.setAutoRenewal(false);

        userSubscriptionRepository.save(userSubscription);
        notificationService.notifyUserAboutSubscriptionCancellation(user, subscription);
    }

    public void toggleAutoRenewal(UUID subscriptionId) {
        User user = userService.getCurrentUser();

        Subscription subscription = userSubscriptionRepository
                .findByIdAndUser(subscriptionId, user.getId())
                .orElseThrow(() -> new SubscriptionNotFoundException("You haven't gotten this subscription yet"));

        UserSubscription userSubscription = userSubscriptionRepository.findBySubscriptionId(subscriptionId);
        boolean inverseAutoRenewal = !userSubscription.getAutoRenewal();
        userSubscription.setAutoRenewal(inverseAutoRenewal);
        userSubscriptionRepository.save(userSubscription);
        notificationService.notifyUserAboutSubscriptionAutoRenewal(user, subscription, inverseAutoRenewal);
    }

    public void deleteSubscription(UUID subscriptionId) {
        TransactionStatus transaction = transactionManager.getTransaction(definition);
        User user = userService.getCurrentUser();
        Subscription subscription = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new SubscriptionNotFoundException(SUBSCRIPTION_NOT_FOUND));
        if(user.getRole() == Role.DEVELOPER && subscription.getApp().getAuthor() != user) {
            transactionManager.rollback(transaction);
            throw new AccessDeniedException("You don't have access to this app");
        }
        userSubscriptionRepository.deleteBySubscription(subscription);
        subscriptionRepository.delete(subscription);
        transactionManager.commit(transaction);
    }
    
    @Scheduled(cron = "0 0 0 * * ?")
    public void processCancelledSubscriptions() {
        userSubscriptionRepository.deleteUserSubscriptionsPeriodically();
    }
}
