package com.driftstay.user.controller;

import com.driftstay.auth.service.AuthService;
import com.driftstay.common.dto.ApiResponse;
import com.driftstay.user.dto.request.UpdateProfileRequest;
import com.driftstay.user.dto.response.ProfileResponse;
import com.driftstay.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.stream.Collectors;

@RestController
@RequestMapping("/v1/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "User profile endpoints")
public class UserController {

    private final UserService userService;
    private final AuthService authService;

    @GetMapping("/me")
    @Operation(summary = "Get current user profile")
    public ResponseEntity<ApiResponse<ProfileResponse>> getMyProfile(@AuthenticationPrincipal UserDetails userDetails) {
        var user = authService.getCurrentUser(userDetails);
        var response = buildProfile(user);
        return ResponseEntity.ok(ApiResponse.success("Profile found", response));
    }

    @PutMapping("/me")
    @Operation(summary = "Update current user profile")
    public ResponseEntity<ApiResponse<ProfileResponse>> updateProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody UpdateProfileRequest request) {
        var user = authService.getCurrentUser(userDetails);
        var updated = userService.updateProfile(user.getPublicId(), request.getFirstName(), request.getLastName(), request.getPhone());
        return ResponseEntity.ok(ApiResponse.success("Profile updated", buildProfile(updated)));
    }

    @GetMapping("/{publicId}")
    @Operation(summary = "Get user profile by public ID")
    public ResponseEntity<ApiResponse<ProfileResponse>> getUserProfile(@PathVariable String publicId) {
        var user = userService.getUserByPublicId(publicId);
        return ResponseEntity.ok(ApiResponse.success("User found", buildProfile(user)));
    }

    private ProfileResponse buildProfile(com.driftstay.user.entity.User user) {
        return ProfileResponse.builder()
                .publicId(user.getPublicId()).email(user.getEmail())
                .firstName(user.getFirstName()).lastName(user.getLastName())
                .phone(user.getPhone()).profilePicture(user.getProfilePicture())
                .isVerified(user.getIsVerified()).status(user.getStatus().name())
                .roles(user.getRoles().stream().map(r -> r.getName()).collect(Collectors.toSet()))
                .lastLogin(user.getLastLogin()).createdAt(user.getCreatedAt())
                .build();
    }
}
