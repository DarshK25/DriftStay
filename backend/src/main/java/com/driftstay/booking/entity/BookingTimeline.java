package com.driftstay.booking.entity;

import com.driftstay.common.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "booking_timeline",
        indexes = {
                @Index(name = "idx_booking_timeline_booking", columnList = "booking_id"),
                @Index(name = "idx_booking_timeline_created", columnList = "booking_id,created_at")
        }
)
@Getter
@Setter
public class BookingTimeline extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "timeline_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    @Column(name = "event_type", nullable = false, length = 50)
    private String eventType;

    @Column(name = "performed_by", nullable = false, length = 100)
    private String performedBy;

    @Column(columnDefinition = "TEXT")
    private String remarks;
}
