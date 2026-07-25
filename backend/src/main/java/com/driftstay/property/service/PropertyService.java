package com.driftstay.property.service;

import com.driftstay.common.enums.PropertyStatus;
import com.driftstay.gallery.service.ImageStorageService;
import com.driftstay.property.entity.Property;
import com.driftstay.property.entity.PropertyImage;
import com.driftstay.property.repository.PropertyRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PropertyService {

    private final PropertyRepository propertyRepository;
    private final ImageStorageService imageStorageService;
    private final EntityManager entityManager;

    @Transactional
    public Property createProperty(Property property, Long ownerId) {
        property.setPublicId(UUID.randomUUID().toString().replace("-", "").substring(0, 26));
        property.setStatus(PropertyStatus.ACTIVE);
        Property saved = propertyRepository.save(property);
        log.info("Property created: id={}, name={}", saved.getId(), saved.getName());
        return saved;
    }

    @Transactional
    @CacheEvict(value = {"properties", "propertyBySlug", "featuredProperties"}, allEntries = true)
    public Property updateProperty(Long id, Property updates) {
        Property property = getProperty(id);
        if (updates.getName() != null) property.setName(updates.getName());
        if (updates.getShortDescription() != null) property.setShortDescription(updates.getShortDescription());
        if (updates.getDescription() != null) property.setDescription(updates.getDescription());
        if (updates.getCity() != null) property.setCity(updates.getCity());
        if (updates.getState() != null) property.setState(updates.getState());
        if (updates.getCountry() != null) property.setCountry(updates.getCountry());
        if (updates.getPropertyType() != null) property.setPropertyType(updates.getPropertyType());
        if (updates.getStarCategory() != null) property.setStarCategory(updates.getStarCategory());
        if (updates.getStatus() != null) property.setStatus(updates.getStatus());
        return propertyRepository.save(property);
    }

    @Transactional
    @CacheEvict(value = {"properties", "featuredProperties"}, allEntries = true)
    public void deleteProperty(Long id) {
        Property property = getProperty(id);
        property.setStatus(PropertyStatus.DELETED);
        propertyRepository.save(property);
        log.info("Property deleted (soft): id={}", id);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "properties", key = "#id")
    public Property getProperty(Long id) {
        return propertyRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Property not found: " + id));
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "propertyBySlug", key = "#slug")
    public Property getPropertyBySlug(String slug) {
        return propertyRepository.findBySlug(slug)
                .orElseThrow(() -> new IllegalArgumentException("Property not found: " + slug));
    }

    @Transactional(readOnly = true)
    public List<Property> listProperties() {
        return propertyRepository.findByStatus(PropertyStatus.ACTIVE);
    }

    @Transactional
    public List<String> uploadImages(Long propertyId, List<MultipartFile> files) {
        Property property = getProperty(propertyId);
        return files.stream().map(file -> {
            String url = imageStorageService.uploadImage(file, "properties/" + propertyId);
            PropertyImage image = new PropertyImage();
            image.setProperty(property);
            image.setImageUrl(url);
            image.setDisplayOrder(property.getImages().size() + 1);
            entityManager.persist(image);
            return url;
        }).collect(Collectors.toList());
    }
}
