package com.example.pmcore.services.data;

import com.example.pmcore.dto.data.bitrix.BitrixBankDetailResponse;
import com.example.pmcore.dto.data.bitrix.BitrixCardResponse;
import com.example.pmcore.dto.data.bitrix.CardResponse;
import com.example.pmcore.dto.data.card.CardDto;
import com.example.pmcore.dto.data.card.DepositRequest;
import com.example.pmcore.exceptions.notfound.CardNotFoundException;
import com.example.pmcore.exceptions.prerequisites.CardAlreadyExistsException;
import com.example.pmcore.jca.BitrixConnection;
import com.example.pmcore.jca.BitrixConnectionFactory;
import com.example.pmcore.mappers.CardMapper;
import com.example.pmcore.model.auth.User;
import com.example.pmcore.model.data.finances.Card;
import com.example.pmcore.repositories.data.finances.CardRepository;
import com.example.pmcore.services.auth.UserService;
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
    private final BitrixConnectionFactory bitrixConnectionFactory;
    private final ObjectMapper objectMapper;

    public Card getCardByIdAndUser(UUID id, UUID userId) {
        return cardRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new CardNotFoundException("Карта не найдена"));
    }

    public Card save(Card card) {
        return cardRepository.save(card);
    }

    public Card addCard(CardDto cardDto) {
        User user = userService.getCurrentUser();
        String token = "mxtqnq3g6b5q4a8q";
        String endpoint = "/rest/1/" + token + "/crm.requisite.bankdetail.add.json";

        if (cardRepository.existsByUserIdAndNumber(user.getId(), cardDto.getNumber())) {
            throw new CardAlreadyExistsException("Карта с таким номером уже существует");
        }

        Card card = cardMapper.mapToModel(user, cardDto);
        if (getUserCards().isEmpty()) {
            card.setIsDefault(true);
        }
        Card saved = save(card);

        String jsonBody = "{ \"fields\": { " +
                "\"ENTITY_ID\": 2, " +
                "\"NAME\": \"" + card.getUserId() + "\", " +
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
        UUID userId = userService.getCurrentUserId();
        return cardRepository.findByUserId(userId);
    }

    public void depositInCard(DepositRequest depositRequest) {
        UUID userId = userService.getCurrentUserId();
        Card card = getCardByIdAndUser(depositRequest.getCardId(), userId);
        card.setBalance(card.getBalance() + depositRequest.getAmount());
        save(card);
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

    public BitrixCardResponse getAllCards() {
        String token = "rek8a5hw0pi7dkrv";
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
        String token = "8uow3z4cz6a3x4ni";
        String endpoint = "/rest/1/" + token + "/crm.requisite.bankdetail.delete.json";

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

        UUID userId = userService.getCurrentUserId();
        Card card = getCardByIdAndUser(cardId, userId);
        cardRepository.delete(card);
    }
}
