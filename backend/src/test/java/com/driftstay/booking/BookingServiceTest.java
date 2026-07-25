package com.driftstay.booking;

import com.driftstay.booking.dto.request.PriceCalculationRequest;
import com.driftstay.booking.dto.response.PriceBreakdown;
import com.driftstay.booking.entity.Booking;
import com.driftstay.booking.repository.BookingRepository;
import com.driftstay.booking.service.AvailabilityService;
import com.driftstay.booking.service.BookingLifecycleService;
import com.driftstay.booking.service.BookingService;
import com.driftstay.booking.service.PricingService;
import com.driftstay.booking.service.cancellation.CancellationPolicyResolver;
import com.driftstay.booking.validator.BookingValidator;
import com.driftstay.common.enums.BookingStatus;
import com.driftstay.common.enums.BookingType;
import com.driftstay.common.enums.PaymentStatus;
import com.driftstay.exception.BookingConflictException;
import com.driftstay.room.entity.Room;
import com.driftstay.room.repository.RoomRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock private BookingRepository bookingRepository;
    @Mock private RoomRepository roomRepository;
    @Mock private BookingValidator bookingValidator;
    @Mock private AvailabilityService availabilityService;
    @Mock private PricingService pricingService;
    @Mock private BookingLifecycleService lifecycleService;
    @Mock private CancellationPolicyResolver cancellationPolicyResolver;

    @InjectMocks private BookingService bookingService;

    private Room room;
    private Booking booking;
    private PriceBreakdown priceBreakdown;

    @BeforeEach
    void setUp() {
        room = new Room();
        room.setId(1L);
        room.setRoomName("Test Room");
        room.setBasePrice(new BigDecimal("5000"));
        room.setCapacity(4);

        booking = new Booking();
        booking.setId(1L);
        booking.setBookingReference("DRF-2026-00001");
        booking.setUserId(1L);
        booking.setRoomId(1L);
        booking.setBookingStatus(BookingStatus.PENDING);
        booking.setPaymentStatus(PaymentStatus.UNPAID);
        booking.setTotalAmount(new BigDecimal("5000"));

        priceBreakdown = PriceBreakdown.builder()
                .basePrice(new BigDecimal("5000"))
                .cleaningFee(new BigDecimal("500"))
                .gst(new BigDecimal("600"))
                .discount(BigDecimal.ZERO)
                .totalAmount(new BigDecimal("6100"))
                .build();
    }

    @Test
    void shouldCreateBookingSuccessfully() {
        when(roomRepository.findById(1L)).thenReturn(Optional.of(room));
        when(pricingService.calculatePrice(any(PriceCalculationRequest.class)))
                .thenReturn(priceBreakdown);
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);

        Booking result = bookingService.createBooking(
                1L, 1L, LocalDate.now().plusDays(5),
                LocalDate.now().plusDays(7), 2, null,
                com.driftstay.common.enums.BookingSource.WEBSITE);

        assertThat(result).isNotNull();
        assertThat(result.getBookingReference()).isEqualTo("DRF-2026-00001");
        verify(bookingValidator).validateBookingRequest(any(), any(), any(), any(), any());
        verify(availabilityService).validateAndLockAvailability(any(), any(), any());
        verify(bookingRepository).save(any(Booking.class));
    }

    @Test
    void shouldThrowWhenRoomNotFound() {
        when(roomRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.createBooking(
                1L, 999L, LocalDate.now().plusDays(1),
                LocalDate.now().plusDays(3), 2, null,
                com.driftstay.common.enums.BookingSource.WEBSITE))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Room not found");
    }

    @Test
    void shouldCancelPendingBooking() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(lifecycleService.transitionStatus(eq(1L), eq(BookingStatus.CANCELLED), any(), any()))
                .thenReturn(booking);

        bookingService.cancelBooking(1L, 1L, "Changed plans");

        verify(lifecycleService).transitionStatus(1L, BookingStatus.CANCELLED, "1", "Changed plans");
    }

    @Test
    void shouldConfirmBooking() {
        booking.setPaymentStatus(PaymentStatus.PAID);
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(lifecycleService.transitionStatus(eq(1L), eq(BookingStatus.CONFIRMED), any(), any()))
                .thenReturn(booking);

        bookingService.confirmBooking(1L, 1L);

        verify(lifecycleService).transitionStatus(1L, BookingStatus.CONFIRMED, "1", "Booking confirmed");
    }
}
