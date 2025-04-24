package com.example.backend.controllers;

import com.example.backend.dto.auth.RoleChangeRequestDto;
import com.example.backend.model.auth.User;
import com.example.backend.services.auth.RoleManagementService;
import com.example.backend.services.auth.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminController {
    private final RoleManagementService roleManagementService;
    private final UserService userService;

    @GetMapping("/requests")
    @PreAuthorize("hasRole('MODERATOR') || hasRole('ADMIN')")
    public List<User> getUsersByRequestStatus(@RequestParam String requestStatus) {
        return userService.findByRequestStatus(requestStatus);
    }

    @PostMapping("/approve/{id}")
    @PreAuthorize("hasRole('MODERATOR') || hasRole('ADMIN')")
    public ResponseEntity<String> approveRequest(@PathVariable UUID id) {
        roleManagementService.approveRequest(id);
        return ResponseEntity.ok("Заявка успешно одобрена.");
    }

    @PostMapping("/reject/{id}")
    @PreAuthorize("hasRole('MODERATOR') || hasRole('ADMIN')")
    public ResponseEntity<String> rejectRequest(@PathVariable UUID id, @RequestBody RoleChangeRequestDto requestDto, @RequestParam String reason) {
        roleManagementService.rejectRequest(id, requestDto.getNewRole(), reason);
        return ResponseEntity.ok("Заявка успешно отклонена с причиной: " + reason);
    }

    @PostMapping("/change-role")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> changeUserRole(@Valid @RequestBody RoleChangeRequestDto requestDto) {
        roleManagementService.grantRole(requestDto.getUserId(), requestDto.getNewRole());
        return ResponseEntity.ok(String.format("Пользователь %s теперь является %s-ом", requestDto.getUserId(), requestDto.getNewRole()));
    }
}
