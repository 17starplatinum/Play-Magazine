package com.example.backend.services.data;

import com.example.backend.dto.data.card.CardDto;
import com.example.backend.dto.data.card.DepositRequest;
import com.example.backend.exceptions.notfound.UserNotFoundException;
import com.example.backend.exceptions.prerequisites.CardAlreadyExistsException;
import com.example.backend.exceptions.notfound.CardNotFoundException;
import com.example.backend.model.auth.User;
import com.example.backend.model.data.Card;
import com.example.backend.repositories.CardRepository;
import com.example.backend.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CardService {
    private static final String USER_NOT_FOUND_MSG = "Пользователь не найден";
    private final CardRepository cardRepository;
    private final UserRepository userRepository;

    public Card addCard(CardDto cardDto, UserDetails currentUser) {
        User user = userRepository.findByEmail(currentUser.getUsername())
                .orElseThrow(() -> new UserNotFoundException(USER_NOT_FOUND_MSG, new RuntimeException()));
        if (cardRepository.existsByUserAndNumber(user, cardDto.getNumber())) {
            throw new CardAlreadyExistsException("Карта с таким номером уже существует", new RuntimeException());
        }
        Card card = Card.builder()
                .user(user)
                .number(cardDto.getNumber())
                .cvv(cardDto.getCvv())
                .expired(cardDto.getExpired())
                .build();
        return cardRepository.save(card);
    }

    public List<Card> getUserCards(UserDetails currentUser) {
        User user = userRepository.findByEmail(currentUser.getUsername())
                .orElseThrow(() -> new UserNotFoundException(USER_NOT_FOUND_MSG, new RuntimeException()));

        return cardRepository.findByUser(user);
    }

    public void depositInCard(DepositRequest depositRequest, UserDetails currentUser) {
        User user = userRepository.findByEmail(currentUser.getUsername())
                .orElseThrow(() -> new UserNotFoundException(USER_NOT_FOUND_MSG, new RuntimeException()));
        Card card = cardRepository.findByIdAndUser(depositRequest.getCardId(), user)
                .orElseThrow(() -> new CardNotFoundException("Карта не найдена", new RuntimeException()));

        card.setBalance(card.getBalance() + depositRequest.getAmount());
        cardRepository.save(card);
    }

    public void deleteCard(UUID cardId, UserDetails currentUser) {
        User user = userRepository.findByEmail(currentUser.getUsername())
                .orElseThrow(() -> new UserNotFoundException(USER_NOT_FOUND_MSG, new RuntimeException()));

        Card card = cardRepository.findByIdAndUser(cardId, user)
                .orElseThrow(() -> new CardNotFoundException("Карта не найдена", new RuntimeException()));

        cardRepository.delete(card);
    }
}
