package com.example.backend.controllers;

import com.example.backend.services.auth.UserService;
import com.example.backend.services.util.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

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
        return ResponseEntity.ok().body("Hope!");
    }

    @ExceptionHandler
    public ResponseEntity<String> handler(Exception e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }
}
