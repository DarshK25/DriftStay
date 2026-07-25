package com.driftstay.auth.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfile {

    private String publicId;
    private String email;
    private String firstName;
    private String lastName;
    private String phone;
    private String profilePicture;
    private boolean isVerified;
    private List<String> roles;
}
