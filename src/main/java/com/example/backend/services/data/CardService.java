package com.example.backend.services.data;

import com.example.backend.dto.data.card.CardDto;
import com.example.backend.dto.data.card.DepositRequest;
import com.example.backend.exceptions.notfound.CardNotFoundException;
import com.example.backend.exceptions.prerequisites.CardAlreadyExistsException;
import com.example.backend.mappers.CardMapper;
import com.example.backend.model.auth.User;
import com.example.backend.model.data.finances.Card;
import com.example.backend.repositories.data.finances.CardRepository;
import com.example.backend.services.auth.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CardService {
    private final CardRepository cardRepository;
    private final UserService userService;
    private final CardMapper cardMapper;
    private final PlatformTransactionManager transactionManager;
    private final DefaultTransactionDefinition definition;

    public Card getCardByIdAndUser(UUID id, UUID userId) {
        return cardRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new CardNotFoundException("Карта не найдена"));
    }

    public Card addCard(CardDto cardDto) {
        User user = userService.getCurrentUser();
        if (cardRepository.existsByUserIdAndNumber(user.getId(), cardDto.getNumber())) {
            throw new CardAlreadyExistsException("Карта с таким номером уже существует");
        }
        Card card = cardMapper.mapToModel(user, cardDto);
        if(getUserCards().isEmpty()) {
            card.setIsDefault(true);
        }
        return cardRepository.save(card);
    }

    public List<Card> getUserCards() {
        UUID userId = userService.getCurrentUserId();
        return cardRepository.findByUserId(userId);
    }

    public void depositInCard(DepositRequest depositRequest) {
        UUID userId = userService.getCurrentUserId();
        Card card = getCardByIdAndUser(depositRequest.getCardId(), userId);

        card.setBalance(card.getBalance() + depositRequest.getAmount());
        cardRepository.save(card);
    }

    public Optional<Card> getCardByDefault() {
        UUID userId = userService.getCurrentUserId();
        return cardRepository.findByUserIdAndIsDefaultTrue(userId);
    }

    @Transactional
    public void setDefaultCard(UUID cardId) {
        TransactionStatus transaction = transactionManager.getTransaction(definition);
        UUID userId = userService.getCurrentUserId();

        if(cardRepository.findByUserId(userId).size() == 1) {
            cardRepository.setDefaultCard(cardId);
            transactionManager.commit(transaction);
            return;
        }

        Card card = getCardByIdAndUser(cardId, userId);

        if (!card.getUserId().equals(userId)) {
            transactionManager.rollback(transaction);
            throw new AccessDeniedException("Карта не принадлежит пользователю");
        }

        if (!cardRepository.existsByIdAndUserId(cardId, userId)) {
            transactionManager.rollback(transaction);
            throw new CardNotFoundException("Карта не найдена");
        }
        cardRepository.clearDefaultFlags(userId);
        cardRepository.setDefaultCard(cardId);
        transactionManager.commit(transaction);
    }

    public void deleteCard(UUID cardId) {
        UUID userId = userService.getCurrentUserId();
        Card card = getCardByIdAndUser(cardId, userId);
        cardRepository.delete(card);
    }
}
