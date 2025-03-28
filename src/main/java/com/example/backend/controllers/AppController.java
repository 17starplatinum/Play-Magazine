package com.example.backend.controllers;

import com.example.backend.dto.data.AppDto;
import com.example.backend.model.data.App;
import com.example.backend.services.data.AppService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;

import javax.print.attribute.standard.Media;
import java.nio.file.attribute.UserPrincipal;
import java.util.List;
import java.util.UUID;

@RestController("/apps")
public class AppController {
    private final AppService appService;
    @GetMapping
    public ResponseEntity<List<App>> getApps() {
        return ResponseEntity.ok(appService.getAllAvailableApps());
    }


    @GetMapping("/{appId}")
    public ResponseEntity<App> getApp(@PathVariable UUID appId) {
        return ResponseEntity.ok(appService.getAppById(appId));
    }


    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole(DEVELOPER)")
    public ResponseEntity<App> createApp(@Valid @ModelAttribute AppDto appCreateDto,
                                         UserPrincipal currentUser,
                                         @RequestParam("file") MultipartFile file) {
        return ResponseEntity.status(HttpStatus.CREATED).body(appService.createApp(appCreateDto, currentUser, file));
    }

    @GetMapping("/{appId}/download")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<byte[]> downloadApp(@PathVariable UUID appId, UserPrincipal currentUser) {
        byte[] fileContent = appService.downloadAppFile(appId, currentUser);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"app.apk\"")
                .body(fileContent);
    }

    @DeleteMapping("/{appId}")
    @PreAuthorize("hasRole('DEVELOPER')")
    public ResponseEntity<Void> deleteApp(@PathVariable UUID appId, UserPrincipal currentUser) {
        appService.deleteApp(appId, currentUser);
        return ResponseEntity.noContent().build();
    }
}
