package com.example.pmcore.security.auth;

import com.example.pmcore.dto.auth.*;
import com.example.pmcore.model.auth.Role;
import com.example.pmcore.model.auth.User;
import com.example.pmcore.model.auth.UserProfile;
import com.example.pmcore.model.auth.UserVerification;
import com.example.pmcore.repositories.auth.file.FileBasedUserProfileRepository;
import com.example.pmcore.repositories.auth.file.FileBasedUserRepository;
import com.example.pmcore.security.jwt.JwtService;
import com.example.pmcore.services.auth.UserService;
import com.example.pmcore.services.auth.UserVerificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private final FileBasedUserProfileRepository userProfileRepository;
    private final FileBasedUserRepository userRepository;

    /**
     * Регистрация пользователя
     *
     * @param request данные пользователя
     * @return токен
     */
    public JwtAuthenticationResponse signUp(SignUpRequest request) {

        var user = User.builder()
                .id(UUID.randomUUID())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.USER)
                .build();

        userService.create(user, request);

        var jwt = jwtService.generateToken(user);
        return new JwtAuthenticationResponse(jwt);
    }

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
    public void change2FAStatus(boolean enabled, String jwt) {
        userRepository.enableTwoFA(enabled, jwtService.extractUserName(jwt));
    }

    @Transactional
    public void updateUserInfo(EditProfileRequest request, String token) {
        String username = request.getEmail();
        token = token.substring(7);
        String email = jwtService.extractUserName(token);
        if (!userService.getByUsername(email).getEmail().equals(username)) {
            throw new IllegalArgumentException("Something went wrong!");
        }
        String newName = request.getName();
        LocalDate newBirthday = request.getBirthday();
        String newSurname = request.getSurname();
        String newPassword = request.getNewPassword();
        String oldPassword = request.getPassword();
        User user = userService.getByUsername(username);
        UserProfile userProfile = userProfileRepository.findById(user.getUserProfileId());

        if (newPassword != null && oldPassword != null) {
            if (!passwordEncoder.matches(oldPassword, user.getPassword()))
                throw new IllegalArgumentException("Password is incorrect!");

            user.setPassword(passwordEncoder.encode(newPassword));
        }

        if (newSurname != null) userProfile.setSurname(newSurname);
        if (newName != null) userProfile.setName(newName);
        if (newBirthday != null) userProfile.setBirthday(newBirthday);

        userProfileRepository.save(userProfile);
    }

    public UserInfoResponse getUserInfoByJwtToken(String token) {
        String email = jwtService.extractUserName(token);
        User user = userService.getByUsername(email);
        UserProfile userProfile = userProfileRepository.findById(user.getUserProfileId());

        return UserInfoResponse.builder()
                .id(user.getId())
                .email(email)
                .name(userProfile.getName())
                .surname(userProfile.getSurname())
                .birthday(userProfile.getBirthday())
                .build();
    }
}