package com.driftstay.property.repository;

import com.driftstay.common.enums.PropertyStatus;
import com.driftstay.property.entity.Property;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface PropertyRepository extends JpaRepository<Property, Long> {
    Optional<Property> findByPublicId(String publicId);
    Optional<Property> findBySlug(String slug);
    boolean existsBySlug(String slug);

    Page<Property> findByStatus(PropertyStatus status, Pageable pageable);
    Page<Property> findByStatusAndCity(PropertyStatus status, String city, Pageable pageable);

    @Query("SELECT p FROM Property p WHERE p.status = :status AND " +
           "(:city IS NULL OR LOWER(p.city) LIKE LOWER(CONCAT('%', :city, '%'))) AND " +
           "(:propertyType IS NULL OR p.propertyType = :propertyType) AND " +
           "(:minRating IS NULL OR p.averageRating >= :minRating)")
    Page<Property> searchProperties(@Param("status") PropertyStatus status,
                                    @Param("city") String city,
                                    @Param("propertyType") String propertyType,
                                    @Param("minRating") BigDecimal minRating,
                                    Pageable pageable);

    @Query("SELECT p FROM Property p WHERE p.status = 'ACTIVE' ORDER BY p.averageRating DESC NULLS LAST, p.bookingCount DESC NULLS LAST")
    List<Property> findFeatured(Pageable pageable);

    @Query("SELECT DISTINCT p.city FROM Property p WHERE p.status = 'ACTIVE' AND p.city IS NOT NULL")
    List<String> findAllCities();

    @Query("SELECT p FROM Property p WHERE p.status = 'ACTIVE' AND p.city = :city")
    List<Property> findPropertiesByCity(@Param("city") String city);
}
