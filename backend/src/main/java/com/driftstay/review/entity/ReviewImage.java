package com.driftstay.review.entity;

import com.driftstay.common.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "review_image",
        indexes = {
                @Index(name = "idx_review_image_review", columnList = "review_id"),
                @Index(name = "idx_review_image_order", columnList = "review_id,display_order", unique = true)
        }
)
@Getter
@Setter
public class ReviewImage extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "review_image_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "review_id", nullable = false)
    private Review review;

    @Column(name = "image_url", nullable = false, length = 500)
    private String imageUrl;

    @Column(name = "display_order", nullable = false)
    private Integer displayOrder;
}