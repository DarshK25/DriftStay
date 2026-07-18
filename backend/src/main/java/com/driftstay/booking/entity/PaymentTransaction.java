package com.driftstay.booking.entity;

import com.driftstay.common.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "payment_transaction",
        indexes = {
                @Index(name = "idx_payment_txn_payment", columnList = "payment_id"),
                @Index(name = "idx_payment_txn_gateway", columnList = "gateway"),
                @Index(name = "idx_payment_txn_status", columnList = "status")
        }
)
@Getter
@Setter
public class PaymentTransaction extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "transaction_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id", nullable = false)
    private Payment payment;

    @Column(nullable = false, length = 50)
    private String gateway;

    @Column(name = "gateway_transaction_id", length = 255)
    private String gatewayTransactionId;

    @Column(name = "gateway_response", columnDefinition = "TEXT")
    private String gatewayResponse;

    @Column(nullable = false, length = 20)
    private String status;
}