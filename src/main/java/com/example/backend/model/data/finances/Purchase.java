package com.example.backend.model.data.finances;

import com.example.backend.model.auth.User;
import com.example.backend.model.data.app.App;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

@Entity
@Table(name = "purchases")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Purchase {

    @Id
    @UuidGenerator
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    private App app;

    @ManyToOne
    @JoinColumn(name = "transaction_id")
    private MonetaryTransaction transaction;
}
