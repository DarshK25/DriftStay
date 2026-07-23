package com.driftstay.booking.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
public class BookingResponse {
    private Long id;
    private String publicId;
    private String bookingReference;
    private Long userId;
    private Long propertyId;
    private Long roomId;
    private String bookingType;
    private LocalDate checkIn;
    private LocalDate checkOut;
    private Integer guestCount;
    private String bookingStatus;
    private String paymentStatus;
    private BigDecimal totalAmount;
    private String specialRequests;
    private LocalDateTime createdAt;
}
