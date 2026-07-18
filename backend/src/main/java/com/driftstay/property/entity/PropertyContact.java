package com.driftstay.property.entity;

import com.driftstay.common.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "property_contact",
        indexes = {
                @Index(name = "idx_property_contact_property", columnList = "property_id"),
                @Index(name = "idx_property_contact_primary", columnList = "property_id,is_primary")
        }
)
@Getter
@Setter
public class PropertyContact extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "contact_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "property_id", nullable = false)
    private Property property;

    @Column(name = "contact_name", nullable = false, length = 255)
    private String contactName;

    @Column(length = 100)
    private String designation;

    @Column(length = 255)
    private String email;

    @Column(nullable = false, length = 20)
    private String phone;

    @Column(name = "is_primary")
    private Boolean isPrimary = false;
}
