package com.driftstay.property.entity;

import com.driftstay.common.BaseEntity;
import com.driftstay.common.enums.AvailabilityStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "room_availability",
        indexes = {
                @Index(name = "idx_room_availability_room_dates", columnList = "room_id,start_date,end_date"),
                @Index(name = "idx_room_availability_room", columnList = "room_id"),
                @Index(name = "idx_room_availability_dates", columnList = "start_date,end_date"),
                @Index(name = "idx_room_availability_status", columnList = "status")
        }
)
@Getter
@Setter
public class RoomAvailability extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "availability_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AvailabilityStatus status;

    @Column(length = 255)
    private String reason;

    @Column(name = "created_by", nullable = false, length = 100)
    private String createdBy;
}
