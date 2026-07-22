package com.driftstay.auth.dto.request;

import com.driftstay.auth.validator.PasswordStrength;
import com.driftstay.auth.validator.PhoneNumber;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterRequest {

    @NotBlank @Email
    private String email;

    @NotBlank @PasswordStrength
    private String password;

    @NotBlank @Size(max = 100)
    private String firstName;

    @NotBlank @Size(max = 100)
    private String lastName;

    @PhoneNumber
    private String phone;
}
