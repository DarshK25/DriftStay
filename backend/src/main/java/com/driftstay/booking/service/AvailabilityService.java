package com.driftstay.booking.service;

import com.driftstay.booking.entity.Booking;
import com.driftstay.booking.repository.BookingRepository;
import com.driftstay.common.enums.AvailabilityStatus;
import com.driftstay.common.enums.BookingStatus;
import com.driftstay.room.entity.RoomAvailability;
import com.driftstay.room.repository.RoomAvailabilityRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

/**
 * Core availability service handling all overlap types:
 *
 * Exact overlap:      new=[1st-5th], existing=[1st-5th]  → CONFLICT
 * Partial overlap:    new=[1st-5th], existing=[3rd-7th]  → CONFLICT
 * Enclosing overlap:  new=[1st-7th], existing=[3rd-5th]  → CONFLICT
 * Contained overlap:  new=[3rd-5th], existing=[1st-7th]  → CONFLICT
 * Adjacent (no gap):  new=[1st-5th], existing=[5th-10th] → OK (checkOut == checkIn is allowed for same-day turnover)
 *
 * Also handles:
 * - Cancelled bookings: ignored (not counted as conflicts)
 * - Pending expiry: PENDING bookings past check-in are auto-failed candidates
 * - Maintenance: blocks booking entirely
 * - Blocked dates: owner-blocks override availability
 *
 * Uses PESSIMISTIC_WRITE lock with 5-second timeout for race condition prevention.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AvailabilityService {

    private final BookingRepository bookingRepository;
    private final RoomAvailabilityRepository roomAvailabilityRepository;

    /** Active booking statuses that occupy a room (non-terminal). */
    private static final Set<BookingStatus> ACTIVE_BOOKING_STATUSES = Set.of(
            BookingStatus.PENDING,
            BookingStatus.CONFIRMED,
            BookingStatus.CHECKED_IN
    );

    /** Terminal statuses — these bookings don't occupy the room. */
    private static final Set<BookingStatus> TERMINAL_STATUSES = Set.of(
            BookingStatus.CANCELLED,
            BookingStatus.COMPLETED,
            BookingStatus.FAILED,
            BookingStatus.REFUNDED,
            BookingStatus.NO_SHOW
    );

    /** Blocked availability statuses that prevent booking (overrides active bookings). */
    private static final List<AvailabilityStatus> BLOCKED_STATUSES = List.of(
            AvailabilityStatus.BLOCKED,
            AvailabilityStatus.MAINTENANCE,
            AvailabilityStatus.RESERVED
    );

    // ========================================================================
    // PUBLIC API
    // ========================================================================

    /**
     * Check if a room is available for the full date range.
     * Handles all overlap types: exact, partial, enclosing, contained.
     */
    @Transactional(readOnly = true)
    public boolean isRoomAvailable(Long roomId, LocalDate checkIn, LocalDate checkOut) {
        log.debug("Checking availability for room {} from {} to {}", roomId, checkIn, checkOut);

        // 1. Check for any overlapping active booking
        boolean hasOverlap = bookingRepository.existsOverlap(
                roomId, checkIn, checkOut, ACTIVE_BOOKING_STATUSES);

        // 2. Check for blocked/maintenance periods
        boolean hasBlock = hasActiveBlock(roomId, checkIn, checkOut);

        boolean available = !hasOverlap && !hasBlock;

        log.debug("Room {} availability: {} (overlap={}, block={})",
                roomId, available, hasOverlap, hasBlock);

        return available;
    }

    /**
     * Check availability excluding a specific booking (for modifications/updates).
     */
    @Transactional(readOnly = true)
    public boolean isRoomAvailableExcluding(Long roomId, LocalDate checkIn,
                                            LocalDate checkOut, Long excludeBookingId) {
        if (hasActiveBlock(roomId, checkIn, checkOut)) {
            return false;
        }

        List<Booking> conflicts = bookingRepository.findConflictingBookings(
                roomId, checkIn, checkOut, ACTIVE_BOOKING_STATUSES);

        boolean hasConflict = conflicts.stream()
                .anyMatch(b -> !b.getId().equals(excludeBookingId));

        return !hasConflict;
    }

    /**
     * Validate availability WITH pessimistic lock — the primary race condition defense.
     * Call this inside a @Transactional method BEFORE inserting the new booking.
     *
     * Sequence: ACQUIRE LOCK → VALIDATE → INSERT → COMMIT
     * NOT: VALIDATE → INSERT (would allow race condition)
     */
    @Transactional
    public void validateAndLockAvailability(Long roomId, LocalDate checkIn, LocalDate checkOut) {
        log.debug("Locking and validating availability for room {} from {} to {}", roomId, checkIn, checkOut);

        // PESSIMISTIC_WRITE lock — this blocks other transactions from reading/writing
        // these rows until our transaction commits
        List<Booking> conflicts = bookingRepository.findConflictingBookingsWithLock(
                roomId, checkIn, checkOut, ACTIVE_BOOKING_STATUSES);

        if (!conflicts.isEmpty()) {
            log.warn("Double booking detected for room {} from {} to {}. Conflicts: {}",
                    roomId, checkIn, checkOut, conflicts.size());
            throw new com.driftstay.exception.BookingConflictException(roomId, checkIn, checkOut);
        }

        if (hasActiveBlock(roomId, checkIn, checkOut)) {
            log.warn("Room {} has blocked dates from {} to {}", roomId, checkIn, checkOut);
            throw new com.driftstay.exception.BookingConflictException(roomId, checkIn, checkOut);
        }

        // Also check that the room's availability records aren't locked/modified
        List<RoomAvailability> blockedRecords = roomAvailabilityRepository
                .findBlockedAvailability(roomId, checkIn, checkOut, BLOCKED_STATUSES);

        if (!blockedRecords.isEmpty()) {
            log.warn("Room {} has blocked availability from {} to {}", roomId, checkIn, checkOut);
            throw new com.driftstay.exception.BookingConflictException(
                    String.format("Room %d is blocked by owner from %s to %s", roomId, checkIn, checkOut));
        }
    }

    /**
     * Get the overlap description for a potential booking (for detailed error messages).
     */
    @Transactional(readOnly = true)
    public String getOverlapDescription(Long roomId, LocalDate checkIn, LocalDate checkOut) {
        List<Booking> conflicts = bookingRepository.findConflictingBookings(
                roomId, checkIn, checkOut, ACTIVE_BOOKING_STATUSES);

        if (conflicts.isEmpty()) {
            return "No conflicts found";
        }

        Booking conflict = conflicts.get(0);
        String overlapType = determineOverlapType(checkIn, checkOut, conflict.getCheckIn(), conflict.getCheckOut());

        return String.format(
                "%s overlap with booking %s (status=%s, dates=%s to %s)",
                overlapType, conflict.getBookingReference(), conflict.getBookingStatus(),
                conflict.getCheckIn(), conflict.getCheckOut()
        );
    }

    // ========================================================================
    // SCHEDULED TASKS (for background processing)
    // ========================================================================

    /**
     * Find expired PENDING bookings that should be auto-cancelled.
     */
    @Transactional(readOnly = true)
    public List<Booking> getExpiredPendingBookings() {
        return bookingRepository.findExpiredPendingBookings(LocalDate.now());
    }

    /**
     * Find bookings due for auto-checkout.
     */
    @Transactional(readOnly = true)
    public List<Booking> getBookingsDueForCheckout() {
        return bookingRepository.findBookingsDueForCheckout(LocalDate.now());
    }

    // ========================================================================
    // PRIVATE HELPERS
    // ========================================================================

    /** Check for overlapping active bookings. */
    private boolean hasOverlappingBooking(Long roomId, LocalDate checkIn, LocalDate checkOut) {
        return bookingRepository.existsOverlap(roomId, checkIn, checkOut, ACTIVE_BOOKING_STATUSES);
    }

    /** Check for blocked availability periods. */
    private boolean hasActiveBlock(Long roomId, LocalDate checkIn, LocalDate checkOut) {
        List<RoomAvailability> blocks = roomAvailabilityRepository.findBlockedAvailability(
                roomId, checkIn, checkOut, BLOCKED_STATUSES);
        return !blocks.isEmpty();
    }

    /**
     * Determine the type of overlap between new and existing date ranges.
     * Useful for detailed error reporting.
     */
    private String determineOverlapType(LocalDate newStart, LocalDate newEnd,
                                         LocalDate existingStart, LocalDate existingEnd) {
        boolean sameStart = newStart.equals(existingStart);
        boolean sameEnd = newEnd.equals(existingEnd);

        if (sameStart && sameEnd) return "Exact";
        if (!newStart.isBefore(existingStart) && !newEnd.isAfter(existingEnd)) return "Contained";
        if (!existingStart.isBefore(newStart) && !existingEnd.isAfter(newEnd)) return "Enclosing";
        return "Partial";
    }
}
