package com.driftstay.property.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PropertySearchRequest {

    private String city;
    private String searchText;
    private String propertyType;
    private Integer starCategory;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private BigDecimal minRating;
    private Integer guestCount;
    private List<String> amenities;
    private String sortBy;
    private String sortDirection;
    private int page;
    private int size;
}
