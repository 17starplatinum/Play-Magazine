package com.example.pmcore.controllers;

import com.example.backend.dto.data.ResponseDto;
import com.example.backend.services.auth.UserService;
import com.example.backend.services.util.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RequestMapping("/test")
@RestController
@RequiredArgsConstructor
public class TestController {
    private final UserService userService;
    private final EmailService emailService;

    @GetMapping("/{userId}")
    public ResponseEntity<?> me(@PathVariable UUID userId) {
        return ResponseEntity.ok().body(userService.getById(userId));
    }

    @GetMapping("/test")
    public ResponseEntity<?> test() {
        return ResponseEntity.ok().body(new ResponseDto("Луки, луки, луки! Захар crumble cookie"));
    }

    @ExceptionHandler
    public ResponseEntity<String> handler(Exception e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }
}
