package com.driftstay.property.service;

import com.driftstay.property.dto.PropertySearchRequest;
import com.driftstay.property.dto.PropertySummary;
import com.driftstay.property.entity.Property;
import com.driftstay.property.entity.PropertyImage;
import com.driftstay.property.repository.PropertyRepository;
import com.driftstay.property.specification.PropertySpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Search service for properties using the JPA Specification API.
 * Builds dynamic queries without 100 if-else statements.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PropertySearchService {

    private final PropertyRepository propertyRepository;

    /**
     * Search for properties based on the given criteria.
     * Results are cached for 10 minutes.
     *
     * @param request Search criteria
     * @return Paginated search results
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "propertySearch", key = "#request.toString()", unless = "#result.isEmpty()")
    public Page<PropertySummary> search(PropertySearchRequest request) {
        log.debug("Searching properties with criteria: {}", request);

        // Build dynamic specification
        PropertySpecification spec = PropertySpecification.builder()
                .activeOnly()
                .city(request.getCity())
                .searchText(request.getSearchText())
                .propertyType(request.getPropertyType())
                .starCategory(request.getStarCategory())
                .minRating(request.getMinRating())
                .build();

        // Apply sorting and pagination
        Pageable pageable = buildPageable(request);

        // Execute query
        Page<Property> properties = propertyRepository.findAll(spec, pageable);

        // Map to summaries
        return properties.map(this::toSummary);
    }

    /**
     * Get featured properties for the homepage.
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "featuredProperties", unless = "#result.isEmpty()")
    public List<PropertySummary> getFeaturedProperties() {
        Pageable pageable = PageRequest.of(0, 8);
        List<Property> properties = propertyRepository.findFeaturedProperties(pageable);

        return properties.stream()
                .map(this::toSummary)
                .collect(Collectors.toList());
    }

    /**
     * Get all active cities for the search filter dropdown.
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "cities")
    public List<String> getActiveCities() {
        return propertyRepository.findDistinctActiveCities();
    }

    private Pageable buildPageable(PropertySearchRequest request) {
        int page = Math.max(request.getPage(), 0);
        int size = Math.min(Math.max(request.getSize(), 1), 50); // 1-50 items per page

        String sortBy = request.getSortBy() != null ? request.getSortBy() : "averageRating";
        String sortDirection = request.getSortDirection() != null ? request.getSortDirection() : "DESC";

        Sort.Direction direction = sortDirection.equalsIgnoreCase("ASC")
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;

        Sort sort = Sort.by(direction, getSortField(sortBy));

        return PageRequest.of(page, size, sort);
    }

    private String getSortField(String sortBy) {
        return switch (sortBy.toLowerCase()) {
            case "price" -> "averageRating"; // Would need room join for actual price
            case "rating" -> "averageRating";
            case "name" -> "name";
            case "newest" -> "createdAt";
            case "popularity" -> "bookingCount";
            default -> "averageRating";
        };
    }

    private PropertySummary toSummary(Property property) {
        PropertyImage thumbnail = property.getImages().stream()
                .filter(img -> Boolean.TRUE.equals(img.getIsThumbnail()))
                .findFirst()
                .orElse(property.getImages().stream().findFirst().orElse(null));

        List<String> amenityNames = property.getPropertyAmenities().stream()
                .map(pa -> pa.getAmenity().getName())
                .collect(Collectors.toList());

        return PropertySummary.builder()
                .publicId(property.getPublicId())
                .slug(property.getSlug())
                .name(property.getName())
                .shortDescription(property.getShortDescription())
                .propertyType(property.getPropertyType())
                .starCategory(property.getStarCategory())
                .city(property.getCity())
                .state(property.getState())
                .country(property.getCountry())
                .averageRating(property.getAverageRating())
                .reviewCount(property.getReviewCount())
                .thumbnailUrl(thumbnail != null ? thumbnail.getImageUrl() : null)
                .amenities(amenityNames)
                .startingPrice(null) // Would need to query the cheapest room
                .build();
    }
}
