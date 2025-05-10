package com.example.backend.controllers;

import com.example.backend.dto.data.ResponseDto;
import com.example.backend.dto.data.app.*;
import com.example.backend.dto.data.purchase.PurchaseRequest;
import com.example.backend.dto.data.review.ReviewRequestDto;
import com.example.backend.dto.data.review.ReviewResponseDto;
import com.example.backend.dto.util.AppCompatibilityResponse;
import com.example.backend.exceptions.accepted.AppDownloadException;
import com.example.backend.exceptions.paymentrequired.AppNotPurchasedException;
import com.example.backend.exceptions.prerequisites.AppUpToDateException;
import com.example.backend.services.data.AppService;
import com.example.backend.services.data.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/apps")
@RequiredArgsConstructor
public class AppController {
    private final AppService appService;
    private final ReviewService reviewService;

    @GetMapping
    public ResponseEntity<AppsInfoResponse> getApps(@RequestParam(value = "limit", defaultValue = "10") int limit) {
        return ResponseEntity.ok(new AppsInfoResponse(appService.getAllAvailableApps(limit)));
    }

    @GetMapping("/{appId}")
    public ResponseEntity<AppInfoResponseDto> getApp(@PathVariable UUID appId) {
        return ResponseEntity.ok(appService.getAppInfoById(appId));
    }

    @GetMapping("/{appId}/compatibility")
    public ResponseEntity<AppCompatibilityResponse> checkCompatibility(
            @PathVariable UUID appId,
            @RequestParam("os") String os
    ) {
        return ResponseEntity.ok(appService.checkCompatibility(appId, os));
    }

    @GetMapping("/{appId}/update-info")
    public ResponseEntity<AppDownloadResponse> checkForUpdates(@PathVariable UUID appId) {
        return ResponseEntity.ok(appService.prepareAppDownload(appId));
    }

    @GetMapping("/{appId}/reviews")
    public ResponseEntity<ReviewResponseDto> getReviews(@PathVariable UUID appId) {
        return ResponseEntity.ok(
                new ReviewResponseDto(
                        appService.getAppNameById(appId),
                        reviewService.getAverageRating(appId),
                        reviewService.getAppReviews(appId)
                )
        );
    }

    @GetMapping("/{appId}/reviews/average")
    public ResponseEntity<ResponseDto> getAverageRating(@PathVariable UUID appId) {
        return ResponseEntity.ok().body(new ResponseDto(reviewService.getAverageRating(appId).toString()));
    }

    @GetMapping("/{appId}/download")
    public ResponseEntity<byte[]> downloadApp(
            @PathVariable UUID appId,
            @Valid @RequestBody(required = false) PurchaseRequest purchaseRequest,
            @RequestParam(value = "force_update", required = false) Boolean forceUpdate
    ) {
        try {
            byte[] fileContent = appService.downloadAppFile(appId, purchaseRequest, forceUpdate);
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"app.apk\"")
                    .body(fileContent);
        } catch (AppNotPurchasedException e) {
            return ResponseEntity.status(HttpStatus.PAYMENT_REQUIRED).build();
        } catch (UsernameNotFoundException | AppDownloadException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (AppUpToDateException e) {
            return ResponseEntity.status(HttpStatus.PRECONDITION_FAILED).build();
        }
    }

    @PostMapping("/{appId}/reviews")
    public ResponseEntity<ResponseDto> createReview(
            @PathVariable UUID appId,
            @Valid @RequestBody ReviewRequestDto reviewRequestDto
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(new ResponseDto(
                reviewService.createReview(appService.getAppById(appId), reviewRequestDto).toString()
        ));
    }

    @PutMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<AppIdDto> createApp(@Valid @ModelAttribute AppCreateRequest appCreateDto
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(new AppIdDto(appService.createApp(appCreateDto)));
    }

    @PutMapping(path = "/{appId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> updateApp(@PathVariable UUID appId,
                                       @Valid @ModelAttribute AppUpdateDto appUpdateDto
    ) {
        appService.bumpApp(appId, appUpdateDto);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping("/{appId}")
    public ResponseEntity<Void> deleteApp(@PathVariable UUID appId) {
        appService.deleteApp(appId);
        return ResponseEntity.noContent().build();
    }
}
