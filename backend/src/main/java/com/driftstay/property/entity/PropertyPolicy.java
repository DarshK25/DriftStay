package com.driftstay.property.entity;

import com.driftstay.common.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalTime;

@Entity
@Table(name = "property_policy",
        uniqueConstraints = @UniqueConstraint(name = "uk_property_policy_property", columnNames = "property_id")
)
@Getter
@Setter
public class PropertyPolicy extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "policy_id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "property_id", nullable = false, unique = true)
    private Property property;

    @Column(name = "check_in_time", nullable = false)
    private LocalTime checkInTime;

    @Column(name = "check_out_time", nullable = false)
    private LocalTime checkOutTime;

    @Column(name = "pets_allowed")
    private Boolean petsAllowed = false;

    @Column(name = "smoking_allowed")
    private Boolean smokingAllowed = false;

    @Column(name = "minimum_age")
    private Integer minimumAge = 18;

    @Column(name = "free_cancellation_hours")
    private Integer freeCancellationHours = 48;

    @Column(name = "extra_bed_available")
    private Boolean extraBedAvailable = false;
}