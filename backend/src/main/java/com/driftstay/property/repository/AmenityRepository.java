package com.driftstay.property.repository;

import com.driftstay.property.entity.Amenity;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AmenityRepository extends JpaRepository<Amenity, Long> {

    @Cacheable(value = "amenities")
    List<Amenity> findAllByOrderByNameAsc();

    @Cacheable(value = "amenities", key = "#category")
    List<Amenity> findByCategory(String category);
}
