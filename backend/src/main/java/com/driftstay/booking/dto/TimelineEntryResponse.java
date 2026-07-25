package com.driftstay.booking.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TimelineEntryResponse {

    private Long id;
    private String eventType;
    private String performedBy;
    private String remarks;
    private LocalDateTime createdAt;
}
