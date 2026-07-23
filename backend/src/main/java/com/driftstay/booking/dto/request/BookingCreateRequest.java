package com.driftstay.booking.dto.request;

import com.driftstay.common.validation.CreateValidation;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class BookingCreateRequest {
    @NotNull(groups = CreateValidation.class)
    private Long propertyId;
    private Long roomId;
    @NotNull(groups = CreateValidation.class)
    private String bookingType;
    @NotNull(groups = CreateValidation.class)
    @Future(groups = CreateValidation.class)
    private LocalDate checkIn;
    @NotNull(groups = CreateValidation.class)
    @Future(groups = CreateValidation.class)
    private LocalDate checkOut;
    @NotNull(groups = CreateValidation.class)
    private Integer guestCount;
    private String specialRequests;
}
