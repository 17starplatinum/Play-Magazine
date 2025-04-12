package com.example.backend.controllers;

import com.example.backend.dto.auth.*;
import com.example.backend.model.auth.User;
import com.example.backend.model.auth.UserVerification;
import com.example.backend.security.auth.AuthenticationService;
import com.example.backend.services.auth.RoleManagementService;
import com.example.backend.services.auth.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthenticationService authenticationService;
    private final RoleManagementService roleManagementService;
    private final UserService userService;

    @PostMapping("/register")
    public JwtAuthenticationResponse signUp(@RequestBody @Valid SignUpRequest request) {
        return authenticationService.signUp(request);
    }

    @PostMapping("/login")
    public ResponseEntity<?> signIn(@RequestBody @Valid SignInRequest request) {
        authenticationService.signIn(request);
        String email = request.getEmail();

        if (authenticationService.is2FAEnable(email)) {
            UserVerification userVerification = authenticationService.createUserVerification(email);
            HttpHeaders headers = new HttpHeaders();
            headers.setLocation(URI.create("/auth/2fa?email=" + email + "&codeId=" + userVerification.getId()));
            return new ResponseEntity<>(headers, HttpStatus.FOUND);
        }
        return ResponseEntity.ok().body(
                new JwtAuthenticationResponse(authenticationService.generateToken(email))
        );
    }

    @GetMapping("/2fa")
    public ResponseEntity<?> check2FAForm(
            @RequestParam("email") String email,
            @RequestParam("codeId") String codeId
    ) {
        return ResponseEntity.ok().body(new CodeVerificationResponse(email, codeId));
    }

    @PostMapping("/request")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<String> requestAuthorRole(@RequestParam String requestedRole) {
        User currentUser = userService.getCurrentUser();
        roleManagementService.requestRole(currentUser.getId(), requestedRole);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body("Заявка успешно подана");
    }

    @PostMapping("/2fa")
    public ResponseEntity<?> check2FA(
            @RequestBody @Valid CodeVerificationRequest request
    ) {
        if (authenticationService.check2FA(request)) {
            HttpHeaders headers = new HttpHeaders();
            headers.setLocation(URI.create("/auth/success?email=" + request.getEmail()));
            return new ResponseEntity<>(headers, HttpStatus.FOUND);
        }

        return ResponseEntity.badRequest().body("Wrong code!");
    }

    @PatchMapping("/edit-info")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<String> updateUserInfo(@RequestBody EditProfileRequest request) {
        authenticationService.updateUserInfo(request);
        return ResponseEntity.ok("Информация успешно обновлена");
    }

    @GetMapping("/success")
    public ResponseEntity<?> success(@RequestParam("email") String email) {
        return ResponseEntity.ok().body(
                new JwtAuthenticationResponse(authenticationService.generateToken(email))
        );
    }

    @ExceptionHandler
    public ResponseEntity<String> handler(Exception e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }
}
