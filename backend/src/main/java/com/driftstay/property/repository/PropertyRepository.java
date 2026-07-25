package com.driftstay.property.repository;

import com.driftstay.common.enums.PropertyStatus;
import com.driftstay.property.entity.Property;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PropertyRepository extends JpaRepository<Property, Long>,
        JpaSpecificationExecutor<Property> {

    // ========== Cached Lookups ==========

    @Cacheable(value = "propertyBySlug", key = "#slug", unless = "#result == null")
    Optional<Property> findBySlug(String slug);

    @Cacheable(value = "propertyBySlug", key = "#publicId", unless = "#result == null")
    Optional<Property> findByPublicId(String publicId);

    // ========== City & Status ==========

    List<Property> findByCityIgnoreCaseAndStatus(String city, PropertyStatus status);

    List<Property> findByStatus(PropertyStatus status);

    Page<Property> findByStatus(PropertyStatus status, Pageable pageable);

    @Cacheable(value = "cities")
    @Query("SELECT DISTINCT p.city FROM Property p WHERE p.status = 'ACTIVE' ORDER BY p.city")
    List<String> findDistinctActiveCities();

    @Cacheable(value = "roomTypes")
    @Query("SELECT DISTINCT r.roomType FROM Room r WHERE r.status = com.driftstay.common.enums.RoomStatus.ACTIVE")
    List<String> findDistinctRoomTypes();

    // ========== Featured Properties ==========

    @Cacheable(value = "featuredProperties")
    @EntityGraph(attributePaths = {"images", "amenities.amenity"})
    @Query("""
            SELECT p FROM Property p
            WHERE p.status = 'ACTIVE'
              AND p.averageRating >= 4.0
              AND p.reviewCount >= 5
            ORDER BY p.averageRating DESC, p.reviewCount DESC
            """)
    List<Property> findFeaturedProperties(Pageable pageable);

    // ========== Cache Eviction ==========

    @CacheEvict(value = {"properties", "propertyBySlug", "featuredProperties"}, allEntries = true)
    Property save(Property property);

    @CacheEvict(value = {"properties", "propertyBySlug", "cities", "featuredProperties"}, allEntries = true)
    void deleteById(Long id);

    // ========== Search Query ==========

    @Query("""
            SELECT p FROM Property p
            WHERE p.status = 'ACTIVE'
              AND (:city IS NULL OR LOWER(p.city) = LOWER(:city))
              AND (:propertyType IS NULL OR p.propertyType = :propertyType)
              AND (:minRating IS NULL OR p.averageRating >= :minRating)
            """)
    List<Property> searchProperties(
            @Param("city") String city,
            @Param("propertyType") String propertyType,
            @Param("minRating") java.math.BigDecimal minRating
    );
}
