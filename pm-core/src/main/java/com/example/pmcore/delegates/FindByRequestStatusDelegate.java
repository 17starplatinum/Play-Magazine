package com.example.pmcore.delegates;

import com.example.pmcore.dto.auth.RoleChangeRequestDto;
import com.example.pmcore.security.RequiresCamundaAuth;
import com.example.pmcore.services.auth.UserService;
import lombok.RequiredArgsConstructor;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

import java.util.List;

@RequiresCamundaAuth
@Component("findByRequestStatusDelegate")
@RequiredArgsConstructor
public class FindByRequestStatusDelegate implements JavaDelegate {

    private final UserService userService;

    @Override
    public void execute(DelegateExecution execution) {
        List<RoleChangeRequestDto> response = userService.findByRequestStatus(String.valueOf(execution.getVariable("RequestStatusList")));
        execution.setVariable("userRequests", response);
    }
}
