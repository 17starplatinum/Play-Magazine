package com.example.pmcore.controllers;

import com.example.pmcore.dto.auth.RoleChangeRequestDto;
import com.example.pmcore.dto.auth.StatusResponse;
import com.example.pmcore.dto.data.ResponseDto;
import com.example.pmcore.model.auth.Role;
import com.example.pmcore.model.auth.User;
import com.example.pmcore.services.auth.RoleManagementService;
import com.example.pmcore.services.auth.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminController {
    private final RoleManagementService roleManagementService;
    private final UserService userService;

    @GetMapping("/admin-requests/status")
    public ResponseEntity<StatusResponse> getAdminRequestsStatus() {
        return ResponseEntity.ok().body(userService.getAdminRequestStatus());
    }

    @GetMapping("/available-roles")
    public ResponseEntity<List<Role>> getAvailableRoles() {
        return ResponseEntity.ok().body(roleManagementService.getAvailableRoles());
    }

    @GetMapping("/requests/{requestStatus}")
    @PreAuthorize("hasRole('ADMIN') || hasRole('MODERATOR')")
    public ResponseEntity<List<RoleChangeRequestDto>> getUsersByRequestStatus(@PathVariable String requestStatus) {
        return ResponseEntity.status(HttpStatus.OK).body(userService.findByRequestStatus(requestStatus));
    }

    @PutMapping("/approve/{username}")
    @PreAuthorize("hasRole('ADMIN') || hasRole('MODERATOR')")
    public ResponseEntity<ResponseDto> approveRequest(@PathVariable String username) {
        roleManagementService.approveRequest(username);
        return ResponseEntity.ok(new ResponseDto("Request has been successfully approved"));
    }

    @PutMapping("/reject/{email}")
    @PreAuthorize("hasRole('ADMIN') || hasRole('MODERATOR')")
    public ResponseEntity<ResponseDto> rejectRequest(
            @PathVariable String email,
            @RequestBody RoleChangeRequestDto requestDto
    ) {
        roleManagementService.rejectRequest(email, requestDto.getRole(), requestDto.getReason());
        return ResponseEntity.ok(new ResponseDto("Заявка успешно отклонена с причиной: " + requestDto.getReason()));
    }

    @PutMapping("/change-role")
    public ResponseEntity<ResponseDto> changeUserRole(@Valid @RequestBody RoleChangeRequestDto requestDto) {
        roleManagementService.grantRole(requestDto.getEmail(), requestDto.getRole());
        User user = userService.getByUsername(requestDto.getEmail());
        return ResponseEntity.ok(
                new ResponseDto(
                String.format(
                        "Пользователь %s теперь является %s-ом",
                        user.getEmail(),
                        requestDto.getRole()
                ))
        );
    }
}
