package com.driftstay.room.entity;

import com.driftstay.common.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "room_image",
        indexes = {
                @Index(name = "idx_room_image_room_order", columnList = "room_id,display_order", unique = true),
                @Index(name = "idx_room_image_room", columnList = "room_id")
        }
)
@Getter
@Setter
public class RoomImage extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "room_image_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

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
