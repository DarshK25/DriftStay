package com.driftstay.user.controller;

import com.driftstay.auth.service.AuthService;
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

@RestController
@RequestMapping("/v1/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "User profile endpoints")
public class UserController {

    private final UserService userService;
    private final AuthService authService;

    @GetMapping("/me")
    @Operation(summary = "Get current user profile")
    public ResponseEntity<ProfileResponse> getMyProfile(@AuthenticationPrincipal UserDetails userDetails) {
        var user = authService.getCurrentUser(userDetails);
        var response = ProfileResponse.builder()
                .publicId(user.getPublicId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .phone(user.getPhone())
                .profilePicture(user.getProfilePicture())
                .isVerified(user.getIsVerified())
                .status(user.getStatus().name())
                .roles(user.getRoles().stream().map(r -> r.getName()).collect(java.util.stream.Collectors.toSet()))
                .lastLogin(user.getLastLogin())
                .createdAt(user.getCreatedAt())
                .build();
        return ResponseEntity.ok(response);
    }

    @PutMapping("/me")
    @Operation(summary = "Update current user profile")
    public ResponseEntity<ProfileResponse> updateProfile(@AuthenticationPrincipal UserDetails userDetails,
                                                          @RequestBody UpdateProfileRequest request) {
        var user = authService.getCurrentUser(userDetails);
        var updated = userService.updateProfile(user.getPublicId(), request.getFirstName(), request.getLastName(), request.getPhone());
        var response = ProfileResponse.builder()
                .publicId(updated.getPublicId())
                .email(updated.getEmail())
                .firstName(updated.getFirstName())
                .lastName(updated.getLastName())
                .phone(updated.getPhone())
                .profilePicture(updated.getProfilePicture())
                .isVerified(updated.getIsVerified())
                .status(updated.getStatus().name())
                .roles(updated.getRoles().stream().map(r -> r.getName()).collect(java.util.stream.Collectors.toSet()))
                .lastLogin(updated.getLastLogin())
                .createdAt(updated.getCreatedAt())
                .build();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{publicId}")
    @Operation(summary = "Get user profile by public ID")
    public ResponseEntity<ProfileResponse> getUserProfile(@PathVariable String publicId) {
        var user = userService.getUserByPublicId(publicId);
        var response = ProfileResponse.builder()
                .publicId(user.getPublicId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .phone(user.getPhone())
                .profilePicture(user.getProfilePicture())
                .isVerified(user.getIsVerified())
                .status(user.getStatus().name())
                .roles(user.getRoles().stream().map(r -> r.getName()).collect(java.util.stream.Collectors.toSet()))
                .lastLogin(user.getLastLogin())
                .createdAt(user.getCreatedAt())
                .build();
        return ResponseEntity.ok(response);
    }
}
