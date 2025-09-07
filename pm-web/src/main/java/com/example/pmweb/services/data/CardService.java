package com.example.pmweb.services.data;

import com.example.pmweb.dto.data.card.CardDto;
import com.example.pmweb.dto.data.card.DepositRequest;
import com.example.pmweb.exceptions.notfound.CardNotFoundException;
import com.example.pmweb.exceptions.prerequisites.CardAlreadyExistsException;
import com.example.pmweb.mappers.CardMapper;
import com.example.pmweb.model.auth.User;
import com.example.pmweb.model.data.finances.Card;
import com.example.pmweb.repositories.data.finances.CardRepository;
import com.example.pmweb.services.auth.UserService;
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

    public Card getCardByIdAndUser(UUID id, User user) {
        return cardRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new CardNotFoundException("Карта не найдена"));
    }

    public Card addCard(CardDto cardDto) {
        User user = userService.getCurrentUser();
        if (cardRepository.existsByUserAndNumber(user, cardDto.getNumber())) {
            throw new CardAlreadyExistsException("Карта с таким номером уже существует");
        }
        Card card = cardMapper.mapToModel(user, cardDto);
        if(getUserCards().isEmpty()) {
            card.setIsDefault(true);
        }
        return cardRepository.save(card);
    }

    public List<Card> getUserCards() {
        User user = userService.getCurrentUser();

        return cardRepository.findByUser(user);
    }

    public void depositInCard(DepositRequest depositRequest) {
        User user = userService.getCurrentUser();
        Card card = getCardByIdAndUser(depositRequest.getCardId(), user);

        card.setBalance(card.getBalance() + depositRequest.getAmount());
        cardRepository.save(card);
    }

    public Optional<Card> getCardByDefault() {
        User user = userService.getCurrentUser();
        return cardRepository.findByUserAndIsDefaultTrue(user);
    }

    @Transactional
    public void setDefaultCard(UUID cardId) {
        TransactionStatus transaction = transactionManager.getTransaction(definition);
        User user = userService.getCurrentUser();

        if(cardRepository.findByUser(user).size() == 1) {
            cardRepository.setDefaultCard(cardId);
            transactionManager.commit(transaction);
            return;
        }

        Card card = getCardByIdAndUser(cardId, user);

        if (!card.getUser().getId().equals(user.getId())) {
            transactionManager.rollback(transaction);
            throw new AccessDeniedException("Карта не принадлежит пользователю");
        }

        if (!cardRepository.existsByIdAndUser(cardId, user)) {
            transactionManager.rollback(transaction);
            throw new CardNotFoundException("Карта не найдена");
        }
        cardRepository.clearDefaultFlags(user.getId());
        cardRepository.setDefaultCard(cardId);
        transactionManager.commit(transaction);
    }

    public void deleteCard(UUID cardId) {
        User user = userService.getCurrentUser();

        Card card = getCardByIdAndUser(cardId, user);

        cardRepository.delete(card);
    }
}
