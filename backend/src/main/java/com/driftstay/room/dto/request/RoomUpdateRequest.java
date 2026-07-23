package com.driftstay.room.dto.request;

import com.driftstay.common.validation.UpdateValidation;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class RoomUpdateRequest {
    @Size(max = 255, groups = UpdateValidation.class)
    private String roomName;
    @Size(max = 50, groups = UpdateValidation.class)
    private String roomNumber;
    @Size(max = 1000, groups = UpdateValidation.class)
    private String description;
    @Size(max = 50, groups = UpdateValidation.class)
    private String roomType;
    private Integer capacity;
    private Integer bedCount;
    @Size(max = 50, groups = UpdateValidation.class)
    private String bedType;
    private Integer bathroomCount;
    private BigDecimal basePrice;
    private BigDecimal weekendPrice;
    private BigDecimal cleaningFee;
    private BigDecimal extraGuestFee;
    private Integer areaSqft;
    private Integer floorNumber;
    @Size(max = 50, groups = UpdateValidation.class)
    private String status;
}
