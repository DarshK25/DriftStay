package com.driftstay.user.dto.request;

import com.driftstay.common.validation.UpdateValidation;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateProfileRequest {
    @Size(max = 100, groups = UpdateValidation.class)
    private String firstName;
    @Size(max = 100, groups = UpdateValidation.class)
    private String lastName;
    @Size(max = 20, groups = UpdateValidation.class)
    private String phone;
}
