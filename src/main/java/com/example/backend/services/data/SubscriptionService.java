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
import com.example.backend.repositories.data.app.AppRepository;
import com.example.backend.repositories.data.finances.InvoiceRepository;
import com.example.backend.repositories.data.subscription.SubscriptionRepository;
import com.example.backend.repositories.data.subscription.UserSubscriptionRepository;
import com.example.backend.services.auth.UserService;
import com.example.backend.services.util.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

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

    public Subscription getSubscriptionById(UUID id) {
        return subscriptionRepository.findById(id).orElseThrow(() -> new SubscriptionNotFoundException(SUBSCRIPTION_NOT_FOUND));
    }

    public List<SubscriptionResponseDto> getSubscriptions(UUID appId) {
        User user = userService.getCurrentUser();
        List<Subscription> subscriptions;
        if(appId == null) {
            subscriptions = userSubscriptionRepository.findByUserId(user.getId());
        } else {
            App app = appRepository.findById(appId)
                    .orElseThrow(() -> new AppNotFoundException("Приложение не найдено"));
            subscriptions = userSubscriptionRepository.findByUserAndApp(user.getId(), app.getId());
        }
        List<SubscriptionResponseDto> subscriptionResponseDtos = new ArrayList<>();
        for (Subscription subscription : subscriptions) {
            UserSubscription userSubscription = userSubscriptionRepository
                    .findBySubscriptionId(subscription.getId());
            LocalDate startDate = userSubscription.getStartDate();
            LocalDate endDate = userSubscription.getEndDate();
            double fee = userSubscription.getInvoice().getAmount();
            subscriptionResponseDtos.add(subscriptionMapper.mapToDtoPartial(subscription, startDate, endDate, fee));
        }
        return subscriptionResponseDtos;
    }

    public List<SubscriptionResponseDto> getAppSubscriptions(UUID appId) {
        App app = appRepository.findById(appId)
                .orElseThrow(() -> new AppNotFoundException("Приложение не найдено"));
        List<Subscription> subscriptions = app.getSubscriptions();
        List<SubscriptionResponseDto> subscriptionResponseDtos = new ArrayList<>();
        for(Subscription subscription : subscriptions) {
            UserSubscription userSubscription = userSubscriptionRepository
                    .findBySubscriptionId(subscription.getId());
            double fee = userSubscription.getInvoice().getAmount();
            int days = userSubscription.getDays();
            subscriptionResponseDtos.add(subscriptionMapper.mapToDtoShort(subscription, fee, days));
        }
        return subscriptionResponseDtos;
    }

    @Transactional
    public Subscription createSubscription(SubscriptionCreationDto subscriptionCreationDto) {
        App app = appRepository.findById(subscriptionCreationDto.getAppId())
                .orElseThrow(() -> new AppNotFoundException("Приложение не найдено"));
        Subscription subscription = Subscription.builder()
                .app(app)
                .name(subscriptionCreationDto.getName())
                .build();
        subscription = subscriptionRepository.save(subscription);

        Invoice invoice = invoiceRepository.save(Invoice.builder().amount(subscriptionCreationDto.getSubscriptionPrice()).build());
        UserSubscription newSubscription = subscriptionMapper.mapToUserModel(subscription, invoice, subscriptionCreationDto);
        userSubscriptionRepository.save(newSubscription);
        return subscription;
    }

    public SubscriptionResponseDto getSubscriptionInfo(UUID id) {
        Subscription subscription = subscriptionRepository
                .findById(id).orElseThrow(() -> new SubscriptionNotFoundException(SUBSCRIPTION_NOT_FOUND));
        UserSubscription userSubscription = userSubscriptionRepository.findBySubscriptionId(subscription.getId());

        LocalDate startDate = userSubscription.getStartDate();
        LocalDate endDate = userSubscription.getEndDate();

        return subscriptionMapper.mapToDtoFull(userSubscription, startDate, endDate);
    }

    @Transactional
    public void buySubscription(UUID id, SubscriptionRequestDto subscriptionRequestDto) {
        User user = userService.getCurrentUser();
        Card card = cardService.getCardByIdAndUser(subscriptionRequestDto.getCardId(), user);

        App app = appRepository.findByName(subscriptionRequestDto.getAppName());
        UserSubscription userSubscription = userSubscriptionRepository
                .findBySubscriptionAndApp(id, app.getId())
                .orElseThrow(() -> new SubscriptionNotFoundException("Такой подписки нет в этом приложении, или в приложении нет такой подписки"));

        userSubscription.setUser(user);
        userSubscription.setCard(card);
        userSubscription.setStartDate(LocalDate.now());
        userSubscription.setEndDate(LocalDate.now().plusDays(userSubscription.getDays()));
        userSubscription.setActive(true);
        userSubscriptionRepository.save(userSubscription);
    }

    @Transactional
    public void cancelSubscription(UUID subscriptionId) {
        User user = userService.getCurrentUser();

        Subscription subscription = userSubscriptionRepository
                .findByIdAndUser(subscriptionId, user.getId())
                .orElseThrow(() -> new SubscriptionNotFoundException("Эту подписку вы ещё не приобрели"));

        UserSubscription userSubscription = userSubscriptionRepository.findBySubscriptionId(subscriptionId);

        userSubscription.setActive(false);
        userSubscription.setAutoRenewal(false);

        userSubscriptionRepository.save(userSubscription);
        notificationService.notifyUserAboutSubscriptionCancellation(user, subscription);
    }

    @Transactional
    public void cancelAutoRenewal(UUID subscriptionId) {
        User user = userService.getCurrentUser();

        Subscription subscription = userSubscriptionRepository
                .findByIdAndUser(subscriptionId, user.getId())
                .orElseThrow(() -> new SubscriptionNotFoundException("Эту подписку вы ещё не приобрели"));

        UserSubscription userSubscription = userSubscriptionRepository.findBySubscriptionId(subscriptionId);

        userSubscription.setAutoRenewal(false);
        userSubscriptionRepository.save(userSubscription);
        notificationService.notifyUserAboutSubscriptionAutoRenewal(user, subscription);
    }

    @Transactional
    public void deleteSubscription(UUID subscriptionId) {
        User user = userService.getCurrentUser();
        Subscription subscription = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new SubscriptionNotFoundException(SUBSCRIPTION_NOT_FOUND));
        if(user.getRole() == Role.DEVELOPER && subscription.getApp().getAuthor() != user) {
            throw new AccessDeniedException("Вы не имеете доступ к этому приложению");
        }
        UserSubscription userSubscription = userSubscriptionRepository.findBySubscriptionId(subscriptionId);
        userSubscriptionRepository.delete(userSubscription);
        subscriptionRepository.delete(subscription);
    }
    
    @Scheduled(cron = "0 0 0 * * ?")
    public void processCancelledSubscriptions() {
        LocalDate today = LocalDate.now();
        Set<UserSubscription> expiredSubscriptionsInfo = userSubscriptionRepository.findByActiveFalseAndEndDateBefore(today);

        subscriptionRepository.deleteAllBySubscribedUser(expiredSubscriptionsInfo);
    }
}
