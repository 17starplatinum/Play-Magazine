package com.example.backend.controllers;

import com.example.backend.dto.auth.RoleChangeRequestDto;
import com.example.backend.dto.data.ResponseDto;
import com.example.backend.model.auth.User;
import com.example.backend.services.auth.RoleManagementService;
import com.example.backend.services.auth.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
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

    @GetMapping("/admin-requests/status")
    public ResponseEntity<ResponseDto> getAdminRequestsStatus() {
        return ResponseEntity.ok().body(new ResponseDto(userService.getAdminRequestStatus()));
    }

    @GetMapping("/requests/{requestStatus}")
    @PreAuthorize("hasRole('ADMIN') || hasRole('MODERATOR')")
    public ResponseEntity<List<RoleChangeRequestDto>> getUsersByRequestStatus(@PathVariable String requestStatus) {
        return ResponseEntity.status(HttpStatus.OK).body(userService.findByRequestStatus(requestStatus));
    }
  
    @PutMapping("/approve/{id}")
    @PreAuthorize("hasRole('ADMIN') || hasRole('MODERATOR')")
    public ResponseEntity<ResponseDto> approveRequest(@PathVariable UUID id) {
        roleManagementService.approveRequest(id);
        return ResponseEntity.ok(new ResponseDto("Request has been successfully approved"));
    }

    @PutMapping("/reject/{id}")
    @PreAuthorize("hasRole('ADMIN') || hasRole('MODERATOR')")
    public ResponseEntity<ResponseDto> rejectRequest(
            @PathVariable UUID id,
            @RequestBody RoleChangeRequestDto requestDto
    ) {
        roleManagementService.rejectRequest(id, requestDto.getRole(), requestDto.getReason());
        return ResponseEntity.ok(new ResponseDto("Заявка успешно отклонена с причиной: " + requestDto.getReason()));
    }

    @PutMapping("/change-role")
    public ResponseEntity<ResponseDto> changeUserRole(@Valid @RequestBody RoleChangeRequestDto requestDto) {
        roleManagementService.grantRole(requestDto.getUserId(), requestDto.getRole());
        User user = userService.getById(requestDto.getUserId());
        return ResponseEntity.ok(
                new ResponseDto(
                String.format(
                        "User %s is now a %s",
                        user.getEmail(),
                        requestDto.getRole()
                ))
        );
    }
}
