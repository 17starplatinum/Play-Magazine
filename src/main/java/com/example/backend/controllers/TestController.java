package com.example.backend.controllers;

import com.example.backend.services.EmailService;
import com.example.backend.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

import java.net.URI;

@RequestMapping("/test")
@RestController
@RequiredArgsConstructor
public class TestController {
    private final UserService userService;
    private final EmailService emailService;

    @GetMapping("/test")
    public ResponseEntity<?> test() {
        return ResponseEntity.ok().body("Hope!");
    }

    @GetMapping("/mail")
    public ResponseEntity<?> mail() {
        emailService.sendVerificationEmail("kolomiec_ns_0919@1511.ru", "1234");
        return ResponseEntity.ok().build();
    }

    @GetMapping("/redirectWithRedirectView")
    public RedirectView redirectWithUsingRedirectView(
            RedirectAttributes attributes) {
        attributes.addFlashAttribute("flashAttribute", "redirectWithRedirectView");
        attributes.addAttribute("attribute", "redirectWithRedirectView");
        return new RedirectView("/test/test");
    }

    @ExceptionHandler
    public ResponseEntity<String> handler(Exception e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }
}
