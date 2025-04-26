package com.example.backend.model.data;

import com.example.backend.model.auth.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDate;
import java.util.UUID;

@Builder
@Entity
@Table(name = "subscriptions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Subscription {
    @Id
    @UuidGenerator
    @Column(name = "id")
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "app_id", nullable = false)
    private App app;

    @Column(name = "price", nullable = false)
    private Float price;

    @Column(name = "start_dates", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_dates", nullable = false)
    private LocalDate endDate;

    @Builder.Default
    @Column(name = "auto_renewal")
    private Boolean autoRenewal = true;

    @ManyToOne
    @JoinColumn(name = "card_id")
    private Card card;
}
