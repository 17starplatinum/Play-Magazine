package com.example.backend.controllers;

import com.example.backend.dto.auth.*;
import com.example.backend.dto.data.ResponseDto;
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
import org.springframework.security.authentication.BadCredentialsException;
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
    public ResponseEntity<?> signUp(@RequestBody @Valid SignUpRequest request) {
        try {
            return ResponseEntity.ok().body(authenticationService.signUp(request));
        } catch (BadCredentialsException e) {
            return ResponseEntity.badRequest().body(new ResponseDto(e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<JwtAuthenticationResponse> signIn(@RequestBody @Valid SignInRequest request) {
        authenticationService.signIn(request);
        String email = request.getEmail();

        if (authenticationService.is2FAEnable(email)) {
            UserVerification userVerification = authenticationService.createUserVerification(email);
            HttpHeaders headers = new HttpHeaders();
            headers.setLocation(URI.create("/api/v1/auth/2fa?email=" + email + "&codeId=" + userVerification.getId()));
            return new ResponseEntity<>(headers, HttpStatus.FOUND);
        }
        return ResponseEntity.ok().body(
                new JwtAuthenticationResponse(authenticationService.generateToken(email))
        );
    }

    @GetMapping("/2fa")
    public ResponseEntity<CodeVerificationResponse> check2FAForm(
            @RequestParam("email") String email,
            @RequestParam("codeId") String codeId
    ) {
        return ResponseEntity.ok().body(new CodeVerificationResponse(email, codeId));
    }

    @PostMapping("/request")
    @PreAuthorize("hasAuthority('USER')")
    public ResponseEntity<ResponseDto> requestAuthorRole(@RequestParam String requestedRole) {
        User currentUser = userService.getCurrentUser();
        String response = roleManagementService.requestRole(currentUser.getId(), requestedRole);
        return ResponseEntity
                .status(HttpStatus.ACCEPTED)
                .body(new ResponseDto("Request successfully created!\n" + response));
    }

    @PostMapping("/2fa")
    public ResponseEntity<ResponseDto> check2FA(
            @RequestBody @Valid CodeVerificationRequest request
    ) {
        if (authenticationService.check2FA(request)) {
            HttpHeaders headers = new HttpHeaders();
            headers.setLocation(URI.create("/api/v1/auth/success?email=" + request.getEmail()));
            return new ResponseEntity<>(headers, HttpStatus.FOUND);
        }

        return ResponseEntity.badRequest().body(new ResponseDto("Wrong code!"));
    }

    @PutMapping("/edit-info")
    public ResponseEntity<ResponseDto> updateUserInfo(
            @RequestBody EditProfileRequest request,
            @RequestHeader("Authorization") String jwt
    ) {
        authenticationService.updateUserInfo(request, jwt);
        return ResponseEntity.ok().body(new ResponseDto("Data has been successfully updated!"));
    }

    @GetMapping("/edit-info")
    public ResponseEntity<Void> enable2FA(
            @RequestParam("2fa") boolean enabled,
            @RequestHeader("Authorization") String jwt
    ) {
        jwt = jwt.replace("Bearer ", "");
        authenticationService.change2FAStatus(enabled, jwt);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/success")
    public ResponseEntity<JwtAuthenticationResponse> success(@RequestParam("email") String email) {
        return ResponseEntity.ok().body(
                new JwtAuthenticationResponse(authenticationService.generateToken(email))
        );
    }

    @ExceptionHandler
    public ResponseEntity<String> handler(Exception e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }
}
