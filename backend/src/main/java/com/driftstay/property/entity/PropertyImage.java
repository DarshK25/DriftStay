package com.driftstay.property.entity;

import com.driftstay.common.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "property_image",
        indexes = {
                @Index(name = "idx_property_image_property_order", columnList = "property_id,display_order", unique = true),
                @Index(name = "idx_property_image_property", columnList = "property_id")
        }
)
@Getter
@Setter
public class PropertyImage extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "image_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "property_id", nullable = false)
    private Property property;

    @Column(name = "image_url", nullable = false, length = 500)
    private String imageUrl;

    @Column(name = "alt_text", length = 255)
    private String altText;

    @Column(length = 255)
    private String caption;

    @Column(name = "display_order", nullable = false)
    private Integer displayOrder;

    @Column(name = "is_thumbnail")
    private Boolean isThumbnail = false;
}
