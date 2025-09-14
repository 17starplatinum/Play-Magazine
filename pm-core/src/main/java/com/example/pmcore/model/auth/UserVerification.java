package com.example.pmcore.model.auth;

import com.example.pmcore.services.util.VerificationCodeGenerator;
import jakarta.persistence.Column;
import jakarta.validation.constraints.Email;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@XmlAccessorType(XmlAccessType.FIELD)
public class UserVerification {

    @Column(name = "id", nullable = false)
    @XmlElement(name = "id")
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