package com.driftstay.booking.dto;

import com.driftstay.common.enums.BookingStatus;
import com.driftstay.common.enums.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingSummary {

    private String publicId;
    private String bookingReference;
    private Long propertyId;
    private String propertyName;
    private String propertyCity;
    private Long roomId;
    private String roomName;
    private LocalDate checkIn;
    private LocalDate checkOut;
    private int guestCount;
    private BookingStatus bookingStatus;
    private PaymentStatus paymentStatus;
    private BigDecimal totalAmount;
    private LocalDateTime createdAt;
}
