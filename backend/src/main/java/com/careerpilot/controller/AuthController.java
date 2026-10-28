package com.careerpilot.controller;

import com.careerpilot.dto.ApiResponse;
import com.careerpilot.dto.AuthResponse;
import com.careerpilot.dto.LoginRequest;
import com.careerpilot.dto.RegisterRequest;
import com.careerpilot.entity.User;
import com.careerpilot.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Account created", authService.register(request)));
    }

    @PostMapping("/login")
    public ApiResponse<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.ok("Logged in", authService.login(request));
    }

    @GetMapping("/me")
    public ApiResponse<User> me(Authentication authentication) {
        return ApiResponse.ok(authService.getByEmail(authentication.getName()));
    }
}
