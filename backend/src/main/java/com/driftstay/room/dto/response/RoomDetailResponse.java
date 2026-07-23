package com.driftstay.room.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Builder
public class RoomDetailResponse {
    private Long id;
    private String publicId;
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
    private List<String> imageUrls;
}
