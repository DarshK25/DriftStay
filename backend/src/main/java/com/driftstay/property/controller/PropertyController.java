package com.driftstay.property.controller;

import com.driftstay.common.dto.PagedResponse;
import com.driftstay.property.dto.request.PropertyCreateRequest;
import com.driftstay.property.dto.request.PropertyUpdateRequest;
import com.driftstay.property.dto.response.PropertyDetailResponse;
import com.driftstay.property.dto.response.PropertySummaryResponse;
import com.driftstay.property.entity.Property;
import com.driftstay.property.mapper.PropertyMapper;
import com.driftstay.property.service.PropertyService;
import io.swagger.v3.oas.annotations.Operation;
import java.math.BigDecimal;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/v1/properties")
@RequiredArgsConstructor
@Tag(name = "Properties", description = "Property management endpoints")
public class PropertyController {

    private final PropertyService propertyService;
    private final PropertyMapper propertyMapper;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a new property")
    public ResponseEntity<PropertyDetailResponse> createProperty(@Valid @RequestBody PropertyCreateRequest request) {
        var property = propertyService.createProperty(propertyMapper.toEntity(request));
        return ResponseEntity.status(HttpStatus.CREATED).body(propertyMapper.toDetail(property));
    }

    @PutMapping("/{publicId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update a property")
    public ResponseEntity<PropertyDetailResponse> updateProperty(@PathVariable String publicId,
                                                                  @Valid @RequestBody PropertyUpdateRequest request) {
        var existing = propertyService.getPropertyByPublicId(publicId);
        propertyMapper.updateEntity(request, existing);
        var updated = propertyService.updateProperty(publicId, existing);
        return ResponseEntity.ok(propertyMapper.toDetail(updated));
    }

    @GetMapping("/{publicId}")
    @Operation(summary = "Get property details by public ID")
    public ResponseEntity<PropertyDetailResponse> getProperty(@PathVariable String publicId) {
        var property = propertyService.getPropertyByPublicId(publicId);
        var response = propertyMapper.toDetail(property);
        response = enrichDetail(response, property);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/slug/{slug}")
    @Operation(summary = "Get property details by slug")
    public ResponseEntity<PropertyDetailResponse> getPropertyBySlug(@PathVariable String slug) {
        var property = propertyService.getPropertyBySlug(slug);
        var response = propertyMapper.toDetail(property);
        response = enrichDetail(response, property);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(summary = "List active properties with pagination")
    public ResponseEntity<PagedResponse<PropertySummaryResponse>> listProperties(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        var sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        Page<PropertySummaryResponse> result = propertyService.getActiveProperties(PageRequest.of(page, size, sort))
                .map(p -> enrichSummary(propertyMapper.toSummary(p), p));
        return ResponseEntity.ok(PagedResponse.of(result));
    }

    @GetMapping("/search")
    @Operation(summary = "Search properties with filters")
    public ResponseEntity<PagedResponse<PropertySummaryResponse>> searchProperties(
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String propertyType,
            @RequestParam(required = false) BigDecimal minRating,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<PropertySummaryResponse> result = propertyService.searchProperties(city, propertyType, minRating, PageRequest.of(page, size))
                .map(p -> enrichSummary(propertyMapper.toSummary(p), p));
        return ResponseEntity.ok(PagedResponse.of(result));
    }

    @GetMapping("/featured")
    @Operation(summary = "Get featured/top-rated properties")
    public ResponseEntity<List<PropertySummaryResponse>> getFeaturedProperties(
            @RequestParam(defaultValue = "8") int limit) {
        var properties = propertyService.getFeaturedProperties(limit);
        var response = properties.stream()
                .map(p -> enrichSummary(propertyMapper.toSummary(p), p))
                .toList();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/cities")
    @Operation(summary = "Get list of all cities with active properties")
    public ResponseEntity<List<String>> getCities() {
        return ResponseEntity.ok(propertyService.getAllCities());
    }

    @DeleteMapping("/{publicId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete a property")
    public ResponseEntity<Void> deleteProperty(@PathVariable String publicId) {
        propertyService.deleteProperty(publicId);
        return ResponseEntity.noContent().build();
    }

    private PropertySummaryResponse enrichSummary(PropertySummaryResponse summary, Property property) {
        var thumbnail = property.getImages().stream()
                .filter(img -> Boolean.TRUE.equals(img.getIsThumbnail()))
                .findFirst();
        var startingPrice = property.getRooms().stream()
                .map(r -> r.getBasePrice())
                .min(BigDecimal::compareTo);
        return PropertySummaryResponse.builder()
                .id(summary.getId())
                .publicId(summary.getPublicId())
                .name(summary.getName())
                .slug(summary.getSlug())
                .shortDescription(summary.getShortDescription())
                .propertyType(summary.getPropertyType())
                .starCategory(summary.getStarCategory())
                .status(summary.getStatus())
                .city(summary.getCity())
                .state(summary.getState())
                .country(summary.getCountry())
                .averageRating(summary.getAverageRating())
                .reviewCount(summary.getReviewCount())
                .startingPrice(startingPrice.orElse(null))
                .thumbnailUrl(thumbnail.map(img -> img.getImageUrl()).orElse(null))
                .build();
    }

    private PropertyDetailResponse enrichDetail(PropertyDetailResponse detail, Property property) {
        var imageUrls = property.getImages().stream()
                .map(img -> img.getImageUrl())
                .toList();
        var amenityNames = property.getPropertyAmenities().stream()
                .map(pa -> pa.getAmenity().getName())
                .toList();
        var policy = property.getPolicy() != null ? propertyMapper.toPolicy(property.getPolicy()) : null;
        return PropertyDetailResponse.builder()
                .id(detail.getId())
                .publicId(detail.getPublicId())
                .name(detail.getName())
                .slug(detail.getSlug())
                .shortDescription(detail.getShortDescription())
                .description(detail.getDescription())
                .propertyType(detail.getPropertyType())
                .starCategory(detail.getStarCategory())
                .status(detail.getStatus())
                .addressLine1(detail.getAddressLine1())
                .addressLine2(detail.getAddressLine2())
                .landmark(detail.getLandmark())
                .city(detail.getCity())
                .state(detail.getState())
                .country(detail.getCountry())
                .postalCode(detail.getPostalCode())
                .latitude(detail.getLatitude())
                .longitude(detail.getLongitude())
                .averageRating(detail.getAverageRating())
                .reviewCount(detail.getReviewCount())
                .bookingCount(detail.getBookingCount())
                .imageUrls(imageUrls)
                .amenityNames(amenityNames)
                .policy(policy)
                .build();
    }
}
