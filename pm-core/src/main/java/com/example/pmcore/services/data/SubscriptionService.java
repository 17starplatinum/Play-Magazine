package com.example.pmcore.services.data;

import com.example.pmcore.dto.data.subscription.SubscriptionCreationDto;
import com.example.pmcore.dto.data.subscription.SubscriptionRequestDto;
import com.example.pmcore.dto.data.subscription.SubscriptionResponseDto;
import com.example.pmcore.exceptions.notfound.AppNotFoundException;
import com.example.pmcore.exceptions.notfound.SubscriptionNotFoundException;
import com.example.pmcore.exceptions.prerequisites.InsufficientFundsException;
import com.example.pmcore.mappers.SubscriptionMapper;
import com.example.pmcore.model.auth.Role;
import com.example.pmcore.model.auth.User;
import com.example.pmcore.model.auth.UserBudget;
import com.example.pmcore.model.data.app.App;
import com.example.pmcore.model.data.finances.Card;
import com.example.pmcore.model.data.finances.Invoice;
import com.example.pmcore.model.data.subscriptions.Subscription;
import com.example.pmcore.model.data.subscriptions.UserSubscription;
import com.example.pmcore.model.data.subscriptions.UserSubscriptionId;
import com.example.pmcore.repositories.data.app.AppRepository;
import com.example.pmcore.repositories.data.finances.InvoiceRepository;
import com.example.pmcore.repositories.data.subscription.SubscriptionRepository;
import com.example.pmcore.repositories.data.subscription.UserSubscriptionRepository;
import com.example.pmcore.services.auth.UserService;
import com.example.pmcore.services.util.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static com.example.pmcore.services.data.AppService.APP_NOT_FOUND_MESSAGE;

@Service
@RequiredArgsConstructor
public class SubscriptionService {
    private static final String SUBSCRIPTION_NOT_FOUND = "Подписка не найдена";
    private final SubscriptionRepository subscriptionRepository;
    private final UserSubscriptionRepository userSubscriptionRepository;
    private final NotificationService notificationService;
    private final UserService userService;
    private final CardService cardService;
    private final BudgetService budgetService;
    private final AppRepository appRepository;
    private final SubscriptionMapper subscriptionMapper;
    private final InvoiceRepository invoiceRepository;
    private final PlatformTransactionManager transactionManager;
    private final DefaultTransactionDefinition definition;
    private final ThreadPoolTaskExecutor taskExecutor;

    public List<SubscriptionResponseDto> getSubscriptions(UUID appId) {
        User user = userService.getCurrentUser();
        List<Subscription> subscriptions = (appId == null)
                ? userSubscriptionRepository.findSubscriptionsByUserId(user.getId())
                : appRepository.findById(appId)
                .map(app -> userSubscriptionRepository.findSubscriptionsByUserAndApp(user.getId(), app.getId()))
                .orElseThrow(() -> new AppNotFoundException(APP_NOT_FOUND_MESSAGE));

        return subscriptions.stream()
                .map(subscription -> {
                    UserSubscription userSubscription = userSubscriptionRepository
                            .findUserSubscriptionBySubscriptionId(subscription.getId());
                    return subscriptionMapper.mapToDtoPartial(
                            subscription,
                            userSubscription.getStartDate(),
                            userSubscription.getEndDate(),
                            subscription.getPrice()
                    );
                })
                .toList();
    }

    public Subscription getSubscriptionById(UUID subId) {
        return subscriptionRepository.findById(subId)
                .orElseThrow(() -> new SubscriptionNotFoundException(SUBSCRIPTION_NOT_FOUND));
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
        UserSubscription userSubscription = userSubscriptionRepository.findUserSubscriptionBySubscriptionId(subscription.getId());

        LocalDate startDate = userSubscription.getStartDate();
        LocalDate endDate = userSubscription.getEndDate();

        return subscriptionMapper.mapToDtoFull(userSubscription, startDate, endDate);
    }

