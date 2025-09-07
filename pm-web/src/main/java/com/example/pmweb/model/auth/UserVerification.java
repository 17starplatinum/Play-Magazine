package com.example.pmweb.model.auth;

import com.example.pmweb.services.util.VerificationCodeGenerator;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

@Entity
@Table(name = "user_verification")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserVerification {

    @Id
    @UuidGenerator
    @Column(name = "id", nullable = false)
    private UUID id;

    @Email(message = "Email адрес должен быть в формате user@example.com")
    @Column(name = "email", nullable = false)
    private String email;

    @Builder.Default
    @Column(name = "code", nullable = false)
    private String verificationCode = VerificationCodeGenerator.generateVerificationCode();

    @Builder.Default
    @Column(name = "creation_time", nullable = false)
    private long creationTime = System.currentTimeMillis();

    @Builder.Default
    @Column(name = "enable", nullable = false)
    private boolean isEnable = true;

    @Builder.Default
    @Column(name = "failed_attempts", nullable = false)
    private Integer failedAttempts = 0;
}