package com.example.backend.controllers;

import com.example.backend.dto.auth.*;
import com.example.backend.model.data.UserVerification;
import com.example.backend.security.auth.AuthenticationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthenticationService authenticationService;

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
