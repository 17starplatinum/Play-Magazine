package com.example.backend.controllers;

import com.example.backend.dto.auth.RoleChangeRequestDto;
import com.example.backend.repositories.UserRepository;
import com.example.backend.services.auth.RoleManagementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.example.backend.model.auth.RequestStatus.PENDING;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminController {
    private final RoleManagementService roleManagementService;
    private final UserRepository userRepository;

    @GetMapping("/requests")
    @PreAuthorize("hasRole('MODERATOR') || hasRole('ADMIN')")
    public List<User> getPendingRequests() {
        return userRepository.findByRequestStatus(String.valueOf(PENDING));
    }

    @PostMapping("/approve")
    @PreAuthorize("hasRole('MODERATOR') || hasRole('ADMIN')")
    public ResponseEntity<String> approveRequest(@Valid @RequestBody RoleChangeRequestDto requestDto, UserDetails moderator) {
        roleManagementService.approveRequest(requestDto.getUserId(), moderator.getUsername());
        return ResponseEntity.ok("Заявка успешно одобрена.");
    }

    @PostMapping("/reject")
    @PreAuthorize("hasRole('MODERATOR') || hasRole('ADMIN')")
    public ResponseEntity<String> rejectRequest(@Valid @RequestBody RoleChangeRequestDto requestDto, UserDetails moderator) {
        roleManagementService.rejectRequest(requestDto.getUserId(), moderator.getUsername(), requestDto.getReason());
        return ResponseEntity.ok("Заявка успешно отклонена с причиной: " + requestDto.getReason());
    }

    @PostMapping("/change-role")
    @PreAuthorize("hasRole('MODERATOR') || hasRole('ADMIN')")
    public ResponseEntity<String> changeUserRole(@Valid @RequestBody RoleChangeRequestDto requestDto) {
        roleManagementService.grantRole(requestDto.getUserId(), requestDto.getNewRole());
        return ResponseEntity.ok(String.format("Пользователь %s теперь является %s-ом", requestDto.getUserId(), requestDto.getNewRole()));
    }
}
