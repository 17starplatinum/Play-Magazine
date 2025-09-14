package com.example.backend.dto.auth.file;

import jakarta.xml.bind.annotation.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@XmlRootElement(name = "user")
@XmlAccessorType(XmlAccessType.FIELD)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserFileDto {
    @XmlElement
    private UUID id;

    @XmlElement
    private String email;

    @XmlElement
    private String password;

    @XmlElement
    private String role;

    @XmlElement
    private boolean enableTwoFA;

    @XmlElement
    private String requestStatus;

    @XmlElement
    private UUID userBudgetId;

    @XmlElement
    private UUID userProfileId;

    @XmlElement
    private UUID userVerificationId;

    @XmlElementWrapper(name = "downloadedAppIds")
    @XmlElement(name = "appId")
    private Set<UUID> downloadedAppIds = new HashSet<>();

    @XmlElementWrapper(name = "userSubscriptionIds")
    @XmlElement(name = "subscriptionId")
    private Set<UUID> userSubscriptionIds = new HashSet<>();
}
