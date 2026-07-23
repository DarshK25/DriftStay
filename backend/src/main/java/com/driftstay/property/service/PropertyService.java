package com.driftstay.property.service;

import com.driftstay.property.entity.Property;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;

public interface PropertyService {
    Property createProperty(Property property);
    Property updateProperty(String publicId, Property property);
    Property getPropertyByPublicId(String publicId);
    Property getPropertyBySlug(String slug);
    Page<Property> getActiveProperties(Pageable pageable);
    Page<Property> searchProperties(String city, String propertyType, BigDecimal minRating, Pageable pageable);
    List<Property> getFeaturedProperties(int limit);
    List<String> getAllCities();
    void deleteProperty(String publicId);
}
