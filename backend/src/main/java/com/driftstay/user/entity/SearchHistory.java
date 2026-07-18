package com.driftstay.user.entity;

import com.driftstay.common.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "search_history",
        indexes = {
                @Index(name = "idx_search_history_user", columnList = "user_id"),
                @Index(name = "idx_search_history_created", columnList = "created_at")
        }
)
@Getter
@Setter
public class SearchHistory extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "search_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(length = 100)
    private String city;

    @Column(name = "check_in")
    private LocalDate checkIn;

    @Column(name = "check_out")
    private LocalDate checkOut;

    @Column(nullable = false)
    private Integer guests;

    @Column(columnDefinition = "TEXT")
    private String filters;
}
