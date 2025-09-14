package com.example.pmcore.dto.auth.file;

import jakarta.xml.bind.annotation.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@XmlRootElement(name = "userDatabase")
@XmlAccessorType(XmlAccessType.FIELD)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDatabaseFileDto {
    @Builder.Default
    @XmlElementWrapper(name = "users")
    @XmlElement(name = "user")
    private List<UserFileDto> users = new ArrayList<>();

    @Builder.Default
    @XmlElementWrapper(name = "budgets")
    @XmlElement(name = "budget")
    private List<UserBudgetFileDto> budgets = new ArrayList<>();

    @Builder.Default
    @XmlElementWrapper(name = "profiles")
    @XmlElement(name = "profile")
    private List<UserProfileFileDto> profiles = new ArrayList<>();

    @Builder.Default
    @XmlElementWrapper(name = "verifications")
    @XmlElement(name = "verification")
    private List<UserVerificationFileDto> verifications = new ArrayList<>();

    public UserFileDto findUserById(UUID id) {
        return users.stream().filter(u -> u.getId().equals(id)).findFirst().orElse(null);
    }

    public UserBudgetFileDto findBudgetById(UUID id) {
        return budgets.stream().filter(b -> b.getId().equals(id)).findFirst().orElse(null);
    }

    public UserProfileFileDto findProfileByUserId(UUID id) {
        return profiles.stream().filter(p -> p.getId().equals(id)).findFirst().orElse(null);
    }

    public UserVerificationFileDto findVerificationByIdAndEmail(UUID id, String email) {
        return verifications.stream().filter(v -> v.getId().equals(id) && v.getEmail().equals(email)).findFirst().orElse(null);
    }

    public UserVerificationFileDto findVerificationById(UUID id) {
        return verifications.stream().filter(v -> v.getId().equals(id)).findFirst().orElse(null);
    }

    public UserVerificationFileDto findVerificationByEmail(String email) {
        return verifications.stream().filter(v -> v.getEmail().equals(email)).findFirst().orElse(null);
    }
}
