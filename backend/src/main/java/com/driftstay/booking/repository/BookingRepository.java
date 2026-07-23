package com.driftstay.booking.repository;

import com.driftstay.booking.entity.Booking;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    Optional<Booking> findByPublicId(String publicId);
    Optional<Booking> findByBookingReference(String bookingReference);
    Page<Booking> findByUserId(Long userId, Pageable pageable);
    List<Booking> findByPropertyId(Long propertyId);
    boolean existsByPropertyIdAndRoomIdAndCheckInLessThanAndCheckOutGreaterThan(
            Long propertyId, Long roomId, LocalDate checkOut, LocalDate checkIn);
}
