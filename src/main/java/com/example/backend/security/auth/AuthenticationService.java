package com.example.backend.security.auth;

import com.example.backend.dto.auth.*;
import com.example.backend.model.auth.Role;
import com.example.backend.model.auth.User;
import com.example.backend.model.data.UserVerification;
import com.example.backend.security.jwt.JwtService;
import com.example.backend.services.UserVerificationService;
import com.example.backend.services.auth.UserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
    private final UserService userService;
    private final UserVerificationService userVerificationService;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    /**
     * Регистрация пользователя
     *
     * @param request данные пользователя
     * @return токен
     */
    public JwtAuthenticationResponse signUp(SignUpRequest request) {

        var user = User.builder()
                .name(request.getName())
                .surname(request.getSurname())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.USER)
                .build();

        userService.create(user);

        var jwt = jwtService.generateToken(user);
        return new JwtAuthenticationResponse(jwt);
    }

    /*public JwtAuthenticationResponse check2FA(CodeVerificationRequest request) {

    }*//*

    public JwtAuthenticationResponse signing(SignInRequest request) {
        String email = request.getEmail();
        String password = request.getPassword();

        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                email,
                password
        ));

        UserDetails user = userService
                .userDetailsService()
                .loadUserByUsername(email);

        var jwt = jwtService.generateToken(user);
        userVerificationService
                .getVerificationIdByEmail(email)
                .ifPresent(userVerificationService::deleteVerificationCode);

        UserVerification userVerification = userVerificationService.createUserVerification(email);

        emailService.sendVerificationEmail(email, userVerification.getVerificationCode());

        return new JwtAuthenticationResponse(jwt);
    }
*/

    /**
     * Аутентификация пользователя
     *
     * @param request данные пользователя
     */
    public void signIn(SignInRequest request) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                request.getEmail(),
                request.getPassword()
        ));
    }

    public String generateToken(String email) {
        var user = userService
                .userDetailsService()
                .loadUserByUsername(email);

        return jwtService.generateToken(user);
    }

    public boolean is2FAEnable(String email) {
        return userService.getByUsername(email).isEnableTwoFA();
    }

    public UserVerification createUserVerification(String email) {
        return userVerificationService.createUserVerification(email);
    }

    public boolean check2FA(CodeVerificationRequest request) {
        return userVerificationService.isValidVerificationCode(
                request.getVerificationCodeId(),
                request.getCode(),
                request.getEmail()
        );
    }

    @Transactional
    public void updateUserInfo(EditProfileRequest request) {
        UUID uuid = request.getId();
        String token = request.getToken();
        token = token.substring(7);
        String email = jwtService.extractUserName(token);
        if (!userService.getByUsername(email).getId().equals(uuid)) {
            throw new IllegalArgumentException("Something went wrong!");
        }
        String newName = request.getName();
        LocalDate newBirthday = request.getBirthday();
        String newSurname = request.getSurname();
        String newPassword = request.getNewPassword();
        String oldPassword = request.getPassword();
        User user = userService.getById(uuid);

        if (newPassword != null && oldPassword != null) {
            if (!passwordEncoder.matches(oldPassword, user.getPassword()))
                throw new IllegalArgumentException("Password is incorrect!");

            user.setPassword(passwordEncoder.encode(newPassword));
        }

        if (newSurname != null) user.setSurname(newSurname);
        if (newName != null) user.setName(newName);
        if (newBirthday != null) user.setBirthday(newBirthday);

        userService.save(user);
    }

    public UserInfoResponse getUserInfoByJwtToken(String token) {
        String email = jwtService.extractUserName(token);
        User user = userService.getByUsername(email);

        return UserInfoResponse.builder()
                .id(user.getId())
                .email(email)
                .name(user.getName())
                .surname(user.getSurname())
                .birthday(user.getBirthday())
                .build();
    }
}