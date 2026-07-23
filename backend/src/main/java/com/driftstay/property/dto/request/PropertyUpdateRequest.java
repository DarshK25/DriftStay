package com.driftstay.property.dto.request;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class PropertyUpdateRequest {
    private String name;
    private String slug;
    private String propertyType;
    private String shortDescription;
    private String description;
    private Integer starCategory;
    private String addressLine1;
    private String addressLine2;
    private String landmark;
    private String city;
    private String state;
    private String country;
    private String postalCode;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String status;
}
