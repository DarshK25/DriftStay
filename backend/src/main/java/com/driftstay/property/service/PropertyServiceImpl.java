package com.driftstay.property.service;

import com.driftstay.common.enums.PropertyStatus;
import com.driftstay.common.exception.ResourceNotFoundException;
import com.driftstay.property.entity.Property;
import com.driftstay.property.repository.PropertyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class PropertyServiceImpl implements PropertyService {

    private final PropertyRepository propertyRepository;

    @Override
    @Transactional
    public Property createProperty(Property property) {
        property.setStatus(PropertyStatus.ACTIVE);
        Property saved = propertyRepository.save(property);
        log.info("Created property: {} (publicId: {})", saved.getName(), saved.getPublicId());
        return saved;
    }

    @Override
    @Transactional
    public Property updateProperty(String publicId, Property updated) {
        Property property = propertyRepository.findByPublicId(publicId)
                .orElseThrow(() -> new ResourceNotFoundException("Property not found: " + publicId));
        updated.setId(property.getId());
        updated.setPublicId(property.getPublicId());
        updated.setAverageRating(property.getAverageRating());
        updated.setReviewCount(property.getReviewCount());
        updated.setBookingCount(property.getBookingCount());
        Property saved = propertyRepository.save(updated);
        log.info("Updated property: {} (publicId: {})", saved.getName(), saved.getPublicId());
        return saved;
    }

    @Override
    public Property getPropertyByPublicId(String publicId) {
        return propertyRepository.findByPublicId(publicId)
                .orElseThrow(() -> new ResourceNotFoundException("Property not found: " + publicId));
    }

    @Override
    public Property getPropertyBySlug(String slug) {
        return propertyRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Property not found: " + slug));
    }

    @Override
    public Page<Property> getActiveProperties(Pageable pageable) {
        return propertyRepository.findByStatus(PropertyStatus.ACTIVE, pageable);
    }

    @Override
    public Page<Property> searchProperties(String city, String propertyType, BigDecimal minRating, Pageable pageable) {
        return propertyRepository.searchProperties(PropertyStatus.ACTIVE, city, propertyType, minRating, pageable);
    }

    @Override
    public List<Property> getFeaturedProperties(int limit) {
        return propertyRepository.findFeatured(Pageable.ofSize(limit));
    }

    @Override
    public List<String> getAllCities() {
        return propertyRepository.findAllCities();
    }

    @Override
    @Transactional
    public void deleteProperty(String publicId) {
        Property property = propertyRepository.findByPublicId(publicId)
                .orElseThrow(() -> new ResourceNotFoundException("Property not found: " + publicId));
        propertyRepository.delete(property);
        log.info("Deleted property: {} (publicId: {})", property.getName(), publicId);
    }
}
