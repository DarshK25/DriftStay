package com.driftstay.property.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class PropertySummaryResponse {
    private Long id;
    private String publicId;
    private String name;
    private String slug;
    private String shortDescription;
    private String propertyType;
    private Integer starCategory;
    private String status;
    private String city;
    private String state;
    private String country;
    private BigDecimal averageRating;
    private Integer reviewCount;
    private BigDecimal startingPrice;
    private String thumbnailUrl;
}
