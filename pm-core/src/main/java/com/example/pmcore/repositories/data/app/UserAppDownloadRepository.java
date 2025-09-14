package com.example.pmcore.repositories.data.app;

import com.example.backend.model.data.app.UserAppDownload;
import com.example.backend.model.data.app.UserAppDownloadId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Set;
import java.util.UUID;

@Repository
public interface UserAppDownloadRepository extends JpaRepository<UserAppDownload, UserAppDownloadId> {
    Set<UserAppDownload> findAppIdsByIdUserId(UUID userId);
    void deleteByIdUserIdAndIdAppId(UUID userId, UUID appId);
    boolean existsByIdUserIdAndIdAppId(UUID id, UUID appId);
}
