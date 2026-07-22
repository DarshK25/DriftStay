package com.driftstay.auth.dto.request;

import com.driftstay.auth.validator.PasswordStrength;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResetPasswordRequest {
    @NotBlank
    private String token;

    @NotBlank @PasswordStrength
    private String newPassword;
}
