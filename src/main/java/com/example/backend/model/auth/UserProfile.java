package com.example.backend.model.auth;

import com.example.backend.services.util.LocalDateAdapter;
import jakarta.persistence.*;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "user_profiles")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserProfile {
    @Id
    @UuidGenerator
    @Column(name = "id")
    private UUID id;

    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Column(length = 32, nullable = false)
    private String name;

    @Column(length = 32, nullable = false)
    private String surname;

    @XmlJavaTypeAdapter(LocalDateAdapter.class)
    private LocalDate birthday;
}
