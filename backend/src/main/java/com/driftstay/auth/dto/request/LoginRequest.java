package com.driftstay.auth.dto.request;

import com.driftstay.common.validation.CreateValidation;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginRequest {

    @NotBlank(groups = CreateValidation.class) @Email(groups = CreateValidation.class)
    private String email;

    @NotBlank(groups = CreateValidation.class)
    private String password;
}
