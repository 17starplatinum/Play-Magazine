package com.example.backend.dto.auth.file;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.*;

import java.util.UUID;

@XmlRootElement(name = "userVerification")
@XmlAccessorType(XmlAccessType.FIELD)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserVerificationFileDto {
    @XmlElement
    private UUID id;

    @XmlElement
    private String email;

    @XmlElement
    private String verificationCode;

    @XmlElement
    private long creationTime;

    @XmlElement
    private boolean isEnable;

    @XmlElement
    private Integer failedAttempts;
}
