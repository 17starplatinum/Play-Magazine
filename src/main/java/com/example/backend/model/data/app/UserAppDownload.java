package com.example.backend.model.data.app;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "user_app_downloads")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserAppDownload {
    @EmbeddedId
    @AttributeOverrides({
            @AttributeOverride(name = "userId", column = @Column(name = "user_id")),
            @AttributeOverride(name = "appId", column = @Column(name = "app_id"))
    })
    private UserAppDownloadId id;

    @MapsId("appId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "app_id", nullable = false)
    private App app;

    @JsonIgnore
    public UUID getUserId() {
        return id != null ? id.getUserId() : null;
    }

    @JsonIgnore
    public UUID getAppId() {
        return id != null ? id.getAppId() : null;
    }
}
