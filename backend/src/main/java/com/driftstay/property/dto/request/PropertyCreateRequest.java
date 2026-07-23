package com.driftstay.property.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class PropertyCreateRequest {
    @NotBlank
    private String name;
    @NotBlank
    private String slug;
    @NotBlank
    private String propertyType;
    private String shortDescription;
    private String description;
    private Integer starCategory;
    @NotBlank
    private String addressLine1;
    private String addressLine2;
    private String landmark;
    @NotBlank
    private String city;
    @NotBlank
    private String state;
    @NotBlank
    private String country;
    private String postalCode;
    private BigDecimal latitude;
    private BigDecimal longitude;
    @NotNull
    private String status;
}
