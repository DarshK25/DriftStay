package com.driftstay.booking.service;

import com.driftstay.booking.entity.Booking;
import com.driftstay.booking.repository.BookingRepository;
import com.driftstay.common.enums.AvailabilityStatus;
import com.driftstay.common.enums.BookingStatus;
import com.driftstay.room.entity.Room;
import com.driftstay.room.entity.RoomAvailability;
import com.driftstay.room.repository.RoomAvailabilityRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;

/**
 * Generates and manages availability calendars for rooms.
 * Produces a 365-day view showing which dates are:
 * - AVAILABLE: Open for booking
 * - BOOKED: Occupied by a confirmed/pending booking
 * - BLOCKED: Blocked by the property owner
 * - MAINTENANCE: Under maintenance
 * - RESERVED: Reserved for special circumstances
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AvailabilityCalendarService {

    private final BookingRepository bookingRepository;
    private final RoomAvailabilityRepository roomAvailabilityRepository;
    private final EntityManager entityManager;

    /**
     * Generate a 365-day availability calendar for a room.
     *
     * @param roomId     The room ID
     * @param fromDate   Start date (defaults to today if null)
     * @return Map of date to availability status for 365 days
     */
    @Transactional(readOnly = true)
    public Map<LocalDate, AvailabilityStatus> generateCalendar(Long roomId, LocalDate fromDate) {
        if (fromDate == null) {
            fromDate = LocalDate.now();
        }
        LocalDate toDate = fromDate.plusDays(365);

        log.debug("Generating availability calendar for room {} from {} to {}", roomId, fromDate, toDate);

        // 1. Get all bookings in range
        List<Booking> bookings = bookingRepository.findBookingsInRange(roomId, fromDate, toDate);

        // 2. Get all manual availability blocks in range
        List<RoomAvailability> availabilityBlocks =
                roomAvailabilityRepository.findAvailabilityInRange(roomId, fromDate, toDate);

        // 3. Build the calendar
        Map<LocalDate, AvailabilityStatus> calendar = new LinkedHashMap<>();
        LocalDate date = fromDate;

        while (!date.isAfter(toDate)) {
            calendar.put(date, AvailabilityStatus.AVAILABLE);
            date = date.plusDays(1);
        }

        // 4. Mark booked dates from active bookings
        Set<BookingStatus> activeStatuses = Set.of(
                BookingStatus.PENDING,
                BookingStatus.CONFIRMED,
                BookingStatus.CHECKED_IN
        );

        for (Booking booking : bookings) {
            if (activeStatuses.contains(booking.getBookingStatus())) {
                LocalDate bookedDate = booking.getCheckIn();
                while (bookedDate.isBefore(booking.getCheckOut())) {
                    if (calendar.containsKey(bookedDate)) {
                        calendar.put(bookedDate, AvailabilityStatus.RESERVED);
                    }
                    bookedDate = bookedDate.plusDays(1);
                }
            }
        }

        // 5. Mark blocked/maintenance/reserved dates (these override AVAILABLE but not BOOKED)
        //    We use a priority: MAINTENANCE > BLOCKED > RESERVED > AVAILABLE
        for (RoomAvailability block : availabilityBlocks) {
            LocalDate blockDate = block.getStartDate();
            while (!blockDate.isAfter(block.getEndDate())) {
                if (calendar.containsKey(blockDate)) {
                    AvailabilityStatus currentStatus = calendar.get(blockDate);
                    AvailabilityStatus newStatus = block.getStatus();

                    // Override only if the new status has higher priority
                    if (hasHigherPriority(newStatus, currentStatus)) {
                        calendar.put(blockDate, newStatus);
                    }
                }
                blockDate = blockDate.plusDays(1);
            }
        }

        log.debug("Calendar generated for room {}: {} available days out of 365",
                roomId,
                calendar.values().stream().filter(s -> s == AvailabilityStatus.AVAILABLE).count());

        return calendar;
    }

    /**
     * Determine status priority for calendar overlay.
     * Higher priority wins when there are conflicts.
     */
    private boolean hasHigherPriority(AvailabilityStatus newStatus, AvailabilityStatus currentStatus) {
        Map<AvailabilityStatus, Integer> priority = Map.of(
                AvailabilityStatus.MAINTENANCE, 4,
                AvailabilityStatus.BLOCKED, 3,
                AvailabilityStatus.RESERVED, 2,
                AvailabilityStatus.AVAILABLE, 1
        );

        return priority.getOrDefault(newStatus, 0) > priority.getOrDefault(currentStatus, 0);
    }

    /**
     * Block a room for a date range (e.g., owner booking, maintenance).
     *
     * @param roomId     The room to block
     * @param startDate  When the block starts
     * @param endDate    When the block ends
     * @param status     The status (BLOCKED, MAINTENANCE)
     * @param reason     Why it's blocked
     * @param createdBy  Who created the block
     */
    @Transactional
    public RoomAvailability blockRoom(Long roomId, LocalDate startDate, LocalDate endDate,
                                      AvailabilityStatus status, String reason, String createdBy) {
        Room room = entityManager.getReference(Room.class, roomId);
        
        RoomAvailability availability = new RoomAvailability();
        availability.setRoom(room);
        availability.setStartDate(startDate);
        availability.setEndDate(endDate);
        availability.setStatus(status);
        availability.setReason(reason);
        availability.setCreatedBy(createdBy);

        RoomAvailability saved = roomAvailabilityRepository.save(availability);
        log.info("Room {} blocked from {} to {}: {} (by {})", roomId, startDate, endDate, status, createdBy);

        return saved;
    }
}
