package com.driftstay.notification.entity;

import com.driftstay.common.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "notification_log",
        indexes = {
                @Index(name = "idx_notification_log_notification", columnList = "notification_id"),
                @Index(name = "idx_notification_log_status", columnList = "status"),
                @Index(name = "idx_notification_log_provider", columnList = "provider")
        }
)
@Getter
@Setter
public class NotificationLog extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "log_id")
    private Long id;

    @Column(name = "notification_id", nullable = false)
    private Long notificationId;

    @Column(nullable = false, length = 50)
    private String provider;

    @Column(name = "provider_response", columnDefinition = "TEXT")
    private String providerResponse;

    @Column(nullable = false, length = 20)
    private String status;
}