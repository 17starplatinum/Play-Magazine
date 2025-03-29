package com.example.backend.services.data;


import com.example.backend.dto.data.purchase.PurchaseRequest;
import com.example.backend.exceptions.notfound.UserNotFoundException;
import com.example.backend.exceptions.prerequisites.AppAlreadyPurchasedException;
import com.example.backend.exceptions.notfound.AppNotFoundException;
import com.example.backend.exceptions.paymentrequired.AppNotPurchasedException;
import com.example.backend.exceptions.notfound.CardNotFoundException;
import com.example.backend.exceptions.prerequisites.InsufficientFundsException;
import com.example.backend.model.auth.User;
import com.example.backend.model.data.App;
import com.example.backend.model.data.Card;
import com.example.backend.model.data.Purchase;
import com.example.backend.model.data.Subscription;
import com.example.backend.repositories.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static com.example.backend.model.data.PurchaseType.FREE;
import static com.example.backend.model.data.PurchaseType.SUBSCRIPTION;

@Service
@RequiredArgsConstructor
public class PurchaseService {
    private final PurchaseRepository purchaseRepository;
    private final AppRepository appRepository;
    private final UserRepository userRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final CardRepository cardRepository;
    private final BudgetService budgetService;

    public Purchase purchaseApp(PurchaseRequest purchaseRequest, UserDetails userDetails) {
        App app = appRepository.findByIdAndAvailableTrue(purchaseRequest.getAppId())
                .orElseThrow(() -> new AppNotFoundException("Приложение не найдено", new RuntimeException()));

        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new UserNotFoundException("Пользователь не найден", new RuntimeException()));

        Card card = cardRepository.findByIdAndUser(purchaseRequest.getCardId(), user)
                .orElseThrow(() -> new CardNotFoundException("Карта не найдена", new RuntimeException()));

        if (app.isFree()) {
            return createFreePurchase(user, app);
        }

        if (app.isSubscription()) {
            return processSubscriptionPurchase(user, app, card);
        }
        return processOneTimePurchase(user, app, card);
    }

    private Purchase processOneTimePurchase(User user, App app, Card card) {
        budgetService.recordSpending(user, app.getPrice());
        if (card.getBalance() < app.getPrice()) {
            throw new InsufficientFundsException("Не хватает средства", new RuntimeException());
        }


        boolean alreadyPurchased = hasUserPurchasedApp(user, app);
        if(alreadyPurchased) {
            throw new AppAlreadyPurchasedException("Приложение уже куплено", new RuntimeException());
        }

        card.setBalance(card.getBalance() - app.getPrice());

        cardRepository.save(card);

        Purchase purchase = Purchase.builder()
                .app(app)
                .user(user)
                .cost(app.getPrice())
                .description(app.getDescription())
                .creationTime(LocalDateTime.now())
                .installedVersion(app.getVersion())
                .lastUpdated(LocalDateTime.now())
                .build();

        return purchaseRepository.save(purchase);
    }

    private Purchase createFreePurchase(User user, App app) {
        return purchaseRepository.save(Purchase.builder()
                .user(user)
                .app(app)
                .cost(0F)
                .purchaseType(FREE)
                .build());
    }

    private Purchase processSubscriptionPurchase(User user, App app, Card card) {
        budgetService.recordSpending(user, app.getSubscriptionPrice());
        if (card.getBalance() < app.getPrice()) {
            throw new InsufficientFundsException("Не хватает средства", new RuntimeException());
        }
        card.setBalance(card.getBalance() - app.getPrice());
        cardRepository.save(card);
        subscriptionRepository.save(
                Subscription.builder()
                        .user(user)
                        .app(app)
                        .card(card)
                        .price(app.getSubscriptionPrice())
                        .startDate(LocalDate.now())
                        .endDate(LocalDate.now().plusMonths(app.getSubscriptionPeriod())
                        ).build());
        return purchaseRepository.save(
                Purchase.builder()
                        .user(user)
                        .app(app)
                        .card(card)
                        .cost(app.getSubscriptionPrice())
                        .purchaseType(SUBSCRIPTION)
                        .build()
        );
    }

    public List<Purchase> getUserPurchases(UserDetails currentUser) {
        User user = userRepository.findByEmail(currentUser.getUsername())
                .orElseThrow(() -> new UserNotFoundException("Пользователь не найден", new RuntimeException()));

        return purchaseRepository.findByUser(user);
    }

    public boolean hasUserPurchasedApp(User user, App app) {
        return purchaseRepository.existsByUserAndApp(user, app);
    }

    public Purchase getPurchaseByApp(User user, App app) {
        return purchaseRepository.findByUserAndApp(user, app)
                .orElseThrow(() -> new AppNotPurchasedException("Приложение не куплено", new RuntimeException()));
    }

    public void validateUpdateAccess(User user, App app) {
        if (!hasUserPurchasedApp(user, app)) {
            throw new AppNotPurchasedException("Вам надо покупать приложение", new RuntimeException());
        }
    }
}
