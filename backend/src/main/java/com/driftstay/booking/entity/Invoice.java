package com.driftstay.booking.entity;

import com.driftstay.common.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "invoice",
        indexes = {
                @Index(name = "idx_invoice_number", columnList = "invoice_number", unique = true),
                @Index(name = "idx_invoice_booking", columnList = "booking_id")
        }
)
@Getter
@Setter
public class Invoice extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "invoice_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    @Column(name = "invoice_number", nullable = false, unique = true, length = 50)
    private String invoiceNumber;

    @Column(name = "pdf_url", length = 500)
    private String pdfUrl;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal;

    @Column(precision = 10, scale = 2)
    private BigDecimal tax;

    @Column(precision = 10, scale = 2)
    private BigDecimal discount;

    @Column(name = "grand_total", nullable = false, precision = 10, scale = 2)
    private BigDecimal grandTotal;

    @Column(nullable = false, length = 20)
    private String status;

    @Column(name = "issued_at")
    private LocalDateTime issuedAt;
}