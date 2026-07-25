package com.driftstay.booking.validator;

import com.driftstay.booking.entity.Booking;
import com.driftstay.booking.service.AvailabilityService;
import com.driftstay.common.enums.BookingStatus;
import com.driftstay.common.enums.PaymentStatus;
import com.driftstay.common.enums.PropertyStatus;
import com.driftstay.common.enums.RoomStatus;
import com.driftstay.exception.BookingConflictException;
import com.driftstay.exception.InvalidBookingStateException;
import com.driftstay.room.entity.Room;
import com.driftstay.room.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.EnumSet;
import java.util.Set;

/**
 * Validates all aspects of a booking request:
 * - Window constraints (dates, min/max stay)
 * - Room and property are active
 * - Guest capacity
 * - Availability (no double booking)
 * - Payment status transitions
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BookingValidator {

    private final BookingWindowValidator windowValidator;
    private final AvailabilityService availabilityService;
    private final RoomRepository roomRepository;

    /** Statuses that prevent a new booking on a room. */
    private static final Set<RoomStatus> ACTIVE_ROOM_STATUSES = EnumSet.of(
            RoomStatus.ACTIVE
    );

    /** Statuses that allow new bookings. */
    private static final Set<com.driftstay.common.enums.PropertyStatus> ACTIVE_PROPERTY_STATUSES = EnumSet.of(
            PropertyStatus.ACTIVE
    );

    /**
     * End-to-end booking request validation.
     * Validates everything before the booking is created.
     */
    public void validateBookingRequest(Long roomId, LocalDate checkIn, LocalDate checkOut,
                                       Integer guestCount, Integer capacity) {
        // 1. Window constraints
        windowValidator.validate(checkIn, checkOut);

        // 2. Room is active
        validateRoomActive(roomId);

        // 3. Guest capacity
        validateGuestCapacity(guestCount, capacity);

        // 4. Availability (double booking check)
        availabilityService.validateAndLockAvailability(roomId, checkIn, checkOut);
    }

    /**
     * Validates a booking modification (dates/guests change).
     */
    public void validateModification(Booking existingBooking, LocalDate newCheckIn,
                                     LocalDate newCheckOut, Integer newGuestCount,
                                     Integer capacity) {
        // Cannot modify cancelled/completed bookings
        validateBookingModifiable(existingBooking);

        windowValidator.validate(newCheckIn, newCheckOut);
        validateGuestCapacity(newGuestCount, capacity);

        // Check availability excluding this booking
        boolean available = availabilityService.isRoomAvailableExcluding(
                existingBooking.getRoomId(), newCheckIn, newCheckOut, existingBooking.getId());

        if (!available) {
            throw new BookingConflictException(
                    existingBooking.getRoomId(), newCheckIn, newCheckOut);
        }
    }

    /**
     * Validate that the room exists and is active.
     */
    public void validateRoomActive(Long roomId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Room not found: " + roomId));

        if (!ACTIVE_ROOM_STATUSES.contains(room.getStatus())) {
            throw new IllegalArgumentException(
                    String.format("Room %s is not available (status: %s)", room.getRoomName(), room.getStatus()));
        }

        // Also check the parent property is active
        if (!ACTIVE_PROPERTY_STATUSES.contains(room.getProperty().getStatus())) {
            throw new IllegalArgumentException(
                    String.format("Property '%s' is not currently active", room.getProperty().getName()));
        }
    }

    /**
     * Validate that a booking can be cancelled (only if in a cancellable state).
     */
    public void validateCancellable(Booking booking) {
        Set<BookingStatus> cancellableStates = EnumSet.of(
                BookingStatus.PENDING,
                BookingStatus.CONFIRMED
        );

        if (!cancellableStates.contains(booking.getBookingStatus())) {
            throw new InvalidBookingStateException(
                    booking.getBookingStatus(), BookingStatus.CANCELLED,
                    booking.getBookingReference());
        }
    }

    /**
     * Validate that a booking can be modified.
     */
    public void validateBookingModifiable(Booking booking) {
        Set<BookingStatus> modifiableStates = EnumSet.of(
                BookingStatus.PENDING,
                BookingStatus.CONFIRMED
        );

        if (!modifiableStates.contains(booking.getBookingStatus())) {
            throw new InvalidBookingStateException(
                    booking.getBookingStatus(), booking.getBookingStatus(),
                    "Booking cannot be modified in its current state: " + booking.getBookingReference());
        }
    }

    /**
     * Validate that a booking can have check-in performed.
     */
    public void validateCheckInReady(Booking booking) {
        if (booking.getBookingStatus() != BookingStatus.CONFIRMED) {
            throw new InvalidBookingStateException(
                    booking.getBookingStatus(), BookingStatus.CHECKED_IN,
                    "Only CONFIRMED bookings can be checked in: " + booking.getBookingReference());
        }

        if (booking.getPaymentStatus() != PaymentStatus.PAID) {
            throw new IllegalArgumentException(
                    "Booking payment is not complete. Status: " + booking.getPaymentStatus());
        }
    }

    // ========================================================================
    // PRIVATE HELPERS
    // ========================================================================

    private void validateGuestCapacity(Integer guestCount, Integer capacity) {
        if (guestCount == null || guestCount < 1) {
            throw new IllegalArgumentException("Guest count must be at least 1");
        }

        if (capacity != null && guestCount > capacity) {
            throw new IllegalArgumentException(
                    String.format("Guest count %d exceeds room capacity of %d", guestCount, capacity));
        }
    }
}
