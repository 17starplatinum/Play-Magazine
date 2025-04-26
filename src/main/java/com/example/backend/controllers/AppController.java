package com.example.backend.controllers;

import com.example.backend.dto.data.ReviewDto;
import com.example.backend.dto.data.app.AppCreateRequest;
import com.example.backend.dto.data.app.AppDownloadResponse;
import com.example.backend.dto.data.app.AppUpdateDto;
import com.example.backend.dto.util.AppCompatibilityResponse;
import com.example.backend.exceptions.accepted.AppDownloadException;
import com.example.backend.exceptions.paymentrequired.AppNotPurchasedException;
import com.example.backend.exceptions.prerequisites.AppUpToDateException;
import com.example.backend.model.data.Review;
import com.example.backend.model.data.app.App;
import com.example.backend.services.data.AppService;
import com.example.backend.services.data.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;
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

    @PostMapping("/{appId}/reviews")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Review> createReview(
            @PathVariable UUID appId,
            @Valid @RequestBody ReviewDto reviewDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(reviewService.createReview(appId, reviewDto));
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('DEVELOPER') || hasRole('MODERATOR') || hasRole('ADMIN')")
    public ResponseEntity<App> createApp(@Valid @ModelAttribute AppCreateRequest appCreateDto,
                                         @RequestParam("file") MultipartFile file) {
        return ResponseEntity.status(HttpStatus.CREATED).body(appService.createApp(appCreateDto, file));
    }

    @PutMapping(path = "/{appId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('DEVELOPER') || hasRole('MODERATOR') || hasRole('ADMIN')")
    public ResponseEntity<App> updateApp(@PathVariable UUID appId, @Valid @ModelAttribute AppUpdateDto appUpdateDto,
                                         @RequestParam("file") MultipartFile file) {
        return ResponseEntity.status(HttpStatus.CREATED).body(appService.bumpApp(appId, appUpdateDto, file));
    }

    @GetMapping("/{appId}/update-info")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<AppDownloadResponse> checkForUpdates(@PathVariable UUID appId) {
        return ResponseEntity.ok(appService.prepareAppDownload(appId));
    }

    @GetMapping("/{appId}/download")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<byte[]> downloadApp(@PathVariable UUID appId, boolean forceUpdate) {
        try {
            byte[] fileContent = appService.downloadAppFile(appId, forceUpdate);
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"app.apk\"")
                    .body(fileContent);
        } catch (AppNotPurchasedException e) {
            return ResponseEntity.status(HttpStatus.PAYMENT_REQUIRED).body(null);
        } catch (UsernameNotFoundException | AppDownloadException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        } catch (AppUpToDateException e) {
            return ResponseEntity.status(HttpStatus.PRECONDITION_FAILED).body(null);
        }
    }

    @DeleteMapping("/{appId}")
    @PreAuthorize("hasRole('DEVELOPER') || hasRole('MODERATOR') || hasRole('ADMIN')")
    public ResponseEntity<Void> deleteApp(@PathVariable UUID appId) {
        appService.deleteApp(appId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{appId}/reviews")
    public ResponseEntity<List<Review>> getReviews(@PathVariable UUID appId) {
        return ResponseEntity.ok(reviewService.getAppReviews(appId));
    }

    @GetMapping("/{appId}/reviews/average")
    public ResponseEntity<Double> getAverageRating(@PathVariable UUID appId) {
        return ResponseEntity.ok(reviewService.getAverageRating(appId));
    }
}
