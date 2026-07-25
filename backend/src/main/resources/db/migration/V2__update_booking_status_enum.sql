-- ============================================================================
-- DriftStay Database Schema V2
-- Flyway Migration: V2__update_booking_status_enum.sql
-- Description: Updates Booking Status CHECK constraint to include new states
--              (COMPLETED, FAILED, REFUNDED) and Payment Status (REFUNDING)
-- ============================================================================

-- Drop the old CHECK constraint on booking
ALTER TABLE booking DROP CONSTRAINT ck_booking_status;

-- Add updated CHECK constraint with new statuses
ALTER TABLE booking ADD CONSTRAINT ck_booking_status
    CHECK (booking_status IN (
        'PENDING',
        'CONFIRMED',
        'CHECKED_IN',
        'CHECKED_OUT',
        'COMPLETED',
        'CANCELLED',
        'NO_SHOW',
        'FAILED',
        'REFUNDED'
    ));
