package com.example.backend.services.data;

import com.example.backend.dto.data.CardDto;
import com.example.backend.exceptions.CardNotFoundException;
import com.example.backend.exceptions.CardAlreadyExistsException;
import com.example.backend.exceptions.UserNotFoundException;
import com.example.backend.model.data.Card;
import com.example.backend.model.auth.User;
import com.example.backend.repositories.CardRepository;
import com.example.backend.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.nio.file.attribute.UserPrincipal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CardService {
    private final CardRepository cardRepository;
    private final UserRepository userRepository;

    public Card addCard(CardDto cardDto, UserPrincipal currentUser) {
        User user = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new UserNotFoundException("Пользователь не найден", new RuntimeException()));
        if(cardRepository.existsByUserAndNumberAndDeletedFalse(user, cardDto.getNumber())) {
            throw new CardAlreadyExistsException("Карта с таким номером уже существует", new RuntimeException());
        }
        Card card = Card.builder()
                .user(user)
                .number(cardDto.getNumber())
                .cvv(cardDto.getCvv())
                .expired(cardDto.getExpired())
                .deleted(false)
                .build();
        return cardRepository.save(card);
    }

    public List<Card> getUserCards(UserPrincipal currentUser) {
        User user = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new UserNotFoundException("Пользователь не найден", new RuntimeException()));

        return cardRepository.findByUserAndDeletedFalse(user);
    }

    public void deleteCard(UUID cardId, UserPrincipal currentUser) {
        User user = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new UserNotFoundException("Пользователь не найден", new RuntimeException()));

        Card card = cardRepository.findByIdAndUser(cardId, user)
                .orElseThrow(() -> new CardNotFoundException("Карта не найдена", new RuntimeException()));

        card.setDeleted(true);
        cardRepository.save(card);
    }


}
