package com.driftstay.room.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class RoomCreateRequest {
    @NotBlank
    private String roomName;
    private String roomNumber;
    private String description;
    @NotBlank
    private String roomType;
    @NotNull
    private Integer capacity;
    @NotNull
    private Integer bedCount;
    @NotBlank
    private String bedType;
    private Integer bathroomCount;
    @NotNull
    private BigDecimal basePrice;
    private BigDecimal weekendPrice;
    private BigDecimal cleaningFee;
    private BigDecimal extraGuestFee;
    private Integer areaSqft;
    private Integer floorNumber;
    @NotBlank
    private String status;
}
