package com.driftstay.booking.entity;

import com.driftstay.common.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payment",
        indexes = {
                @Index(name = "idx_payment_booking", columnList = "booking_id"),
                @Index(name = "idx_payment_status", columnList = "status")
        }
)
@Getter
@Setter
public class Payment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payment_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, length = 3)
    private String currency = "INR";

    @Column(name = "payment_method", nullable = false, length = 50)
    private String paymentMethod;

    @Column(nullable = false, length = 50)
    private String provider;

    @Column(name = "transaction_reference", length = 255)
    private String transactionReference;

    @Column(nullable = false, length = 20)
    private String status;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;
}