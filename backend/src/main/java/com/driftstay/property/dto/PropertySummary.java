package com.driftstay.property.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PropertySummary {

    private String publicId;
    private String slug;
    private String name;
    private String shortDescription;
    private String propertyType;
    private Integer starCategory;
    private String city;
    private String state;
    private String country;
    private BigDecimal averageRating;
    private Integer reviewCount;
    private String thumbnailUrl;
    private List<String> amenities;
    private BigDecimal startingPrice;
}
