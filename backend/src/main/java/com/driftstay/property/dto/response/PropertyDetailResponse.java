package com.driftstay.property.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Builder
public class PropertyDetailResponse {
    private Long id;
    private String publicId;
    private String name;
    private String slug;
    private String shortDescription;
    private String description;
    private String propertyType;
    private Integer starCategory;
    private String status;
    private String addressLine1;
    private String addressLine2;
    private String landmark;
    private String city;
    private String state;
    private String country;
    private String postalCode;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private BigDecimal averageRating;
    private Integer reviewCount;
    private Integer bookingCount;
    private List<String> imageUrls;
    private List<String> amenityNames;
    private PropertyPolicyResponse policy;
}
