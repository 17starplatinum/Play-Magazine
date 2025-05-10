package com.example.backend.services.data;

import com.example.backend.dto.data.purchase.PurchaseRequest;
import com.example.backend.dto.data.subscription.SubscriptionRequestDto;
import com.example.backend.exceptions.notfound.AppNotFoundException;
import com.example.backend.exceptions.notfound.SubscriptionNotFoundException;
import com.example.backend.exceptions.paymentrequired.AppNotPurchasedException;
import com.example.backend.exceptions.prerequisites.AppAlreadyPurchasedException;
import com.example.backend.exceptions.prerequisites.InsufficientFundsException;
import com.example.backend.mappers.PurchaseMapper;
import com.example.backend.model.auth.User;
import com.example.backend.model.auth.UserBudget;
import com.example.backend.model.data.app.App;
import com.example.backend.model.data.finances.Card;
import com.example.backend.model.data.finances.Invoice;
import com.example.backend.model.data.finances.MonetaryTransaction;
import com.example.backend.model.data.finances.Purchase;
import com.example.backend.model.data.subscriptions.Subscription;
import com.example.backend.model.data.subscriptions.UserSubscription;
import com.example.backend.repositories.auth.UserBudgetRepository;
import com.example.backend.repositories.data.app.AppRepository;
import com.example.backend.repositories.data.finances.CardRepository;
import com.example.backend.repositories.data.finances.InvoiceRepository;
import com.example.backend.repositories.data.finances.MonetaryRepository;
import com.example.backend.repositories.data.finances.PurchaseRepository;
import com.example.backend.repositories.data.subscription.UserSubscriptionRepository;
import com.example.backend.services.auth.UserService;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PurchaseService {
    private final PurchaseRepository purchaseRepository;
    private final AppRepository appRepository;
    private final UserService userService;
    private final SubscriptionService subscriptionService;
    private final CardRepository cardRepository;
    private final BudgetService budgetService;
    private final CardService cardService;
    private final UserBudgetRepository userBudgetRepository;
    private final UserSubscriptionRepository userSubscriptionRepository;
    private final PurchaseMapper purchaseMapper;
    private final InvoiceRepository invoiceRepository;
    private final MonetaryRepository monetaryRepository;

    @Resource
    private PurchaseService purchaseServiceResource;

    @Transactional
    public Purchase processPurchase(UUID appId, PurchaseRequest purchaseRequest) {
        App app = appRepository.findById(appId)
                .orElseThrow(() -> new AppNotFoundException("Application not found"));

        User user = userService.getCurrentUser();

        if (app.getPrice() == 0D) {
            return createFreePurchase(user, app);
        }

        Card card;
        if (purchaseRequest != null) {
            card = cardService.getCardByIdAndUser(purchaseRequest.getCardId(), user);
        } else {
            card = cardService.getCardByDefault()
                    .orElseThrow(() -> new UnsupportedOperationException("Не явно, какую карту выбрать"));
        }

        if (app.hasSubscriptions() && purchaseRequest != null)
            return purchaseServiceResource.processSubscriptionPurchase(
                    user,
                    appId,
                    purchaseRequest.getCardId(),
                    purchaseRequest.getSubscriptionId()
            );

        return processOneTimePurchase(user, app, card);
    }

    private Purchase processOneTimePurchase(User user, App app, Card card) {
        Double price = app.getPrice();
        UserBudget userBudget = userBudgetRepository.findUserBudgetByUser(user);
        budgetService.recordSpending(userBudget, price);


        if (card.getBalance() < app.getPrice())
            throw new InsufficientFundsException("Not enough money!");


        boolean alreadyPurchased = hasUserPurchasedApp(user, app);
        if (alreadyPurchased)
            throw new AppAlreadyPurchasedException("Application has already bought");

        card.setBalance(card.getBalance() - price);

        cardRepository.save(card);

        Invoice invoice = invoiceRepository.save(purchaseMapper.mapToInvoice(price));
        MonetaryTransaction transaction = monetaryRepository.save(purchaseMapper.mapToTransaction(card, invoice));
        Purchase purchase = purchaseMapper.mapToModel(app, transaction, user);

        return purchaseRepository.save(purchase);
    }

    private Purchase createFreePurchase(User user, App app) {
        return purchaseRepository.save(Purchase.builder()
                .user(user)
                .app(app)
                .downloadedVersion(app.getLatestVersion().getVersion())
                .build());
    }

    @Transactional
    public Purchase processSubscriptionPurchase(User user, UUID appId, UUID cardId, UUID subscriptionId) {
        App app = appRepository.findById(appId).orElseThrow(() -> new AppNotFoundException("Application not found!"));
        Card card = cardService.getCardByIdAndUser(cardId, user);
        Subscription subscription = subscriptionService.getSubscriptionById(subscriptionId);
        UserBudget userBudget = userBudgetRepository.findUserBudgetByUser(user);

        UserSubscription userSubscription = userSubscriptionRepository
                .findBySubscriptionAndApp(subscription.getId(), app.getId())
                .orElseThrow(
                        () -> new SubscriptionNotFoundException("Cannot find this subscription!"));

        SubscriptionRequestDto requestDto = new SubscriptionRequestDto(
                appId,
                cardId,
                subscription.getName(),
                subscription.getApp().getName(),
                userSubscription.getInvoice().getAmount(),
                userSubscription.getDays(),
                userSubscription.getAutoRenewal()
        );
        double subscriptionPrice = userSubscription.getInvoice().getAmount();
        subscriptionService.buySubscription(subscription.getId(), requestDto);

        budgetService.recordSpending(userBudget, subscriptionPrice);

        if (card.getBalance() < subscriptionPrice) {
            throw new InsufficientFundsException("Not enough money!");
        }

        Invoice invoice = invoiceRepository.save(purchaseMapper.mapToInvoice(subscriptionPrice));

        MonetaryTransaction transaction = monetaryRepository.save(purchaseMapper.mapToTransaction(card, invoice));

        card.setBalance(card.getBalance() - subscriptionPrice);
        cardRepository.save(card);

        return purchaseRepository.save(purchaseMapper.mapToModel(app, transaction, user));
    }

    public List<Purchase> getUserPurchases() {
        User user = userService.getCurrentUser();

        return purchaseRepository.findByUser(user);
    }

    public Purchase getLastUserPurchaseByApp(App app) {
        User user = userService.getCurrentUser();
        validateDownloadAccess(user, app);
        List<Purchase> userPurchases = purchaseRepository.findAllByUserAndApp(user, app);

        return userPurchases.get(userPurchases.size() - 1);
    }

    public boolean hasUserPurchasedApp(User user, App app) {
        return purchaseRepository.existsByUserAndApp(user, app);
    }

    public void validateDownloadAccess(User user, App app) {
        if (!hasUserPurchasedApp(user, app)) {
            throw new AppNotPurchasedException("You must buy this application!");
        }
    }
}
