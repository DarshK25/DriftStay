package com.driftstay.property.dto.request;

import com.driftstay.common.validation.UpdateValidation;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class PropertyUpdateRequest {
    @Size(max = 255, groups = UpdateValidation.class)
    private String name;
    @Size(max = 255, groups = UpdateValidation.class)
    private String slug;
    @Size(max = 50, groups = UpdateValidation.class)
    private String propertyType;
    @Size(max = 500, groups = UpdateValidation.class)
    private String shortDescription;
    private String description;
    private Integer starCategory;
    @Size(max = 255, groups = UpdateValidation.class)
    private String addressLine1;
    private String addressLine2;
    private String landmark;
    @Size(max = 100, groups = UpdateValidation.class)
    private String city;
    @Size(max = 100, groups = UpdateValidation.class)
    private String state;
    @Size(max = 100, groups = UpdateValidation.class)
    private String country;
    private String postalCode;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String status;
}
