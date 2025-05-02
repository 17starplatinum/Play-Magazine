package com.example.backend.services.data;


import com.example.backend.dto.data.purchase.PurchaseRequest;
import com.example.backend.exceptions.notfound.AppNotFoundException;
import com.example.backend.exceptions.notfound.SubscriptionNotFoundException;
import com.example.backend.exceptions.paymentrequired.AppNotPurchasedException;
import com.example.backend.exceptions.prerequisites.AppAlreadyPurchasedException;
import com.example.backend.exceptions.prerequisites.InsufficientFundsException;
import com.example.backend.mappers.PurchaseMapper;
import com.example.backend.mappers.SubscriptionMapper;
import com.example.backend.model.auth.User;
import com.example.backend.model.auth.UserBudget;
import com.example.backend.model.data.app.App;
import com.example.backend.model.data.finances.Card;
import com.example.backend.model.data.finances.Invoice;
import com.example.backend.model.data.finances.MonetaryTransaction;
import com.example.backend.model.data.finances.Purchase;
import com.example.backend.model.data.subscriptions.Subscription;
import com.example.backend.model.data.subscriptions.SubscriptionInfo;
import com.example.backend.repositories.auth.UserBudgetRepository;
import com.example.backend.repositories.data.app.AppRepository;
import com.example.backend.repositories.data.finances.CardRepository;
import com.example.backend.repositories.data.finances.PurchaseRepository;
import com.example.backend.repositories.data.subscription.SubscriptionInfoRepository;
import com.example.backend.repositories.data.subscription.SubscriptionRepository;
import com.example.backend.services.auth.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;


@Service
@RequiredArgsConstructor
public class PurchaseService {
    private final PurchaseRepository purchaseRepository;
    private final AppRepository appRepository;
    private final UserService userService;
    private final SubscriptionRepository subscriptionRepository;
    private final CardRepository cardRepository;
    private final BudgetService budgetService;
    private final CardService cardService;
    private final UserBudgetRepository userBudgetRepository;
    private final SubscriptionInfoRepository infoRepository;
    private final PurchaseMapper purchaseMapper;
    private final SubscriptionMapper subscriptionMapper;

    @Transactional
    public Purchase purchaseApp(PurchaseRequest purchaseRequest) {
        App app = appRepository.findById(purchaseRequest.getAppId())
                .orElseThrow(() -> new AppNotFoundException("Application not found"));

        User user = userService.getCurrentUser();

        Card card = cardService.getCardByIdAndUser(purchaseRequest.getCardId(), user);

        if (app.getPrice() == 0D) {
            return createFreePurchase(user, app);
        }

        if (app.hasSubscriptions()) {
            Subscription subscription = subscriptionRepository.findById(purchaseRequest.getSubscriptionId())
                    .orElseThrow(() -> new SubscriptionNotFoundException("Cannot find subscription!"));
            return processSubscriptionPurchase(user, app, card, subscription);
        }
        return processOneTimePurchase(user, app, card);
    }

    private Purchase processOneTimePurchase(User user, App app, Card card) {
        Double price = app.getPrice();
        UserBudget userBudget = userBudgetRepository.findUserBudgetByUser(user);
        budgetService.recordSpending(userBudget, price);


        if (card.getBalance() < app.getPrice()) {
            throw new InsufficientFundsException("Not enough money!");
        }

        boolean alreadyPurchased = hasUserPurchasedApp(user, app);
        if(alreadyPurchased) {
            throw new AppAlreadyPurchasedException("Application has already bought");
        }

        card.setBalance(card.getBalance() - price);

        cardRepository.save(card);

        Invoice invoice = purchaseMapper.mapToInvoice(price);
        MonetaryTransaction transaction = purchaseMapper.mapToTransaction(card, invoice);
        Purchase purchase = purchaseMapper.mapToModel(app, transaction, user);

        return purchaseRepository.save(purchase);
    }

    private Purchase createFreePurchase(User user, App app) {
        return purchaseRepository.save(Purchase.builder()
                .user(user)
                .app(app)
                .build());
    }

    private Purchase processSubscriptionPurchase(User user, App app, Card card, Subscription subscription) {
        UserBudget userBudget = userBudgetRepository.findUserBudgetByUser(user);
        SubscriptionInfo subscriptionInfo = subscription.getSubscriptionInfo();
        double subscriptionPrice = subscription.getSubscriptionInfo().getInvoice().getAmount();
        budgetService.recordSpending(userBudget, subscriptionPrice);

        if (card.getBalance() < subscriptionPrice) {
            throw new InsufficientFundsException("Not enough money!");
        }

        card.setBalance(card.getBalance() - subscriptionPrice);
        cardRepository.save(card);

        subscriptionInfo.setStartDate(LocalDate.now());
        subscriptionInfo.setEndDate(LocalDate.now().plusDays(subscriptionInfo.getDays()));
        subscriptionInfo.setAutoRenewal(true);
        subscriptionInfo.setActive(true);

        subscriptionInfo = infoRepository.save(subscriptionInfo);

        subscriptionRepository.save(subscriptionMapper.mapViaInfo(user, card, subscription.getName(), subscriptionInfo));
        Invoice invoice = purchaseMapper.mapToInvoice(subscriptionPrice);

        MonetaryTransaction transaction = purchaseMapper.mapToTransaction(card, invoice);

        return purchaseRepository.save(purchaseMapper.mapToModel(app, transaction, user));
    }

    public List<Purchase> getUserPurchases() {
        User user = userService.getCurrentUser();

        return purchaseRepository.findByUser(user);
    }

    public boolean hasUserPurchasedApp(User user, App app) {
        return purchaseRepository.existsByUserAndApp(user, app);
    }

    public Purchase getPurchaseByUserAndApp(User user, App app) {
        return purchaseRepository.findByUserAndApp(user, app)
                .orElseThrow(() -> new AppNotPurchasedException("You have no this application"));
    }

    public void validateUpdateAccess(User user, App app) {
        if (!hasUserPurchasedApp(user, app)) {
            throw new AppNotPurchasedException("You must buy this application");
        }
    }
}
