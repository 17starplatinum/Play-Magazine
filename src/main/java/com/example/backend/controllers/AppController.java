package com.example.backend.controllers;

import com.example.backend.dto.data.AppDto;
import com.example.backend.dto.data.ReviewDto;
import com.example.backend.dto.util.AppCompatibilityResponse;
import com.example.backend.model.data.App;
import com.example.backend.model.data.Review;
import com.example.backend.services.data.AppService;
import com.example.backend.services.data.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/apps")
@RequiredArgsConstructor
public class AppController {
    private final AppService appService;
    private final ReviewService reviewService;

    @GetMapping
    public ResponseEntity<List<App>> getApps() {
        return ResponseEntity.ok(appService.getAllAvailableApps());
    }


    @GetMapping("/{appId}")
    public ResponseEntity<App> getApp(@PathVariable UUID appId) {
        return ResponseEntity.ok(appService.getAppById(appId));
    }

    @GetMapping("/{appId}/compatibility")
    public ResponseEntity<AppCompatibilityResponse> checkCompatibility(@PathVariable UUID appId) {
        return ResponseEntity.ok(appService.checkCompatibility(appId));
    }

    @GetMapping("/{appId}/reviews")
    public ResponseEntity<List<Review>> getAppReviews(@PathVariable UUID appId) {
        return ResponseEntity.ok(reviewService.getAppReviews(appId));
    }

    @PostMapping("/{appId}/reviews")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Review> createReview(
            @PathVariable UUID appId,
            @Valid @RequestBody ReviewDto reviewDto,
            @AuthenticationPrincipal UserDetails currentUser) {
        return ResponseEntity.ok(reviewService.createReview(appId, reviewDto, currentUser));
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('DEVELOPER')")
    public ResponseEntity<App> createApp(@Valid @ModelAttribute AppDto appCreateDto,
                                         @AuthenticationPrincipal UserDetails currentUser,
                                         @RequestParam("file") MultipartFile file) {
        return ResponseEntity.status(HttpStatus.CREATED).body(appService.createApp(appCreateDto, currentUser, file));
    }

    @GetMapping("/{appId}/download")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<byte[]> downloadApp(@PathVariable UUID appId, @AuthenticationPrincipal UserDetails currentUser) {
        byte[] fileContent = appService.downloadAppFile(appId, currentUser);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"app.apk\"")
                .body(fileContent);
    }

    @DeleteMapping("/{appId}")
    @PreAuthorize("hasRole('DEVELOPER')")
    public ResponseEntity<Void> deleteApp(@PathVariable UUID appId, @AuthenticationPrincipal UserDetails currentUser) {
        appService.deleteApp(appId, currentUser);
        return ResponseEntity.noContent().build();
    }
}
