package com.driftstay.property.entity;

import com.driftstay.common.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

@Entity
@Table(name = "property_amenity",
        indexes = {
                @Index(name = "idx_property_amenity_property", columnList = "property_id"),
                @Index(name = "idx_property_amenity_amenity", columnList = "amenity_id")
        },
        uniqueConstraints = @UniqueConstraint(name = "uk_property_amenity", columnNames = {"property_id", "amenity_id"})
)
@Getter
@Setter
public class PropertyAmenity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "property_id", nullable = false)
    private Property property;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "amenity_id", nullable = false)
    private Amenity amenity;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PropertyAmenity that)) return false;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
