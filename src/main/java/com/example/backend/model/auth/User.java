package com.example.backend.model.auth;

import com.example.backend.model.data.app.App;
import com.example.backend.model.data.subscriptions.UserSubscription;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.*;

@Entity
@Table(name = "users")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@XmlRootElement(name = "user")
@XmlAccessorType(XmlAccessType.FIELD)
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

    @JsonIgnore
    @NotNull
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    private UserBudget userBudget;

    @JsonBackReference
    @ToString.Exclude
    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JsonIgnore
    @Builder.Default
    @JoinTable(
            name = "user_app_downloads",
            joinColumns = @JoinColumn(name = "user_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "app_id", referencedColumnName = "id")
    )
    private Set<App> downloadedApps = new HashSet<>();

    @JsonIgnore
    @ToString.Exclude
    @Builder.Default
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<UserSubscription> userSubscriptions = new HashSet<>();

    public void removeApp(App app) {
        this.downloadedApps.remove(app);
        app.getUsersWhoDownloaded().remove(this);
    }

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
