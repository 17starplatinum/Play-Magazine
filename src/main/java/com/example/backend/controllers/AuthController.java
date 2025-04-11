package com.example.backend.controllers;

import com.example.backend.dto.auth.*;
import com.example.backend.model.auth.RequestStatus;
import com.example.backend.security.auth.AuthenticationService;
import com.example.backend.services.auth.RoleManagementService;
import com.example.backend.services.auth.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthenticationService authenticationService;
    private final RoleManagementService roleManagementService;
    private final UserService userService;

    @PostMapping("/sign-up")
    public JwtAuthenticationResponse signUp(@RequestBody @Valid SignUpRequest request) {
        return authenticationService.signUp(request);
    }

    @PostMapping("/sign-in")
    public JwtAuthenticationResponse signIn(@RequestBody @Valid SignInRequest request) {
        return authenticationService.signIn(request);
    }

    @PostMapping("/request")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<String> requestAuthorRole() {
        roleManagementService.requestRole(userService.getCurrentUser());
        return ResponseEntity.status(HttpStatus.ACCEPTED).body("Заявка успешно подана");
    }

    @GetMapping("/my-status")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<RequestStatus> getMyStatus() {
        return ResponseEntity.ok(userService.getCurrentUser().getRequestStatus());
    }

    @PostMapping("/edit-info")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<String> updateUserInfo(@RequestBody EditProfileRequest request) {
        authenticationService.updateUserInfo(request);
        return ResponseEntity.ok("Информация успешно обновлена");
    }

    @ExceptionHandler
    public ResponseEntity<String> handler(Exception e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }
}
