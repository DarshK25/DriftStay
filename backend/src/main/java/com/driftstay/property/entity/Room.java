package com.driftstay.property.entity;

import com.driftstay.common.BaseEntity;
import com.driftstay.common.enums.RoomStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "room",
        indexes = {
                @Index(name = "idx_room_property_status", columnList = "property_id,status"),
                @Index(name = "uk_room_property_number", columnList = "property_id,room_number", unique = true)
        }
)
@Getter
@Setter
public class Room extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "room_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "property_id", nullable = false)
    private Property property;

    @Column(name = "room_number", length = 20)
    private String roomNumber;

    @Column(name = "room_name", nullable = false, length = 255)
    private String roomName;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "room_type", nullable = false, length = 50)
    private String roomType;

    @Column(nullable = false)
    private Integer capacity;

    @Column(name = "bed_count", nullable = false)
    private Integer bedCount;

    @Column(name = "bed_type", nullable = false, length = 50)
    private String bedType;

    @Column(name = "bathroom_count")
    private Integer bathroomCount;

    @Column(name = "base_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal basePrice;

    @Column(name = "weekend_price", precision = 10, scale = 2)
    private BigDecimal weekendPrice;

    @Column(name = "cleaning_fee", precision = 10, scale = 2)
    private BigDecimal cleaningFee;

    @Column(name = "extra_guest_fee", precision = 10, scale = 2)
    private BigDecimal extraGuestFee;

    @Column(name = "area_sqft")
    private Integer areaSqft;

    @Column(name = "floor_number")
    private Integer floorNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RoomStatus status;

    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RoomImage> images = new ArrayList<>();

    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RoomAvailability> availabilities = new ArrayList<>();
}