package com.example.pmcore.delegates;

import com.example.pmcore.security.RequiresCamundaAuth;
import com.example.pmcore.services.data.CardService;
import lombok.RequiredArgsConstructor;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

import java.util.UUID;

@RequiresCamundaAuth
@Component("setDefaultCardDelegate")
@RequiredArgsConstructor
public class SetDefaultCardDelegate implements JavaDelegate {
    private final CardService cardService;

    @Override
    public void execute(DelegateExecution execution) {
        cardService.setDefaultCard((UUID.fromString(execution.getVariable("CardField").toString())));
    }
}
