package com.example.pmcore.delegates;

import com.example.pmcore.security.RequiresCamundaAuth;
import com.example.pmcore.services.auth.RoleManagementService;
import com.example.pmcore.services.auth.UserService;
import lombok.RequiredArgsConstructor;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

import java.util.Map;

@RequiresCamundaAuth
@Component("roleRequestDelegate")
@RequiredArgsConstructor
public class RoleRequestDelegate implements JavaDelegate {
    private final RuntimeService runtimeService;
    private final RoleManagementService roleManagementService;
    private final UserService userService;
    private static final String PROCESS_KEY = "role-request";

    @Override
    public void execute(DelegateExecution execution) {
        runtimeService.startProcessInstanceByKey(PROCESS_KEY, Map.of("UsernameField", userService.getCurrentUser().getUsername()));
        String response = roleManagementService.requestRole((String) execution.getVariable("RequestedRole"));
        execution.setVariable("response", response);
    }
}
