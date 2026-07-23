package com.driftstay.room.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class RoomSummaryResponse {
    private Long id;
    private String publicId;
    private String roomName;
    private String roomNumber;
    private String roomType;
    private Integer capacity;
    private Integer bedCount;
    private String bedType;
    private BigDecimal basePrice;
    private BigDecimal weekendPrice;
    private String status;
    private String thumbnailUrl;
}
