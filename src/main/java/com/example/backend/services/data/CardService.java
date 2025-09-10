package com.example.backend.services.data;

import com.example.backend.dto.data.bitrix.BitrixBankDetailResponse;
import com.example.backend.dto.data.bitrix.BitrixCardResponse;
import com.example.backend.dto.data.bitrix.CardResponse;
import com.example.backend.dto.data.card.CardDto;
import com.example.backend.dto.data.card.DepositRequest;
import com.example.backend.exceptions.notfound.CardNotFoundException;
import com.example.backend.exceptions.prerequisites.CardAlreadyExistsException;
import com.example.backend.jca.BitrixConnection;
import com.example.backend.jca.BitrixConnectionFactory;
import com.example.backend.mappers.CardMapper;
import com.example.backend.model.auth.User;
import com.example.backend.model.data.finances.Card;
import com.example.backend.repositories.data.finances.CardRepository;
import com.example.backend.services.auth.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
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
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CardService {

    private final CardRepository cardRepository;
    private final UserService userService;
    private final CardMapper cardMapper;
    private final PlatformTransactionManager transactionManager;
    private final DefaultTransactionDefinition definition;
    private final BitrixConnectionFactory bitrixConnectionFactory; // ← JCA вместо RestClient
    private final ObjectMapper objectMapper;

    public Card getCardByIdAndUser(UUID id, User user) {
        return cardRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new CardNotFoundException("Карта не найдена"));
    }

    public Card addCard(CardDto cardDto) {
        User user = userService.getCurrentUser();
        String token = "mxtqnq3g6b5q4a8q"; // Токен для add
        String endpoint = "/rest/1/" + token + "/crm.requisite.bankdetail.add.json";

        if (cardRepository.existsByUserAndNumber(user, cardDto.getNumber())) {
            throw new CardAlreadyExistsException("Карта с таким номером уже существует");
        }

        Card card = cardMapper.mapToModel(user, cardDto);
        if (getUserCards().isEmpty()) {
            card.setIsDefault(true);
        }
        Card saved = cardRepository.save(card);

        String jsonBody = "{ \"fields\": { " +
                "\"ENTITY_ID\": 2, " +
                "\"NAME\": \"" + card.getUser().getId() + "\", " +
                "\"XML_ID\": \"" + card.getId() + "\", " +
                "\"RQ_COR_ACC_NUM\": \"" + card.getExpiryDate().getDayOfMonth() + "/" + card.getExpiryDate().getYear() + "\", " +
                "\"RQ_ACC_NUM\": \"" + card.getNumber() + "\" " +
                "} }";

        BitrixConnection connection = null;
        try {
            connection = bitrixConnectionFactory.getConnection(token);
            connection.callApi(endpoint, jsonBody);
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при добавлении карты в Bitrix24", e);
        } finally {
            if (connection != null) {
                connection.close();
            }
        }

        return saved;
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

        if (cardRepository.findByUser(user).size() == 1) {
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

    public BitrixCardResponse getAllCards() {
        String token = "rek8a5hw0pi7dkrv"; // Токен для list
        String endpoint = "/rest/1/" + token + "/crm.requisite.bankdetail.list.json";

        String jsonBody = "{ " +
                "\"filter\": { \"NAME\": \"" + userService.getCurrentUser().getId() + "\" }, " +
                "\"select\": [\"ID\", \"RQ_ACC_NUM\", \"RQ_COR_ACC_NUM\", \"XML_ID\"] " +
                "}";

        BitrixConnection connection = null;
        try {
            connection = bitrixConnectionFactory.getConnection(token);
            String bitrixResponse = connection.callApi(endpoint, jsonBody);

            BitrixBankDetailResponse response = objectMapper.readValue(bitrixResponse, BitrixBankDetailResponse.class);
            if (response.getBankDetails() == null) {
                return new BitrixCardResponse(List.of());
            }

            return new BitrixCardResponse(
                    response.getBankDetails().stream()
                            .map(detail -> new CardResponse(
                                    detail.getId(),
                                    detail.getUuid(),
                                    detail.getNumber(),
                                    detail.getExpiredDate()
                            ))
                            .filter(card -> card.getUuid() != null && card.getNumber() != null)
                            .collect(Collectors.toList())
            );
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при получении карт из Bitrix24", e);
        } finally {
            if (connection != null) {
                connection.close();
            }
        }
    }

    public void deleteCard(UUID cardId) {
        String token = "8uow3z4cz6a3x4ni"; // Токен для delete
        String endpoint = "/rest/1/" + token + "/crm.requisite.bankdetail.delete.json";

        // Получаем ID карты из Bitrix
        CardResponse removedCard = getAllCards().getBankDetails().stream()
                .filter(card -> UUID.fromString(card.getUuid()).equals(cardId))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("Карта с uuid " + cardId + " не найдена"));

        String jsonBody = "{ \"id\": " + removedCard.getId() + " }";

        BitrixConnection connection = null;
        try {
            connection = bitrixConnectionFactory.getConnection(token);
            connection.callApi(endpoint, jsonBody);
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при удалении карты в Bitrix24", e);
        } finally {
            if (connection != null) {
                connection.close();
            }
        }

        User user = userService.getCurrentUser();
        Card card = getCardByIdAndUser(cardId, user);
        cardRepository.delete(card);
    }
}