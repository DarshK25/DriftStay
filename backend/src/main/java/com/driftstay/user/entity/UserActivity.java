package com.driftstay.user.entity;

import com.driftstay.common.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "user_activity",
        indexes = {
                @Index(name = "idx_user_activity_user", columnList = "user_id"),
                @Index(name = "idx_user_activity_type", columnList = "activity_type"),
                @Index(name = "idx_user_activity_entity", columnList = "entity_type,entity_id"),
                @Index(name = "idx_user_activity_created", columnList = "created_at")
        }
)
@Getter
@Setter
public class UserActivity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "activity_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "activity_type", nullable = false, length = 50)
    private String activityType;

    @Column(name = "entity_type", length = 50)
    private String entityType;

    @Column(name = "entity_id")
    private Long entityId;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "user_agent", columnDefinition = "TEXT")
    private String userAgent;

    @Column(length = 100)
    private String city;

    @Column(length = 100)
    private String country;
}