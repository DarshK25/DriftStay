package com.driftstay.booking.repository;

import com.driftstay.booking.entity.BookingTimeline;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookingTimelineRepository extends JpaRepository<BookingTimeline, Long> {

    List<BookingTimeline> findByBookingIdOrderByCreatedAtAsc(Long bookingId);
}