    public UserSubscription buySubscription(SubscriptionRequestDto subscriptionRequestDto) {
        UUID userId = userService.getCurrentUserId();
        Card card = cardService.getCardByIdAndUser(subscriptionRequestDto.getCardId(), userId);
        Subscription subscription = getSubscriptionById(subscriptionRequestDto.getId());
        Invoice invoice = invoiceRepository.save(Invoice.builder().amount(subscription.getPrice()).build());

        UserSubscription userSubscription = UserSubscription.builder()
                .id(new UserSubscriptionId(userId, subscriptionRequestDto.getId()))
                .card(card)
                .startDate(LocalDate.now())
                .invoice(invoice)
                .endDate(LocalDate.now().plusDays(subscription.getDays()))
                .active(true)
                .subscription(subscription)
                .build();

        return userSubscriptionRepository.save(userSubscription);
    }

    public void cancelSubscription(UUID subscriptionId) {
        User user = userService.getCurrentUser();

        Subscription subscription = userSubscriptionRepository
                .findSubscriptionByIdAndUser(subscriptionId, user.getId())
                .orElseThrow(() -> new SubscriptionNotFoundException("You haven't gotten this subscription yet"));

        UserSubscription userSubscription = userSubscriptionRepository.findUserSubscriptionBySubscriptionId(subscriptionId);

        userSubscription.setActive(false);
        userSubscription.setAutoRenewal(false);

        userSubscriptionRepository.save(userSubscription);
        notificationService.notifyUserAboutSubscriptionCancellation(user, subscription);
    }

    public void toggleAutoRenewal(UUID subscriptionId) {
        User user = userService.getCurrentUser();

        Subscription subscription = userSubscriptionRepository
                .findSubscriptionByIdAndUser(subscriptionId, user.getId())
                .orElseThrow(() -> new SubscriptionNotFoundException("You haven't gotten this subscription yet"));

        UserSubscription userSubscription = userSubscriptionRepository.findUserSubscriptionBySubscriptionId(subscriptionId);
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
        if(user.getRole() == Role.DEVELOPER && subscription.getApp().getAuthorId() != user.getId()) {
            transactionManager.rollback(transaction);
            throw new AccessDeniedException("You don't have access to this app");
        }
        userSubscriptionRepository.deleteBySubscription(subscription);
        subscriptionRepository.delete(subscription);
        transactionManager.commit(transaction);
    }

    @Scheduled(cron = "0 0 0 * * ?")
    public void pollAndCharge() {
        UUID userId = userService.getCurrentUserId();
        List<UserSubscription> due = userSubscriptionRepository.findAllByIdUserIdAndEndDateBeforeAndActiveTrueAndAutoRenewalTrue(userId, LocalDate.now());
        for (UserSubscription us : due) {
            taskExecutor.execute(() -> processSubscriptionCharge(us));
        }
    }

    public void processSubscriptionCharge(UserSubscription us) {
        UserBudget budget = budgetService.getUserBudget();
        double fee = us.getSubscription().getPrice();
        Card card = us.getCard();

        if (card.getBalance() < fee || budgetService.isOverBudget(budget, fee)) {
            us.setActive(false);
            throw new InsufficientFundsException("Insufficient funds");
        }

        TransactionStatus transaction = transactionManager.getTransaction(definition);
        card.setBalance(card.getBalance() - fee);
        us.setStartDate(LocalDate.now());
        us.setEndDate(LocalDate.now().plusDays(us.getSubscription().getDays()));
        Invoice invoice = Invoice.builder().amount(us.getSubscription().getPrice()).build();
        us.setInvoice(invoiceRepository.save(invoice));
        cardService.save(card);
        userSubscriptionRepository.save(us);
        notificationService.notifyUserAboutSubscriptionCharge(userService.getCurrentUser(), us.getSubscription(), us);
        transactionManager.commit(transaction);
    }

    @Scheduled(cron = "0 0 0 * * ?")
    public void processCancelledSubscriptions() {
        userSubscriptionRepository.deleteUserSubscriptionsPeriodically();
    }
}
