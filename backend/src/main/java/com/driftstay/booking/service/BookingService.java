package com.driftstay.booking.service;

import com.driftstay.booking.entity.Booking;
import com.driftstay.common.enums.BookingSource;
import com.driftstay.common.enums.BookingStatus;
import com.driftstay.common.enums.PaymentStatus;
import com.driftstay.common.exception.ResourceNotFoundException;
import com.driftstay.booking.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class BookingService {

    private final BookingRepository bookingRepository;

    @Transactional
    public Booking createBooking(Booking booking) {
        booking.setBookingReference(generateBookingReference());
        booking.setBookingStatus(BookingStatus.PENDING);
        booking.setPaymentStatus(PaymentStatus.UNPAID);
        booking.setBookingSource(BookingSource.WEBSITE);
        Booking saved = bookingRepository.save(booking);
        log.info("Created booking: {} (ref: {})", saved.getPublicId(), saved.getBookingReference());
        return saved;
    }

    @Transactional
    public Booking cancelBooking(String publicId) {
        Booking booking = bookingRepository.findByPublicId(publicId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found: " + publicId));
        booking.setBookingStatus(BookingStatus.CANCELLED);
        Booking saved = bookingRepository.save(booking);
        log.info("Cancelled booking: {} (ref: {})", publicId, saved.getBookingReference());
        return saved;
    }

    public Booking getBookingByPublicId(String publicId) {
        return bookingRepository.findByPublicId(publicId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found: " + publicId));
    }

    public Page<Booking> getUserBookings(Long userId, Pageable pageable) {
        return bookingRepository.findByUserId(userId, pageable);
    }

    public boolean isRoomAvailable(Long propertyId, Long roomId, LocalDate checkIn, LocalDate checkOut) {
        return !bookingRepository.existsByPropertyIdAndRoomIdAndCheckInLessThanAndCheckOutGreaterThan(
                propertyId, roomId, checkOut, checkIn);
    }

    private String generateBookingReference() {
        return "DRIFT-" + UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
    }
}
