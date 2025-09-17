package com.example.pmcore.delegates;

import com.example.pmcore.model.auth.Role;
import com.example.pmcore.services.auth.RoleManagementService;
import lombok.RequiredArgsConstructor;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component("roleTypesDelegate")
@RequiredArgsConstructor
public class RoleTypesDelegate implements JavaDelegate {

    private final RoleManagementService roleManagementService;

    @Override
    public void execute(DelegateExecution execution) {
        List<Role> availableRoles = roleManagementService.getAvailableRoles();
        execution.setVariable("availableRoles", availableRoles);
    }
}
