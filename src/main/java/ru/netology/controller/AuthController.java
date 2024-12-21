package ru.netology.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import ru.netology.dto.LoginRequest;
import ru.netology.service.AuthService;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody LoginRequest loginRequest) {
        String token = authService.authenticate(loginRequest.getLogin(), loginRequest.getPassword());
        Map<String, String> response = new HashMap<>();
        response.put("auth-token", token);
        response.put("email", loginRequest.getLogin());
        log.info("Login successful");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(@RequestHeader("auth-token") String token) {
        authService.logout(token);
        log.info("Logout successful");
        return ResponseEntity.ok("Logged out successfully");
    }
}

