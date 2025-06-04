package com.example.backend.services.data;

import com.example.backend.dto.data.purchase.PurchaseHistoryDto;
import com.example.backend.dto.data.subscription.SubscriptionRequestDto;
import com.example.backend.exceptions.conflict.SubscriptionAlreadyPurchasedException;
import com.example.backend.exceptions.notfound.AppNotFoundException;
import com.example.backend.exceptions.notfound.SubscriptionNotFoundException;
import com.example.backend.exceptions.paymentrequired.AppNotPurchasedException;
import com.example.backend.exceptions.conflict.AppAlreadyPurchasedException;
import com.example.backend.exceptions.prerequisites.InsufficientFundsException;
import com.example.backend.mappers.PurchaseMapper;
import com.example.backend.model.auth.User;
import com.example.backend.model.auth.UserBudget;
import com.example.backend.model.data.app.App;
import com.example.backend.model.data.finances.Card;
import com.example.backend.model.data.finances.Invoice;
import com.example.backend.model.data.finances.MonetaryTransaction;
import com.example.backend.model.data.finances.Purchase;
import com.example.backend.model.data.subscriptions.UserSubscription;
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
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.example.backend.model.data.finances.PurchaseType.APP;
import static com.example.backend.model.data.finances.PurchaseType.SUBSCRIPTION;

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
    private final PurchaseMapper purchaseMapper;
    private final InvoiceRepository invoiceRepository;
    private final MonetaryRepository monetaryRepository;
    private final UserSubscriptionRepository userSubscriptionRepository;
    private final PlatformTransactionManager transactionManager;
    private final DefaultTransactionDefinition definition;
    @Resource
    private PurchaseService purchaseServiceResource;

    public Purchase processPurchase(UUID appId, UUID cardId, Optional<SubscriptionRequestDto> requestDto) {
        App app = appRepository.findById(appId)
                .orElseThrow(() -> new AppNotFoundException("Application not found"));

        User user = userService.getCurrentUser();

        if (app.getPrice() == 0D) {
            return createFreePurchase(user, app);
        }

        Card card;
        if (cardId != null) {
            card = cardService.getCardByIdAndUser(cardId, user);
        } else {
            card = cardService.getCardByDefault()
                    .orElseThrow(() -> new UnsupportedOperationException("Не явно, какую карту выбрать"));
        }
        return processOneTimePurchase(user, app, card);
    }

    private Purchase processOneTimePurchase(User user, App app, Card card) {
        TransactionStatus transaction = transactionManager.getTransaction(definition);
        Double price = app.getPrice();
        UserBudget userBudget = budgetService.getUserBudget();
        budgetService.recordSpending(userBudget, price);

        if (card.getBalance() < app.getPrice()) {
            transactionManager.rollback(transaction);
            throw new InsufficientFundsException("Not enough money!");
        }

        boolean alreadyPurchased = hasUserPurchasedApp(user, app);
        if(alreadyPurchased) {
            transactionManager.rollback(transaction);
            throw new AppAlreadyPurchasedException("Application has already bought");
        }

        card.setBalance(card.getBalance() - price);

        cardRepository.save(card);

        Invoice invoice = invoiceRepository.save(purchaseMapper.mapToInvoice(price));
        MonetaryTransaction monetaryTransaction = monetaryRepository.save(purchaseMapper.mapToTransaction(card, invoice));
        Purchase purchase = purchaseMapper.mapToModel(APP, app, monetaryTransaction, user);

        transactionManager.commit(transaction);
        return purchaseRepository.save(purchase);
    }

    private Purchase createFreePurchase(User user, App app) {
        return purchaseRepository.save(Purchase.builder()
                .user(user)
                .purchaseType(APP)
                .app(app)
                .downloadedVersion(app.getLatestVersion().getVersion())
                .build());
    }

    public Purchase processSubscriptionPurchase(User user, SubscriptionRequestDto requestDto) {
        TransactionStatus transaction = transactionManager.getTransaction(definition);
        if(userSubscriptionRepository.findByIdAndUser(requestDto.getId(), user.getId()).isPresent()) {
            transactionManager.rollback(transaction);
            throw new SubscriptionAlreadyPurchasedException("You have already purchased this subscription");
        }
        App app = appRepository.findById(requestDto.getAppId()).orElseThrow(() -> new AppNotFoundException("Приложение не найдено"));
        Card card = cardService.getCardByIdAndUser(requestDto.getCardId(), user);
        UserBudget userBudget = budgetService.getUserBudget();

        double subscriptionPrice = requestDto.getFee();
        UserSubscription userSubscription = subscriptionService.buySubscription(requestDto);

        budgetService.recordSpending(userBudget, subscriptionPrice);

        if (card.getBalance() < requestDto.getFee()) {
            transactionManager.rollback(transaction);
            throw new InsufficientFundsException("Not enough money!");
        }

        MonetaryTransaction monetaryTransaction = monetaryRepository.save(purchaseMapper.mapToTransaction(card, userSubscription.getInvoice()));

        card.setBalance(card.getBalance() - subscriptionPrice);
        cardRepository.save(card);

        transactionManager.commit(transaction);
        return purchaseRepository.save(purchaseMapper.mapToModel(SUBSCRIPTION, app, monetaryTransaction, user));
    }

    public List<PurchaseHistoryDto> getUserPurchases() {
        User user = userService.getCurrentUser();
        List<Purchase> userPurchases = purchaseRepository.findByUser(user);
        List<PurchaseHistoryDto> purchaseHistoryDtos = new ArrayList<>();
        for (Purchase purchase : userPurchases) {
            MonetaryTransaction transaction;
            try {
                transaction = purchase.getTransaction();
            } catch (NullPointerException e) {
                continue;
            }
            String secureNumber = transaction.getCard().getNumber();
            PurchaseHistoryDto dto = PurchaseHistoryDto.builder()
                    .appName(purchase.getApp().getName())
                    .purchaseType(purchase.getPurchaseType())
                    .cardNumber("*" + secureNumber.substring(secureNumber.length() - 4))
                    .purchaseDate(transaction.getProcessedAt().toLocalDate())
                    .purchasePrice(transaction.getInvoice().getAmount())
                    .build();
            purchaseHistoryDtos.add(dto);
        }
        return purchaseHistoryDtos;
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
            throw new AppNotPurchasedException("You must buy this application");
        }
    }
}
