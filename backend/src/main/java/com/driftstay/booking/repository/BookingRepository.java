package com.driftstay.booking.repository;

import com.driftstay.booking.entity.Booking;
import com.driftstay.common.enums.BookingStatus;
import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    // ========================================================================
    // CONFLICT DETECTION (Double Booking Prevention)
    // ========================================================================

    /** Standard overlap check for non-terminal bookings */
    @Query("""
            SELECT b FROM Booking b
            WHERE b.roomId = :roomId
              AND b.bookingStatus IN :activeStatuses
              AND b.checkIn < :checkOut
              AND b.checkOut > :checkIn
            """)
    List<Booking> findConflictingBookings(
            @Param("roomId") Long roomId,
            @Param("checkIn") LocalDate checkIn,
            @Param("checkOut") LocalDate checkOut,
            @Param("activeStatuses") Set<BookingStatus> activeStatuses
    );

    /** Overlap check with PESSIMISTIC_WRITE lock — the primary race condition defense */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints(@QueryHint(name = "jakarta.persistence.lock.timeout", value = "5000"))
    @Query("""
            SELECT b FROM Booking b
            WHERE b.roomId = :roomId
              AND b.bookingStatus IN :activeStatuses
              AND b.checkIn < :checkOut
              AND b.checkOut > :checkIn
            """)
    List<Booking> findConflictingBookingsWithLock(
            @Param("roomId") Long roomId,
            @Param("checkIn") LocalDate checkIn,
            @Param("checkOut") LocalDate checkOut,
            @Param("activeStatuses") Set<BookingStatus> activeStatuses
    );

    /** Count conflicts (lightweight check, no entity loading) */
    @Query("""
            SELECT COUNT(b) FROM Booking b
            WHERE b.roomId = :roomId
              AND b.bookingStatus IN :activeStatuses
              AND b.checkIn < :checkOut
              AND b.checkOut > :checkIn
            """)
    long countConflictingBookings(
            @Param("roomId") Long roomId,
            @Param("checkIn") LocalDate checkIn,
            @Param("checkOut") LocalDate checkOut,
            @Param("activeStatuses") Set<BookingStatus> activeStatuses
    );

    /** Boolean exists check — most efficient for simple availability checks */
    @Query("""
            SELECT CASE WHEN COUNT(b) > 0 THEN true ELSE false END FROM Booking b
            WHERE b.roomId = :roomId
              AND b.bookingStatus IN :activeStatuses
              AND b.checkIn < :checkOut
              AND b.checkOut > :checkIn
            """)
    boolean existsOverlap(
            @Param("roomId") Long roomId,
            @Param("checkIn") LocalDate checkIn,
            @Param("checkOut") LocalDate checkOut,
            @Param("activeStatuses") Set<BookingStatus> activeStatuses
    );

    // ========================================================================
    // STANDARD LOOKUPS
    // ========================================================================

    @EntityGraph(attributePaths = {})
    Optional<Booking> findByPublicId(String publicId);

    @EntityGraph(attributePaths = {})
    Optional<Booking> findByBookingReference(String bookingReference);

    // ========================================================================
    // USER BOOKINGS
    // ========================================================================

    @Query("SELECT b FROM Booking b WHERE b.userId = :userId ORDER BY b.createdAt DESC")
    List<Booking> findByUser(@Param("userId") Long userId);

    Page<Booking> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    /** Find upcoming bookings (check-in in the future) for a user */
    @Query("""
            SELECT b FROM Booking b
            WHERE b.userId = :userId
              AND b.checkIn >= :today
              AND b.bookingStatus NOT IN :terminalStatuses
            ORDER BY b.checkIn ASC
            """)
    List<Booking> findUpcomingBookings(
            @Param("userId") Long userId,
            @Param("today") LocalDate today,
            @Param("terminalStatuses") Set<BookingStatus> terminalStatuses
    );

    /** Find active bookings (currently checked in or confirmed) for a user */
    @Query("""
            SELECT b FROM Booking b
            WHERE b.userId = :userId
              AND b.bookingStatus IN :activeStatuses
            ORDER BY b.checkIn ASC
            """)
    List<Booking> findActiveBookings(
            @Param("userId") Long userId,
            @Param("activeStatuses") Set<BookingStatus> activeStatuses
    );

    // ========================================================================
    // PROPERTY & HOST BOOKINGS
    // ========================================================================

    List<Booking> findByPropertyIdOrderByCreatedAtDesc(Long propertyId);

    Page<Booking> findByPropertyId(Long propertyId, Pageable pageable);

    List<Booking> findByRoomIdOrderByCreatedAtDesc(Long roomId);

    /** Find bookings for a specific property */
    List<Booking> findByPropertyId(Long propertyId);

    @Query("""
            SELECT b FROM Booking b
            WHERE b.propertyId IN :propertyIds
            ORDER BY b.createdAt DESC
            """)
    List<Booking> findByProperties(@Param("propertyIds") List<Long> propertyIds);

    // ========================================================================
    // STATUS-BASED
    // ========================================================================

    List<Booking> findByBookingStatus(BookingStatus status);

    long countByBookingStatus(BookingStatus status);

    // ========================================================================
    // DATE-BASED OPERATIONS
    // ========================================================================

    @Query("""
            SELECT b FROM Booking b
            WHERE b.roomId = :roomId
              AND b.checkIn < :endDate
              AND b.checkOut > :startDate
            ORDER BY b.checkIn ASC
            """)
    List<Booking> findBookingsInRange(
            @Param("roomId") Long roomId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    @Query("""
            SELECT b FROM Booking b
            WHERE b.bookingStatus = 'PENDING'
              AND b.checkIn < :today
            """)
    List<Booking> findExpiredPendingBookings(@Param("today") LocalDate today);

    @Query("""
            SELECT b FROM Booking b
            WHERE b.bookingStatus IN ('CHECKED_IN', 'CONFIRMED')
              AND b.checkOut <= :today
            """)
    List<Booking> findBookingsDueForCheckout(@Param("today") LocalDate today);

    // ========================================================================
    // ANALYTICS / REVENUE
    // ========================================================================

    @Query("""
            SELECT COALESCE(SUM(b.totalAmount), 0) FROM Booking b
            WHERE b.bookingStatus IN :completedStatuses
              AND b.createdAt BETWEEN :startDate AND :endDate
            """)
    double findRevenue(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("completedStatuses") Set<BookingStatus> completedStatuses
    );

    @Query("""
            SELECT COALESCE(SUM(b.totalAmount), 0) FROM Booking b
            WHERE b.propertyId = :propertyId
              AND b.bookingStatus = 'COMPLETED'
            """)
    double findRevenueByProperty(@Param("propertyId") Long propertyId);

    @Query("""
            SELECT COUNT(b) FROM Booking b
            WHERE b.roomId = :roomId
              AND b.bookingStatus IN :activeStatuses
              AND b.checkIn <= :date
              AND b.checkOut > :date
            """)
    long findOccupancy(
            @Param("roomId") Long roomId,
            @Param("date") LocalDate date,
            @Param("activeStatuses") Set<BookingStatus> activeStatuses
    );

    @Query("""
            SELECT COUNT(b) FROM Booking b
            WHERE b.propertyId = :propertyId
              AND b.bookingStatus IN :activeStatuses
              AND b.checkIn < :endDate
              AND b.checkOut > :startDate
            """)
    long countActiveBookingsInRange(
            @Param("propertyId") Long propertyId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("activeStatuses") Set<BookingStatus> activeStatuses
    );
}
