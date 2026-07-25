package com.driftstay.booking.dto;

import com.driftstay.common.enums.BookingSource;
import com.driftstay.common.enums.BookingStatus;
import com.driftstay.common.enums.BookingType;
import com.driftstay.common.enums.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingResponse {

    private Long id;
    private String publicId;
    private String bookingReference;
    private Long userId;
    private Long propertyId;
    private Long roomId;
    private BookingType bookingType;
    private LocalDate checkIn;
    private LocalDate checkOut;
    private int guestCount;
    private BookingStatus bookingStatus;
    private PaymentStatus paymentStatus;
    private BookingSource bookingSource;
    private BigDecimal totalAmount;
    private BigDecimal basePriceSnapshot;
    private BigDecimal cleaningFeeSnapshot;
    private BigDecimal taxSnapshot;
    private BigDecimal discountSnapshot;
    private String specialRequests;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
