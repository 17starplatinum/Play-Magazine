package com.example.backend.security.auth;

import com.example.backend.dto.auth.*;
import com.example.backend.model.auth.Role;
import com.example.backend.model.auth.User;
import com.example.backend.security.jwt.JwtService;
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

    /**
     * Аутентификация пользователя
     *
     * @param request данные пользователя
     * @return токен
     */
    public JwtAuthenticationResponse signIn(SignInRequest request) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                request.getEmail(),
                request.getPassword()
        ));

        var user = userService
                .userDetailsService()
                .loadUserByUsername(request.getEmail());

        var jwt = jwtService.generateToken(user);
        return new JwtAuthenticationResponse(jwt);
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