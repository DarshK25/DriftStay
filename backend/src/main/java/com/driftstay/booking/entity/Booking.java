package com.driftstay.booking.entity;

import com.driftstay.common.BaseEntity;
import com.driftstay.common.enums.BookingSource;
import com.driftstay.common.enums.BookingStatus;
import com.driftstay.common.enums.BookingType;
import com.driftstay.common.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "booking",
        indexes = {
                @Index(name = "idx_booking_public_id", columnList = "public_id", unique = true),
                @Index(name = "idx_booking_ref", columnList = "booking_reference", unique = true),
                @Index(name = "idx_booking_user", columnList = "user_id"),
                @Index(name = "idx_booking_property", columnList = "property_id"),
                @Index(name = "idx_booking_status", columnList = "booking_status"),
                @Index(name = "idx_booking_dates", columnList = "check_in,check_out")
        }
)
@Getter
@Setter
public class Booking extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "booking_id")
    private Long id;

    @Column(name = "public_id", nullable = false, unique = true, length = 26)
    private String publicId;

    @Column(name = "booking_reference", nullable = false, unique = true, length = 20)
    private String bookingReference;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "property_id", nullable = false)
    private Long propertyId;

    @Column(name = "room_id")
    private Long roomId;

    @Enumerated(EnumType.STRING)
    @Column(name = "booking_type", nullable = false, length = 20)
    private BookingType bookingType;

    @Column(name = "check_in", nullable = false)
    private LocalDate checkIn;

    @Column(name = "check_out", nullable = false)
    private LocalDate checkOut;

    @Column(name = "guest_count", nullable = false)
    private Integer guestCount;

    @Enumerated(EnumType.STRING)
    @Column(name = "booking_status", nullable = false, length = 20)
    private BookingStatus bookingStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false, length = 20)
    private PaymentStatus paymentStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "booking_source", nullable = false, length = 20)
    private BookingSource bookingSource;

    @Column(name = "special_requests", columnDefinition = "TEXT")
    private String specialRequests;

    @Column(name = "base_price_snapshot", precision = 12, scale = 2)
    private BigDecimal basePriceSnapshot;

    @Column(name = "cleaning_fee_snapshot", precision = 12, scale = 2)
    private BigDecimal cleaningFeeSnapshot;

    @Column(name = "tax_snapshot", precision = 12, scale = 2)
    private BigDecimal taxSnapshot;

    @Column(name = "discount_snapshot", precision = 12, scale = 2)
    private BigDecimal discountSnapshot;

    @Column(name = "total_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount;

    @PrePersist
    void generatePublicId() {
        if (publicId == null) {
            publicId = UUID.randomUUID().toString().replace("-", "").substring(0, 26);
        }
    }
}
