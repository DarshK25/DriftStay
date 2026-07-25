package com.driftstay.config.observability;

import com.driftstay.booking.entity.Booking;
import com.driftstay.booking.repository.BookingRepository;
import com.driftstay.common.enums.BookingStatus;
import com.driftstay.common.enums.PaymentStatus;
import com.driftstay.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;

/**
 * Simple metrics tracking service for business KPIs.
 * In production, replace with Micrometer + Prometheus + Grafana.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MetricsService {

    private final BookingRepository bookingRepository;

    private final LongAdder totalBookings = new LongAdder();
    private final LongAdder successfulPayments = new LongAdder();
    private final LongAdder failedPayments = new LongAdder();
    private final LongAdder searchCount = new LongAdder();
    private final AtomicLong cacheHits = new AtomicLong(0);
    private final AtomicLong cacheMisses = new AtomicLong(0);

    // ========================================================================
    // METRIC RECORDING
    // ========================================================================

    public void recordBookingCreated() {
        totalBookings.increment();
    }

    public void recordSuccessfulPayment() {
        successfulPayments.increment();
    }

    public void recordFailedPayment() {
        failedPayments.increment();
    }

    public void recordSearch() {
        searchCount.increment();
    }

    public void recordCacheHit() {
        cacheHits.incrementAndGet();
    }

    public void recordCacheMiss() {
        cacheMisses.incrementAndGet();
    }

    // ========================================================================
    // METRIC QUERIES
    // ========================================================================

    public MetricsSnapshot getSnapshot() {
        LocalDate today = LocalDate.now();
        Set<BookingStatus> completedStatuses = Set.of(BookingStatus.COMPLETED, BookingStatus.CHECKED_OUT);

        return MetricsSnapshot.builder()
                .totalBookingsToday(bookingRepository.countByBookingStatus(BookingStatus.PENDING)
                        + bookingRepository.countByBookingStatus(BookingStatus.CONFIRMED))
                .revenueToday(BigDecimal.valueOf(bookingRepository.findRevenue(
                        today, today.plusDays(1), completedStatuses)))
                .failedPayments(failedPayments.sum())
                .successfulPayments(successfulPayments.sum())
                .searchCount(searchCount.sum())
                .cacheHits(cacheHits.get())
                .cacheMisses(cacheMisses.get())
                .cacheHitRate(calculateCacheHitRate())
                .build();
    }

    private double calculateCacheHitRate() {
        long hits = cacheHits.get();
        long misses = cacheMisses.get();
        long total = hits + misses;
        if (total == 0) return 0.0;
        return (double) hits / total * 100;
    }

    @lombok.Builder
    @lombok.Getter
    public static class MetricsSnapshot {
        private final long totalBookingsToday;
        private final BigDecimal revenueToday;
        private final long successfulPayments;
        private final long failedPayments;
        private final long searchCount;
        private final long cacheHits;
        private final long cacheMisses;
        private final double cacheHitRate;
    }
}
