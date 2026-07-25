package com.driftstay.auth.controller;

import com.driftstay.auth.dto.request.LoginRequest;
import com.driftstay.auth.dto.request.RefreshTokenRequest;
import com.driftstay.auth.dto.request.RegisterRequest;
import com.driftstay.auth.dto.response.AuthResponse;
import com.driftstay.auth.dto.response.UserProfile;
import com.driftstay.auth.service.AuthService;
import com.driftstay.security.filter.JwtUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * Register a new user account.
     * POST /api/auth/register
     */
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        log.debug("Register request for email: {}", request.getEmail());
        AuthResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Authenticate user and return tokens.
     * POST /api/auth/login
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        log.debug("Login request for email: {}", request.getEmail());
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Refresh an access token using a valid refresh token.
     * POST /api/auth/refresh
     */
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refreshToken(
            @Valid @RequestBody RefreshTokenRequest request) {
        log.debug("Refresh token request");
        AuthResponse response = authService.refreshToken(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Logout the current user by revoking all refresh tokens.
     * POST /api/auth/logout
     */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@AuthenticationPrincipal JwtUser currentUser) {
        if (currentUser != null) {
            authService.logout(currentUser.getUserId());
            log.debug("User logged out: {}", currentUser.getUserId());
        }
        return ResponseEntity.noContent().build();
    }

    /**
     * Get the current authenticated user's profile.
     * GET /api/auth/me
     */
    @GetMapping("/me")
    public ResponseEntity<UserProfile> getCurrentUser(
            @AuthenticationPrincipal JwtUser currentUser) {
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        UserProfile profile = authService.getCurrentUser(currentUser.getUserId());
        return ResponseEntity.ok(profile);
    }
}
