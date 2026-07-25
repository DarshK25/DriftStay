package com.driftstay.booking.controller;

import com.driftstay.booking.dto.BookingRequest;
import com.driftstay.booking.dto.BookingResponse;
import com.driftstay.booking.dto.BookingSummary;
import com.driftstay.booking.dto.TimelineEntryResponse;
import com.driftstay.booking.dto.response.PriceBreakdown;
import com.driftstay.booking.entity.Booking;
import com.driftstay.booking.entity.BookingTimeline;
import com.driftstay.booking.entity.Invoice;
import com.driftstay.booking.service.BookingService;
import com.driftstay.common.enums.BookingSource;
import com.driftstay.common.enums.BookingStatus;
import com.driftstay.security.filter.JwtUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    /**
     * Create a new booking.
     * POST /api/bookings
     */
    @PostMapping
    public ResponseEntity<BookingResponse> createBooking(
            @Valid @RequestBody BookingRequest request,
            @AuthenticationPrincipal JwtUser currentUser) {
        Booking booking = bookingService.createBooking(
                currentUser.getUserId(),
                request.getRoomId(),
                request.getCheckIn(),
                request.getCheckOut(),
                request.getGuestCount(),
                request.getSpecialRequests(),
                BookingSource.WEBSITE
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(booking));
    }

    /**
     * Get booking by ID.
     * GET /api/bookings/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<BookingResponse> getBooking(@PathVariable Long id) {
        Booking booking = bookingService.getBooking(id);
        return ResponseEntity.ok(toResponse(booking));
    }

    /**
     * Get current user's bookings.
     * GET /api/bookings/me
     */
    @GetMapping("/me")
    public ResponseEntity<List<BookingSummary>> getMyBookings(
            @AuthenticationPrincipal JwtUser currentUser) {
        List<Booking> bookings = bookingService.getBookingsForUser(currentUser.getUserId());
        List<BookingSummary> summaries = bookings.stream()
                .map(this::toSummary)
                .collect(Collectors.toList());
        return ResponseEntity.ok(summaries);
    }

    /**
     * Cancel a booking.
     * PUT /api/bookings/{id}/cancel
     */
    @PutMapping("/{id}/cancel")
    public ResponseEntity<BookingResponse> cancelBooking(
            @PathVariable Long id,
            @AuthenticationPrincipal JwtUser currentUser,
            @RequestParam(required = false) String reason) {
        Booking booking = bookingService.cancelBooking(id, currentUser.getUserId(), reason);
        return ResponseEntity.ok(toResponse(booking));
    }

    /**
     * Confirm a booking.
     * PUT /api/bookings/{id}/confirm
     */
    @PutMapping("/{id}/confirm")
    public ResponseEntity<BookingResponse> confirmBooking(
            @PathVariable Long id,
            @AuthenticationPrincipal JwtUser currentUser) {
        Booking booking = bookingService.confirmBooking(id, currentUser.getUserId());
        return ResponseEntity.ok(toResponse(booking));
    }

    /**
     * Check in a booking.
     * PUT /api/bookings/{id}/checkin
     */
    @PutMapping("/{id}/checkin")
    public ResponseEntity<BookingResponse> checkIn(
            @PathVariable Long id,
            @AuthenticationPrincipal JwtUser currentUser) {
        Booking booking = bookingService.checkIn(id, currentUser.getUserId());
        return ResponseEntity.ok(toResponse(booking));
    }

    /**
     * Check out a booking.
     * PUT /api/bookings/{id}/checkout
     */
    @PutMapping("/{id}/checkout")
    public ResponseEntity<BookingResponse> checkOut(
            @PathVariable Long id,
            @AuthenticationPrincipal JwtUser currentUser) {
        Booking booking = bookingService.checkOut(id, currentUser.getUserId());
        return ResponseEntity.ok(toResponse(booking));
    }

    /**
     * Complete a booking.
     * PUT /api/bookings/{id}/complete
     */
    @PutMapping("/{id}/complete")
    public ResponseEntity<BookingResponse> completeBooking(
            @PathVariable Long id,
            @AuthenticationPrincipal JwtUser currentUser) {
        Booking booking = bookingService.completeBooking(id, currentUser.getUserId());
        return ResponseEntity.ok(toResponse(booking));
    }

    /**
     * Get booking timeline.
     * GET /api/bookings/{id}/timeline
     */
    @GetMapping("/{id}/timeline")
    public ResponseEntity<List<TimelineEntryResponse>> getTimeline(@PathVariable Long id) {
        List<BookingTimeline> timeline = bookingService.getTimeline(id);
        List<TimelineEntryResponse> entries = timeline.stream()
                .map(t -> TimelineEntryResponse.builder()
                        .id(t.getId())
                        .eventType(t.getEventType())
                        .performedBy(t.getPerformedBy())
                        .remarks(t.getRemarks())
                        .createdAt(t.getCreatedAt())
                        .build())
                .collect(Collectors.toList());
        return ResponseEntity.ok(entries);
    }

    /**
     * Get booking invoice.
     * GET /api/bookings/{id}/invoice
     */
    @GetMapping("/{id}/invoice")
    public ResponseEntity<Invoice> getInvoice(@PathVariable Long id) {
        Invoice invoice = bookingService.generateInvoice(id);
        return ResponseEntity.ok(invoice);
    }

    // ========================================================================
    // PRICE CALCULATION ENDPOINT
    // ========================================================================

    /**
     * Preview price for a booking (server-authoritative).
     * GET /api/bookings/price?roomId=X&checkIn=Y&checkOut=Z
     */
    @GetMapping("/price")
    public ResponseEntity<PriceBreakdown> calculatePrice(
            @RequestParam Long roomId,
            @RequestParam String checkIn,
            @RequestParam String checkOut) {
        PriceBreakdown price = bookingService.calculatePrice(
                roomId,
                java.time.LocalDate.parse(checkIn),
                java.time.LocalDate.parse(checkOut)
        );
        return ResponseEntity.ok(price);
    }

    // ========================================================================
    // DTO MAPPERS
    // ========================================================================

    private BookingResponse toResponse(Booking booking) {
        return BookingResponse.builder()
                .id(booking.getId())
                .publicId(booking.getPublicId())
                .bookingReference(booking.getBookingReference())
                .userId(booking.getUserId())
                .propertyId(booking.getPropertyId())
                .roomId(booking.getRoomId())
                .bookingType(booking.getBookingType())
                .checkIn(booking.getCheckIn())
                .checkOut(booking.getCheckOut())
                .guestCount(booking.getGuestCount())
                .bookingStatus(booking.getBookingStatus())
                .paymentStatus(booking.getPaymentStatus())
                .bookingSource(booking.getBookingSource())
                .totalAmount(booking.getTotalAmount())
                .basePriceSnapshot(booking.getBasePriceSnapshot())
                .cleaningFeeSnapshot(booking.getCleaningFeeSnapshot())
                .taxSnapshot(booking.getTaxSnapshot())
                .discountSnapshot(booking.getDiscountSnapshot())
                .specialRequests(booking.getSpecialRequests())
                .createdAt(booking.getCreatedAt())
                .updatedAt(booking.getUpdatedAt())
                .build();
    }

    private BookingSummary toSummary(Booking booking) {
        return BookingSummary.builder()
                .publicId(booking.getPublicId())
                .bookingReference(booking.getBookingReference())
                .propertyId(booking.getPropertyId())
                .checkIn(booking.getCheckIn())
                .checkOut(booking.getCheckOut())
                .guestCount(booking.getGuestCount())
                .bookingStatus(booking.getBookingStatus())
                .paymentStatus(booking.getPaymentStatus())
                .totalAmount(booking.getTotalAmount())
                .createdAt(booking.getCreatedAt())
                .build();
    }
}
