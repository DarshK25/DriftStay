package com.driftstay.user.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Set;

@Getter
@Builder
public class ProfileResponse {
    private String publicId;
    private String email;
    private String firstName;
    private String lastName;
    private String phone;
    private String profilePicture;
    private Boolean isVerified;
    private String status;
    private Set<String> roles;
    private LocalDateTime lastLogin;
    private LocalDateTime createdAt;
}
