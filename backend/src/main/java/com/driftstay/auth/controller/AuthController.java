package com.driftstay.auth.controller;

import com.driftstay.auth.dto.request.ForgotPasswordRequest;
import com.driftstay.auth.dto.request.LoginRequest;
import com.driftstay.auth.dto.request.RefreshTokenRequest;
import com.driftstay.auth.dto.request.RegisterRequest;
import com.driftstay.auth.dto.request.ResetPasswordRequest;
import com.driftstay.auth.dto.response.AuthResponse;
import com.driftstay.auth.entity.UserPrincipal;
import com.driftstay.auth.service.AuthService;
import com.driftstay.auth.service.EmailVerificationService;
import com.driftstay.auth.service.PasswordResetService;
import com.driftstay.common.dto.ApiResponse;
import com.driftstay.common.validation.CreateValidation;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Auth endpoints (register, login, refresh, logout, password reset, email verification)")
public class AuthController {

    private final AuthService authService;
    private final PasswordResetService passwordResetService;
    private final EmailVerificationService emailVerificationService;

    @PostMapping("/register")
    @Operation(summary = "Register a new user")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Validated(CreateValidation.class) @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(authService.register(request)));
    }

    @PostMapping("/login")
    @Operation(summary = "Login with email and password")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Validated(CreateValidation.class) @RequestBody LoginRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Login successful", authService.login(request)));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh access token")
    public ResponseEntity<ApiResponse<AuthResponse>> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Token refreshed", authService.refresh(request)));
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout and revoke refresh token")
    public ResponseEntity<ApiResponse<Void>> logout(@Valid @RequestBody RefreshTokenRequest request) {
        authService.logout(request);
        return ResponseEntity.ok(ApiResponse.success("Logged out successfully", null));
    }

    @PostMapping("/logout-all")
    @Operation(summary = "Logout from all devices")
    public ResponseEntity<ApiResponse<Void>> logoutAll(@AuthenticationPrincipal UserPrincipal principal) {
        authService.logoutAll(principal.getEmail());
        return ResponseEntity.ok(ApiResponse.success("Logged out from all devices", null));
    }

    @GetMapping("/sessions")
    @Operation(summary = "Get active session count")
    public ResponseEntity<ApiResponse<Long>> getActiveSessions(@AuthenticationPrincipal UserPrincipal principal) {
        long count = authService.getActiveSessions(principal.getEmail());
        return ResponseEntity.ok(ApiResponse.success(count + " active sessions", count));
    }

    @PostMapping("/forgot-password")
    @Operation(summary = "Request password reset email")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(@Validated(CreateValidation.class) @RequestBody ForgotPasswordRequest request) {
        passwordResetService.requestReset(request.getEmail());
        return ResponseEntity.ok(ApiResponse.success("If the email exists, a reset link has been sent", null));
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Reset password with token")
    public ResponseEntity<ApiResponse<Void>> resetPassword(@Validated(CreateValidation.class) @RequestBody ResetPasswordRequest request) {
        passwordResetService.resetPassword(request.getToken(), request.getNewPassword());
        return ResponseEntity.ok(ApiResponse.success("Password reset successfully", null));
    }

    @PostMapping("/verify-email")
    @Operation(summary = "Verify email with token")
    public ResponseEntity<ApiResponse<Void>> verifyEmail(@RequestParam String token) {
        emailVerificationService.verifyEmail(token);
        return ResponseEntity.ok(ApiResponse.success("Email verified successfully", null));
    }

    @PostMapping("/admin/logout-user/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Admin force logout a user")
    public ResponseEntity<ApiResponse<Void>> adminLogoutUser(@PathVariable Long userId) {
        authService.logoutAll(String.valueOf(userId));
        return ResponseEntity.ok(ApiResponse.success("User logged out", null));
    }
}
