package com.driftstay.room.dto.request;

import com.driftstay.common.validation.CreateValidation;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class RoomCreateRequest {
    @NotBlank(groups = CreateValidation.class)
    private String roomName;
    private String roomNumber;
    private String description;
    @NotBlank(groups = CreateValidation.class)
    private String roomType;
    @NotNull(groups = CreateValidation.class)
    private Integer capacity;
    @NotNull(groups = CreateValidation.class)
    private Integer bedCount;
    @NotBlank(groups = CreateValidation.class)
    private String bedType;
    private Integer bathroomCount;
    @NotNull(groups = CreateValidation.class)
    private BigDecimal basePrice;
    private BigDecimal weekendPrice;
    private BigDecimal cleaningFee;
    private BigDecimal extraGuestFee;
    private Integer areaSqft;
    private Integer floorNumber;
    @NotBlank(groups = CreateValidation.class)
    private String status;
}
