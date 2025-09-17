package com.example.pmcore.configs;

import com.example.pmcore.model.auth.Role;
import com.example.pmcore.security.jwt.JwtAuthenticationFilter;
import com.example.pmcore.services.auth.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;

import java.util.List;

import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfiguration {
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final UserService userService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(request -> {
                    var corsConfiguration = new CorsConfiguration();
                    corsConfiguration.setAllowedOriginPatterns(List.of("*"));
                    corsConfiguration.setAllowedMethods(List.of("GET", "POST", "DELETE", "PUT", "OPTIONS"));
                    corsConfiguration.setAllowedHeaders(List.of("*"));
                    corsConfiguration.setAllowCredentials(false);
                    return corsConfiguration;
                }))
                .cors(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(request -> request
                        .requestMatchers("/api/v1/auth/**").permitAll()
                        .requestMatchers("/test/**").hasAuthority(Role.ADMIN.toString())
                        .requestMatchers("/api/v1/admin/change-role").hasAuthority(Role.ADMIN.toString())
                        .requestMatchers("/api/v1/admin/admin-requests/status").authenticated()
                        .requestMatchers("/api/v1/admin/available-roles").authenticated()
                        .requestMatchers("/api/v1/admin/**").hasAnyAuthority(
                                Role.ADMIN.toString(),
                                Role.MODERATOR.toString()
                        )
                        .requestMatchers(HttpMethod.GET,
                                "/api/v1/apps/*/update-info",
                                "/api/v1/apps/*/download").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/v1/apps/*/reviews").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/apps/*/reviews").authenticated()
                        .requestMatchers(HttpMethod.GET,
                                "/api/v1/apps",
                                "/api/v1/*/compatibility",
                                "/api/v1/apps/*",
                                "/api/v1/apps/*/reviews",
                                "/api/v1/apps/*/reviews/average"
                        ).permitAll()
                        .requestMatchers(HttpMethod.PUT, "/api/v1/apps").hasAnyAuthority(
                                Role.ADMIN.toString(),
                                Role.DEVELOPER.toString()
                        ).requestMatchers(HttpMethod.PUT, "/api/v1/apps/*").hasAnyAuthority(
                                Role.ADMIN.toString(),
                                Role.DEVELOPER.toString()
                        ).requestMatchers(HttpMethod.DELETE, "/api/v1/apps/*").hasAnyAuthority(
                                Role.ADMIN.toString(),
                                Role.DEVELOPER.toString(),
                                Role.MODERATOR.toString()
                        )
                        .requestMatchers("/api/v1/cards/**").authenticated()
                        .requestMatchers("/api/v1/purchases/**").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/subscriptions/**").hasAnyAuthority(
                                Role.DEVELOPER.toString(),
                                Role.MODERATOR.toString(),
                                Role.ADMIN.toString()
                        )
                        .requestMatchers("/api/v1/subscriptions/**").authenticated()
                        .requestMatchers("/api/v1/budget/**").authenticated()
                        .anyRequest().permitAll())
                .sessionManagement(manager -> manager.sessionCreationPolicy(STATELESS))
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(userService.userDetailsService());
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
            throws Exception {
        return config.getAuthenticationManager();
    }
}