package com.driftstay.notification.entity;

import com.driftstay.common.BaseEntity;
import com.driftstay.common.enums.NotificationChannel;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "notification_template",
        indexes = {
                @Index(name = "idx_notification_template_name", columnList = "name", unique = true),
                @Index(name = "idx_notification_template_channel", columnList = "channel")
        }
)
@Getter
@Setter
public class NotificationTemplate extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "template_id")
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private NotificationChannel channel;

    @Column(length = 255)
    private String subject;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String body;

    @Column(columnDefinition = "TEXT")
    private String variables;
}