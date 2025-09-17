package com.example.pmcore.delegates;

import com.example.pmcore.model.data.finances.Card;
import com.example.pmcore.security.RequiresCamundaAuth;
import com.example.pmcore.services.data.CardService;
import lombok.RequiredArgsConstructor;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

import java.util.List;

@RequiresCamundaAuth
@Component("getUserCardsDelegate")
@RequiredArgsConstructor
public class GetUserCardsDelegate implements JavaDelegate {
    private final CardService cardService;

    @Override
    public void execute(DelegateExecution execution) {
        List<Card> cardResponse = cardService.getUserCards();
        execution.setVariable("cardResponse", cardResponse);
    }
}
