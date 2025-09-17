package com.example.pmcore.services.aspect;

import com.example.pmcore.dto.auth.SignInRequest;
import com.example.pmcore.security.auth.AuthenticationService;
import com.example.pmcore.services.auth.UserService;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Aspect
@Component
@RequiredArgsConstructor
public class CamundaAuthAspect {
    private final AuthenticationService authenticationService;
    private final UserService userService;
    private final IdentityService identityService;

    @Around("@within(com.example.pmcore.security.RequiresCamundaAuth) || @annotation(com.example.pmcore.security.RequiresCamundaAuth)")
    public Object aroundCamundaDelegates(ProceedingJoinPoint pjp) throws Throwable {
        DelegateExecution execution = Arrays.stream(pjp.getArgs())
                .filter(DelegateExecution.class::isInstance)
                .map(DelegateExecution.class::cast)
                .findFirst()
                .orElse(null);

        if (execution == null) {
            return pjp.proceed();
        }

        String username = (String) execution.getVariable("UsernameField");
        String password = (String) execution.getVariable("PasswordField");

        if (username == null || password == null) {
            return pjp.proceed();
        }

        authenticationService.signIn(new SignInRequest(username, password));

        identityService.setAuthenticatedUserId(username);

        UserDetails userDetails = userService.getByUsername(username);
        UsernamePasswordAuthenticationToken token =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

        SecurityContext originalContext = SecurityContextHolder.getContext();
        SecurityContext newContext = new SecurityContextImpl();
        newContext.setAuthentication(token);
        SecurityContextHolder.setContext(newContext);

        try {
            return pjp.proceed();
        } finally {
            SecurityContextHolder.setContext(originalContext);
            identityService.setAuthenticatedUserId(null);
        }
    }
}
