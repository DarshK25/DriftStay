package com.driftstay.user.entity;

import com.driftstay.common.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "wishlist",
        uniqueConstraints = @UniqueConstraint(name = "uk_wishlist_user_property", columnNames = {"user_id", "property_id"}),
        indexes = {
                @Index(name = "idx_wishlist_user", columnList = "user_id"),
                @Index(name = "idx_wishlist_property", columnList = "property_id")
        }
)
@Getter
@Setter
public class Wishlist extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "wishlist_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "property_id", nullable = false)
    private Long propertyId;
}