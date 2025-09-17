package com.example.pmcore.delegates;

import com.example.pmcore.security.RequiresCamundaAuth;
import com.example.pmcore.services.auth.RoleManagementService;
import lombok.RequiredArgsConstructor;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

@RequiresCamundaAuth
@Component("grantRoleDelegate")
@RequiredArgsConstructor
public class GrantRoleDelegate implements JavaDelegate {
    private final RoleManagementService roleManagementService;

    @Override
    public void execute(DelegateExecution execution) {
        String username = (String) execution.getVariable("UsernameField");
        String roleName = (String) execution.getVariable("RoleName");
        roleManagementService.grantRole(username, roleName);
    }
}
