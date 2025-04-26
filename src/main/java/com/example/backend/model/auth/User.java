package com.example.backend.model.auth;

import com.example.backend.model.data.app.App;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "users")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class User implements UserDetails {
    @Id
    @UuidGenerator
    private UUID id;

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

    @ManyToMany
    @JsonIgnoreProperties({"downloadedApps"})
    @JoinTable(
            name = "user_app_downloads",
            joinColumns = @JoinColumn(name = "user_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "app_id", referencedColumnName = "id")
    )
    private Set<App> downloadedApps;

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
