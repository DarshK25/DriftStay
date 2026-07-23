package com.driftstay.auth.dto.request;

import com.driftstay.auth.validator.PasswordStrength;
import com.driftstay.common.validation.CreateValidation;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResetPasswordRequest {
    @NotBlank(groups = CreateValidation.class)
    private String token;

    @NotBlank(groups = CreateValidation.class) @PasswordStrength
    private String newPassword;
}
