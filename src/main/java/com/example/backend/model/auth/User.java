package com.example.backend.model.auth;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Version;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.xml.bind.annotation.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@XmlRootElement(name = "user")
@XmlAccessorType(XmlAccessType.FIELD)
public class User implements UserDetails {
    @Column(name = "id", nullable = false)
    @XmlElement(name = "id")
    private UUID id;

    @Version
    private int version;

    @Column(length = 64, nullable = false)
    private String password;

    @NotNull
    @Email(message = "E-mail адрес должен быть в формате 'user@example.com'")
    private String email;

    @NotNull
    @Builder.Default
    @Enumerated(EnumType.STRING)
    private Role role = Role.USER;

    @Column(name = "enable_two_fa", nullable = false)
    private boolean enableTwoFA;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(name = "request_status", nullable = false, length = 20)
    private RequestStatus requestStatus = RequestStatus.NOT_REQUESTED;

    @JsonIgnore
    @NotNull
    @XmlElement
    private UUID userBudgetId;

    @JsonIgnore
    @NotNull
    @XmlElement
    private UUID userProfileId;

    @JsonBackReference
    @JsonIgnore
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @Builder.Default
    @XmlElementWrapper(name = "subscriptionIds")
    @XmlElement(name = "subscriptionId")
    private Set<UUID> userSubscriptions = new HashSet<>();

    @JsonBackReference
    @JsonIgnore
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @Builder.Default
    @XmlElementWrapper(name = "appIds")
    @XmlElement(name = "appId")
    private Set<UUID> userAppDownloads = new HashSet<>();

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(role.name()));
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
