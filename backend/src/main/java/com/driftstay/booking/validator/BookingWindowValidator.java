package com.driftstay.booking.validator;

import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.Period;

@Component
public class BookingWindowValidator {

    private static final int MIN_NIGHTS = 1;
    private static final int MAX_NIGHTS = 30;
    private static final int MAX_ADVANCE_BOOKING_DAYS = 365;
    private static final int MIN_ADVANCE_BOOKING_HOURS = 1; // At least 1 hour ahead

    /**
     * Validates the booking window constraints.
     *
     * @param checkIn  Check-in date
     * @param checkOut Check-out date
     * @throws IllegalArgumentException if the window is invalid
     */
    public void validate(LocalDate checkIn, LocalDate checkOut) {
        validateDates(checkIn, checkOut);
        validateDuration(checkIn, checkOut);
        validateAdvanceBooking(checkIn);
    }

    /**
     * Validates that dates are logically correct.
     */
    private void validateDates(LocalDate checkIn, LocalDate checkOut) {
        if (checkIn == null || checkOut == null) {
            throw new IllegalArgumentException("Check-in and check-out dates are required");
        }

        if (checkOut.isBefore(checkIn)) {
            throw new IllegalArgumentException(
                    String.format("Check-out date %s cannot be before check-in date %s", checkOut, checkIn)
            );
        }

        if (checkOut.equals(checkIn)) {
            throw new IllegalArgumentException("Check-out date must be after check-in date (minimum 1 night stay)");
        }
    }

    /**
     * Validates the stay duration is within allowed limits.
     */
    private void validateDuration(LocalDate checkIn, LocalDate checkOut) {
        int nights = Period.between(checkIn, checkOut).getDays();

        if (nights < MIN_NIGHTS) {
            throw new IllegalArgumentException(
                    String.format("Minimum stay is %d night(s)", MIN_NIGHTS)
            );
        }

        if (nights > MAX_NIGHTS) {
            throw new IllegalArgumentException(
                    String.format("Maximum stay is %d nights", MAX_NIGHTS)
            );
        }
    }

    /**
     * Validates that the booking is made with sufficient advance notice.
     */
    private void validateAdvanceBooking(LocalDate checkIn) {
        LocalDate today = LocalDate.now();

        if (checkIn.isBefore(today)) {
            throw new IllegalArgumentException(
                    String.format("Check-in date %s cannot be in the past", checkIn)
            );
        }

        if (Period.between(today, checkIn).getDays() > MAX_ADVANCE_BOOKING_DAYS) {
            throw new IllegalArgumentException(
                    String.format("Bookings can only be made up to %d days in advance", MAX_ADVANCE_BOOKING_DAYS)
            );
        }
    }
}
