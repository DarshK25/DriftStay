package com.driftstay.booking.service;

import com.driftstay.common.util.UlidGenerator;
import com.driftstay.booking.dto.request.PriceCalculationRequest;
import com.driftstay.booking.dto.response.PriceBreakdown;
import com.driftstay.booking.entity.Booking;
import com.driftstay.booking.entity.BookingGuest;
import com.driftstay.booking.entity.BookingTimeline;
import com.driftstay.booking.entity.Invoice;
import com.driftstay.booking.event.BookingEvent;
import com.driftstay.booking.event.BookingEventPublisher;
import com.driftstay.booking.repository.BookingRepository;
import com.driftstay.booking.repository.BookingTimelineRepository;
import com.driftstay.booking.service.cancellation.CancellationPolicyResolver;
import com.driftstay.booking.validator.BookingValidator;
import com.driftstay.common.enums.BookingSource;
import com.driftstay.common.enums.BookingStatus;
import com.driftstay.common.enums.BookingType;
import com.driftstay.common.enums.InvoiceStatus;
import com.driftstay.common.enums.PaymentStatus;
import com.driftstay.room.entity.Room;
import com.driftstay.room.repository.RoomRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Core booking service orchestrating the complete booking lifecycle:
 *
 * PENDING → CONFIRMED → CHECKED_IN → CHECKED_OUT → COMPLETED
 * PENDING → CANCELLED → REFUNDED
 * PENDING → FAILED
 *
 * Every state transition:
 * 1. Validates the transition is legal
 * 2. Performs business logic (refund calc, payment update)
 * 3. Publishes a BookingEvent for timeline/notifications
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final RoomRepository roomRepository;
    private final BookingValidator bookingValidator;
    private final AvailabilityService availabilityService;
    private final PricingService pricingService;
    private final BookingEventPublisher eventPublisher;
    private final BookingLifecycleService lifecycleService;
    private final CancellationPolicyResolver cancellationPolicyResolver;
    private final BookingTimelineRepository bookingTimelineRepository;
    private final EntityManager entityManager;

    // ULID-based generation — collision-safe across server restarts

    // ========================================================================
    // BOOKING CREATION
    // ========================================================================

    /**
     * Create a new booking with full validation and server-side pricing.
     *
     * Flow: VALIDATE → LOCK → PRICE → INSERT → PUBLISH
     */
    /**
     * Create a new booking with full validation and server-side pricing.
     *
     * Flow inside ONE transaction:
     * 1. LOCK room (PESSIMISTIC_WRITE) — blocks concurrent bookings on same room
     * 2. VALIDATE availability, capacity, dates
     * 3. PRICE (server-side only)
     * 4. INSERT booking
     * 5. PUBLISH event (timeline listener fires AFTER_COMMIT)
     *
     * CRITICAL: Lock BEFORE validate, all in one @Transactional.
     * Never: validate() then save() in separate transactions.
     */
    @Transactional
    public Booking createBooking(Long userId, Long roomId, LocalDate checkIn,
                                 LocalDate checkOut, Integer guestCount,
                                 String specialRequests, BookingSource source) {
        // Step 1: LOCK the room with PESSIMISTIC_WRITE — blocks concurrent bookings
        // This is the primary race condition defense.
        Room room = roomRepository.findRoomWithLock(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Room not found: " + roomId));

        // 2. Validate everything (includes pessimistic lock on availability)
        bookingValidator.validateBookingRequest(roomId, checkIn, checkOut, guestCount, room.getCapacity());

        // 3. Calculate price server-side (NEVER trust frontend totals!)
        PriceBreakdown price = calculatePrice(room, checkIn, checkOut);

        // 4. Create booking entity
        Booking booking = new Booking();
        booking.setPublicId(UlidGenerator.generateShort());
        booking.setBookingReference(generateBookingReference());
        booking.setUserId(userId);
        booking.setPropertyId(room.getProperty().getId());
        booking.setRoomId(roomId);
        booking.setBookingType(BookingType.ROOM);
        booking.setCheckIn(checkIn);
        booking.setCheckOut(checkOut);
        booking.setGuestCount(guestCount);
        booking.setBookingStatus(BookingStatus.PENDING);
        booking.setPaymentStatus(PaymentStatus.UNPAID);
        booking.setBookingSource(source != null ? source : BookingSource.WEBSITE);
        booking.setSpecialRequests(specialRequests);
        booking.setBasePriceSnapshot(price.getBasePrice());
        booking.setCleaningFeeSnapshot(price.getCleaningFee());
        booking.setTaxSnapshot(price.getGst());
        booking.setDiscountSnapshot(price.getDiscount());
        booking.setTotalAmount(price.getTotalAmount());

        booking = bookingRepository.save(booking);

        // 4. Publish event for timeline
        publishEvent(booking, BookingLifecycleService.EVENT_CREATED, null, userId, "Booking created");

        log.info("Booking created: ref={}, room={}, from={} to={}, total={}",
                booking.getBookingReference(), roomId, checkIn, checkOut, price.getTotalAmount());

        return booking;
    }

    // ========================================================================
    // LIFECYCLE TRANSITIONS
    // ========================================================================

    /** Confirm a booking: PENDING → CONFIRMED */
    @Transactional
    public Booking confirmBooking(Long bookingId, Long userId) {
        Booking booking = findBookingOrThrow(bookingId);

        if (booking.getPaymentStatus() == PaymentStatus.UNPAID) {
            throw new IllegalArgumentException("Cannot confirm booking without payment");
        }

        return lifecycleService.transitionStatus(
                bookingId, BookingStatus.CONFIRMED,
                String.valueOf(userId), "Booking confirmed");
    }

    /** Cancel a booking: PENDING/CONFIRMED → CANCELLED */
    @Transactional
    public Booking cancelBooking(Long bookingId, Long userId, String reason) {
        Booking booking = findBookingOrThrow(bookingId);
        bookingValidator.validateCancellable(booking);

        // Calculate refund if payment was made
        if (booking.getPaymentStatus() == PaymentStatus.PAID) {
            BigDecimal refundAmount = cancellationPolicyResolver.calculateRefund(booking, LocalDateTime.now());
            if (refundAmount.compareTo(BigDecimal.ZERO) > 0) {
                booking.setPaymentStatus(PaymentStatus.REFUNDED);
                bookingRepository.save(booking);
            }
        }

        return lifecycleService.transitionStatus(
                bookingId, BookingStatus.CANCELLED,
                String.valueOf(userId),
                reason != null ? reason : "Booking cancelled");
    }

    /** Check in: CONFIRMED → CHECKED_IN */
    @Transactional
    public Booking checkIn(Long bookingId, Long userId) {
        Booking booking = findBookingOrThrow(bookingId);
        bookingValidator.validateCheckInReady(booking);

        return lifecycleService.transitionStatus(
                bookingId, BookingStatus.CHECKED_IN,
                String.valueOf(userId), "Guest checked in");
    }

    /** Check out: CHECKED_IN → CHECKED_OUT */
    @Transactional
    public Booking checkOut(Long bookingId, Long userId) {
        Booking booking = findBookingOrThrow(bookingId);

        if (booking.getBookingStatus() != BookingStatus.CHECKED_IN) {
            throw new com.driftstay.exception.InvalidBookingStateException(
                    booking.getBookingStatus(), BookingStatus.CHECKED_OUT,
                    "Only CHECKED_IN bookings can be checked out");
        }

        return lifecycleService.transitionStatus(
                bookingId, BookingStatus.CHECKED_OUT,
                String.valueOf(userId), "Guest checked out");
    }

    /** Complete: CHECKED_OUT → COMPLETED */
    @Transactional
    public Booking completeBooking(Long bookingId, Long userId) {
        Booking booking = findBookingOrThrow(bookingId);

        if (booking.getBookingStatus() != BookingStatus.CHECKED_OUT) {
            throw new com.driftstay.exception.InvalidBookingStateException(
                    booking.getBookingStatus(), BookingStatus.COMPLETED,
                    "Only CHECKED_OUT bookings can be completed");
        }

        return lifecycleService.transitionStatus(
                bookingId, BookingStatus.COMPLETED,
                String.valueOf(userId), "Booking completed");
    }

    // ========================================================================
    // QUERIES
    // ========================================================================

    public Booking getBooking(Long bookingId) {
        return findBookingOrThrow(bookingId);
    }

    public Booking getBookingByPublicId(String publicId) {
        return bookingRepository.findByPublicId(publicId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found: " + publicId));
    }

    public List<Booking> getBookingsForUser(Long userId) {
        return bookingRepository.findByUser(userId);
    }

    public List<Booking> getBookingsForProperty(Long propertyId) {
        return bookingRepository.findByPropertyIdOrderByCreatedAtDesc(propertyId);
    }

    public List<Booking> getBookingsByStatus(BookingStatus status) {
        return bookingRepository.findByBookingStatus(status);
    }

    public List<Booking> getUpcomingBookings(Long userId) {
        return bookingRepository.findUpcomingBookings(userId, LocalDate.now(),
                Set.of(BookingStatus.CANCELLED, BookingStatus.COMPLETED,
                       BookingStatus.REFUNDED, BookingStatus.NO_SHOW));
    }

    public List<Booking> getActiveBookings(Long userId) {
        return bookingRepository.findActiveBookings(userId,
                Set.of(BookingStatus.PENDING, BookingStatus.CONFIRMED, BookingStatus.CHECKED_IN));
    }

    // ========================================================================
    // BOOKING TIMELINE
    // ========================================================================

    public List<BookingTimeline> getTimeline(Long bookingId) {
        Booking booking = findBookingOrThrow(bookingId);
        return bookingTimelineRepository.findByBookingIdOrderByCreatedAtAsc(booking.getId());
    }

    // ========================================================================
    // INVOICE
    // ========================================================================

    @Transactional
    public Invoice generateInvoice(Long bookingId) {
        Booking booking = findBookingOrThrow(bookingId);

        Invoice invoice = new Invoice();
        invoice.setBooking(booking);
        invoice.setInvoiceNumber(generateInvoiceNumber(booking));
        invoice.setSubtotal(booking.getTotalAmount());
        invoice.setTax(booking.getTaxSnapshot());
        invoice.setDiscount(booking.getDiscountSnapshot());
        invoice.setGrandTotal(booking.getTotalAmount());
        invoice.setStatus(InvoiceStatus.PAID);
        invoice.setIssuedAt(LocalDateTime.now());

        entityManager.persist(invoice);
        log.info("Invoice generated: {} for booking {}", invoice.getInvoiceNumber(), booking.getBookingReference());

        return invoice;
    }

    // ========================================================================
    // PRICE VALIDATION
    // ========================================================================

    /**
     * Public method to calculate price (used by controllers for price preview).
     * Always calculated server-side — frontend values are never trusted.
     */
    public PriceBreakdown calculatePrice(Long roomId, LocalDate checkIn, LocalDate checkOut) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Room not found: " + roomId));

        return calculatePrice(room, checkIn, checkOut);
    }

    // ========================================================================
    // PRIVATE HELPERS
    // ========================================================================

    private PriceBreakdown calculatePrice(Room room, LocalDate checkIn, LocalDate checkOut) {
        PriceCalculationRequest request = PriceCalculationRequest.builder()
                .roomId(room.getId())
                .checkIn(checkIn)
                .checkOut(checkOut)
                .guestCount(1)
                .basePrice(room.getBasePrice())
                .weekendPrice(room.getWeekendPrice())
                .cleaningFee(room.getCleaningFee())
                .build();

        return pricingService.calculatePrice(request);
    }

    private void validateTransition(BookingStatus current, BookingStatus target, String ref) {
        lifecycleService.validateTransition(current, target, ref);
    }

    private void publishEvent(Booking booking, String eventType, BookingStatus previousStatus,
                              Long performedBy, String remarks) {
        BookingEvent event = BookingEvent.builder()
                .eventType(eventType)
                .bookingId(booking.getId())
                .bookingReference(booking.getBookingReference())
                .previousStatus(previousStatus)
                .newStatus(booking.getBookingStatus())
                .performedBy(performedBy)
                .remarks(remarks)
                .timestamp(LocalDateTime.now())
                .build();
        eventPublisher.publishBookingEvent(event);
    }

    private Booking findBookingOrThrow(Long bookingId) {
        return bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found: " + bookingId));
    }

    private String generateBookingReference() {
        return "DRF-" + UlidGenerator.generate();
    }

    private String generateInvoiceNumber(Booking booking) {
        return "INV-" + booking.getBookingReference() + "-"
                + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
    }
}
