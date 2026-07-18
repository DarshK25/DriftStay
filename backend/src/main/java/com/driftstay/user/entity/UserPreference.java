package com.driftstay.user.entity;

import com.driftstay.common.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "user_preference")
@Getter
@Setter
public class UserPreference extends BaseEntity {

    @Id
    @Column(name = "user_id")
    private Long userId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "preferred_currency", length = 3)
    private String preferredCurrency = "INR";

    @Column(name = "preferred_language", length = 10)
    private String preferredLanguage = "en";

    @Column(name = "preferred_city", length = 100)
    private String preferredCity;

    @Column(length = 20)
    private String theme = "LIGHT";

    @Column(name = "marketing_emails")
    private Boolean marketingEmails = true;

    @Column(name = "email_notifications")
    private Boolean emailNotifications = true;

    @Column(name = "push_notifications")
    private Boolean pushNotifications = true;

    @Column(name = "sms_notifications")
    private Boolean smsNotifications = false;
}