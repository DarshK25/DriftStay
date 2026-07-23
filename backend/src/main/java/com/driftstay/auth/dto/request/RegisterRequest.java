package com.driftstay.auth.dto.request;

import com.driftstay.auth.validator.PasswordStrength;
import com.driftstay.auth.validator.PhoneNumber;
import com.driftstay.common.validation.CreateValidation;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterRequest {

    @NotBlank(groups = CreateValidation.class) @Email(groups = CreateValidation.class)
    private String email;

    @NotBlank(groups = CreateValidation.class) @PasswordStrength
    private String password;

    @NotBlank(groups = CreateValidation.class) @Size(max = 100, groups = CreateValidation.class)
    private String firstName;

    @NotBlank(groups = CreateValidation.class) @Size(max = 100, groups = CreateValidation.class)
    private String lastName;

    @PhoneNumber
    private String phone;
}
