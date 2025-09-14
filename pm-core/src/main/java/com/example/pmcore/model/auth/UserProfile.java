package com.example.pmcore.model.auth;

import com.example.pmcore.services.util.LocalDateAdapter;
import jakarta.persistence.Column;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@XmlAccessorType(XmlAccessType.FIELD)
public class UserProfile {

    @Column(name = "id", nullable = false)
    @XmlElement(name = "id")
    private UUID id;

    @Column(length = 32, nullable = false)
    private String name;

    @Column(length = 32, nullable = false)
    private String surname;

    @XmlJavaTypeAdapter(LocalDateAdapter.class)
    private LocalDate birthday;
}
