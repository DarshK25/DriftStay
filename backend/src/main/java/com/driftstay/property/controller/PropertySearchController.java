package com.driftstay.property.controller;

import com.driftstay.property.dto.PropertySearchRequest;
import com.driftstay.property.dto.PropertySummary;
import com.driftstay.property.service.PropertySearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/properties")
@RequiredArgsConstructor
public class PropertySearchController {

    private final PropertySearchService propertySearchService;

    /**
     * Search properties with filters.
     * GET /api/properties/search?city=...&minPrice=...&...
     */
    @GetMapping("/search")
    public ResponseEntity<Page<PropertySummary>> search(
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String searchText,
            @RequestParam(required = false) String propertyType,
            @RequestParam(required = false) Integer starCategory,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) BigDecimal minRating,
            @RequestParam(required = false) Integer guestCount,
            @RequestParam(required = false) List<String> amenities,
            @RequestParam(defaultValue = "rating") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        PropertySearchRequest request = PropertySearchRequest.builder()
                .city(city)
                .searchText(searchText)
                .propertyType(propertyType)
                .starCategory(starCategory)
                .minPrice(minPrice)
                .maxPrice(maxPrice)
                .minRating(minRating)
                .guestCount(guestCount)
                .amenities(amenities)
                .sortBy(sortBy)
                .sortDirection(sortDirection)
                .page(page)
                .size(size)
                .build();

        Page<PropertySummary> results = propertySearchService.search(request);
        return ResponseEntity.ok(results);
    }

    /**
     * Get featured properties for homepage.
     * GET /api/properties/featured
     */
    @GetMapping("/featured")
    public ResponseEntity<List<PropertySummary>> getFeatured() {
        return ResponseEntity.ok(propertySearchService.getFeaturedProperties());
    }

    /**
     * Get available cities for search filters.
     * GET /api/properties/cities
     */
    @GetMapping("/cities")
    public ResponseEntity<List<String>> getCities() {
        return ResponseEntity.ok(propertySearchService.getActiveCities());
    }
}
