package com.example.backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDate;
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
    @Column(name = "id")
    private UUID id;

    @Column(name = "name", length = 32, nullable = false)
    private String name;

    @Column(name = "surname", length = 32, nullable = false)
    private String surname;

    @Column(name = "password", length = 64, nullable = false)
    private String password;

    @Column(name = "email", length = 32, nullable = false)
    private String email;

    @Column(name = "role", nullable = false)
    @Enumerated(EnumType.STRING)
    private Role role;

    @Column(name = "birthday")
    private LocalDate birthday;

    @Column(name = "is_blocked", nullable = false)
    private boolean isBlocked;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "user_app_downloads",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "app_id")
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
