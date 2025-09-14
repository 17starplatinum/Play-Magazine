package com.example.pmcore.model.data.app;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;
import java.util.UUID;

@Embeddable
@Builder
@EqualsAndHashCode
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserAppDownloadId implements Serializable {
    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "app_id")
    private UUID appId;
}
