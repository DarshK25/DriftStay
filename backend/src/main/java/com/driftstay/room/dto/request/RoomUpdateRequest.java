package com.driftstay.room.dto.request;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class RoomUpdateRequest {
    private String roomName;
    private String roomNumber;
    private String description;
    private String roomType;
    private Integer capacity;
    private Integer bedCount;
    private String bedType;
    private Integer bathroomCount;
    private BigDecimal basePrice;
    private BigDecimal weekendPrice;
    private BigDecimal cleaningFee;
    private BigDecimal extraGuestFee;
    private Integer areaSqft;
    private Integer floorNumber;
    private String status;
}
