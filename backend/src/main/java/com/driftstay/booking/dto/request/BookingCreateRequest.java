package com.driftstay.booking.dto.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class BookingCreateRequest {
    @NotNull
    private Long propertyId;
    private Long roomId;
    @NotNull
    private String bookingType;
    @NotNull
    @Future
    private LocalDate checkIn;
    @NotNull
    @Future
    private LocalDate checkOut;
    @NotNull
    private Integer guestCount;
    private String specialRequests;
}
