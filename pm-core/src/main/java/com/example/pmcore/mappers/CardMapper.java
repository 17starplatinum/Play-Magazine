package com.example.pmcore.mappers;

import com.example.backend.dto.data.card.CardDto;
import com.example.pmweb.model.auth.User;
import com.example.pmweb.model.data.finances.Card;
import org.springframework.stereotype.Component;

@Component
public class CardMapper {
    public Card mapToModel(User user, CardDto cardDto) {
        return Card.builder()
                .userId(user.getId())
                .number(cardDto.getNumber())
                .cvv(cardDto.getCvv())
                .expiryDate(cardDto.getExpiryDate())
                .build();
    }
}
