package com.example.pmcore.mappers;

import com.example.pmcore.dto.data.card.CardDto;
import com.example.pmcore.model.auth.User;
import com.example.pmcore.model.data.finances.Card;
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
