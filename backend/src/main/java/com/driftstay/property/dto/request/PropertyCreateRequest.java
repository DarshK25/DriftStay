package com.driftstay.property.dto.request;

import com.driftstay.common.validation.CreateValidation;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class PropertyCreateRequest {
    @NotBlank(groups = CreateValidation.class)
    private String name;
    @NotBlank(groups = CreateValidation.class)
    private String slug;
    @NotBlank(groups = CreateValidation.class)
    private String propertyType;
    private String shortDescription;
    private String description;
    private Integer starCategory;
    @NotBlank(groups = CreateValidation.class)
    private String addressLine1;
    private String addressLine2;
    private String landmark;
    @NotBlank(groups = CreateValidation.class)
    private String city;
    @NotBlank(groups = CreateValidation.class)
    private String state;
    @NotBlank(groups = CreateValidation.class)
    private String country;
    private String postalCode;
    private BigDecimal latitude;
    private BigDecimal longitude;
    @NotNull(groups = CreateValidation.class)
    private String status;
}
