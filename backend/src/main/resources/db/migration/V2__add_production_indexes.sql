-- ============================================================================
-- DriftStay Database Schema V2
-- Flyway Migration: V2__add_production_indexes.sql
-- Description: Adds critical composite indexes for search, booking conflicts,
--              and common query patterns identified via EXPLAIN ANALYZE.
--
-- Why: Single-column indexes are not enough.
--      Booking conflict query needs (room_id, status, check_in, check_out).
--      Search needs (city, status, price, rating) composite.
-- ============================================================================

-- ============================================================================
-- BOOKING INDEXES — Critical for conflict detection
-- ============================================================================

-- Primary conflict detection index: room + status + date range
CREATE INDEX IF NOT EXISTS idx_booking_conflict_detection
    ON booking(room_id, booking_status, check_in, check_out);

-- User booking history (most common query: "my bookings")
CREATE INDEX IF NOT EXISTS idx_booking_user_status_created
    ON booking(user_id, booking_status, created_at DESC);

-- Property bookings (host dashboard queries)
CREATE INDEX IF NOT EXISTS idx_booking_property_status_dates
    ON booking(property_id, booking_status, check_in, check_out);

-- ============================================================================
-- PROPERTY INDEXES — Critical for search performance
-- ============================================================================

-- Property search: city + status + rating (most common search pattern)
CREATE INDEX IF NOT EXISTS idx_property_search_rating
    ON property(city, status, average_rating DESC);

-- Featured properties query
CREATE INDEX IF NOT EXISTS idx_property_featured
    ON property(status, average_rating DESC, booking_count DESC);

-- ============================================================================
-- ROOM INDEXES
-- ============================================================================

-- Room price search (sorting by price)
CREATE INDEX IF NOT EXISTS idx_room_price_search
    ON room(property_id, status, base_price);

-- ============================================================================
-- REVIEW INDEXES
-- ============================================================================

-- Property reviews (sorted by recency)
CREATE INDEX IF NOT EXISTS idx_review_property_created
    ON review(property_id, status, created_at DESC);

-- User reviews
CREATE INDEX IF NOT EXISTS idx_review_user_created
    ON review(user_id, status, created_at DESC);

-- ============================================================================
-- PAYMENT INDEXES
-- ============================================================================

-- Payment lookup by provider for webhook processing
CREATE INDEX IF NOT EXISTS idx_payment_provider_lookup
    ON payment(provider, provider_payment_id);

-- Payment booking lookup
CREATE INDEX IF NOT EXISTS idx_payment_booking_status
    ON payment(booking_id, status);

-- ============================================================================
-- NOTIFICATION INDEXES
-- ============================================================================

-- Batch notification sending
CREATE INDEX IF NOT EXISTS idx_notification_send_queue
    ON notification(status, scheduled_at, channel)
    WHERE status = 'PENDING';
