package com.driftstay.booking.entity;

import com.driftstay.common.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "booking_guest",
        indexes = {
                @Index(name = "idx_booking_guest_booking", columnList = "booking_id")
        }
)
@Getter
@Setter
public class BookingGuest extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "guest_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    @Column(name = "full_name", nullable = false, length = 255)
    private String fullName;

    private Integer age;

    @Column(length = 10)
    private String gender;

    @Column(name = "government_id", length = 50)
    private String governmentId;

    @Column(name = "is_primary_guest")
    private Boolean isPrimaryGuest = false;
}
