package com.example.backend.services.data;


import com.example.backend.exceptions.*;
import com.example.backend.model.data.App;
import com.example.backend.model.data.Card;
import com.example.backend.model.data.Purchase;
import com.example.backend.model.auth.User;
import com.example.backend.repositories.AppRepository;
import com.example.backend.repositories.CardRepository;
import com.example.backend.repositories.PurchaseRepository;
import com.example.backend.repositories.UserRepository;
import com.sun.security.auth.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PurchaseService {
    private final PurchaseRepository purchaseRepository;
    private final AppRepository appRepository;
    private final UserRepository userRepository;
    private final CardRepository cardRepository;

    public Purchase purchaseApp(UUID appId, UUID cardId, UserPrincipal currentUser) {
        App app = appRepository.findByIdAndAvailable(appId)
                .orElseThrow(() -> new AppNotFoundException("Приложение не найдено", new RuntimeException()));

        User user = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new UserNotFoundException("Пользователь не найден", new RuntimeException()));

        Card card = cardRepository.findByIdAndUser(cardId, user)
                .orElseThrow(() -> new CardNotFoundException("Карта не найдена", new RuntimeException()));

        if(card.getDeleted()) {
            throw new CardDeletedException("Карта удалена", new RuntimeException());
        }

        boolean alreadyPurchased = purchaseRepository.existsByUserAndApp(user, app);
        if(alreadyPurchased) {
            throw new AppAlreadyPurchasedException("Приложение уже куплено", new RuntimeException());
        }

        double newBalance = card.getBalance() - app.getPrice();
        cardRepository.save(newBalance);

        Purchase purchase = Purchase.builder()
                .app(app)
                .user(user)
                .card(card)
                .cost(app.getPrice())
                .description(app.getDescription())
                .creationTime(LocalDateTime.now())
                .build();

        return purchaseRepository.save(purchase);
    }

    public List<Purchase> getUserPurchases(UserPrincipal currentUser) {
        User user = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new UserNotFoundException("Пользователь не найден", new RuntimeException()));

        return purchaseRepository.findByUser(user);
    }

    public boolean hasUserPurchasedApp(User user, App app) {
        return purchaseRepository.existsByUserAndApp(user, app);
    }
}
