-- ============================================================================
-- DriftStay Database Schema v1.0
-- Flyway Migration: V1__initial_schema.sql
-- Description: Creates all core tables, enums, indexes, and relationships
-- ============================================================================

-- ============================================================================
-- ENUMS
-- ============================================================================

CREATE TYPE property_status AS ENUM ('ACTIVE', 'INACTIVE', 'ARCHIVED', 'DELETED');
CREATE TYPE room_status AS ENUM ('ACTIVE', 'INACTIVE', 'MAINTENANCE', 'DELETED');
CREATE TYPE booking_status AS ENUM ('PENDING', 'CONFIRMED', 'CHECKED_IN', 'CHECKED_OUT', 'CANCELLED', 'NO_SHOW');
CREATE TYPE booking_type AS ENUM ('PROPERTY', 'ROOM');
CREATE TYPE payment_status AS ENUM ('UNPAID', 'PARTIALLY_PAID', 'PAID', 'REFUNDED', 'PARTIALLY_REFUNDED');
CREATE TYPE booking_source AS ENUM ('WEBSITE', 'MOBILE_APP', 'ADMIN', 'AGENCY');
CREATE TYPE notification_channel AS ENUM ('EMAIL', 'SMS', 'PUSH', 'IN_APP');

-- ============================================================================
-- PROPERTY AGGREGATE
-- ============================================================================

-- ----------------------------------------------------------------------------
-- Table: property
-- Root entity for the Property aggregate. Represents an accommodation listing.
-- ----------------------------------------------------------------------------
CREATE TABLE property (
    property_id     BIGSERIAL       PRIMARY KEY,
    slug            VARCHAR(255)    NOT NULL UNIQUE,
    name            VARCHAR(255)    NOT NULL,
    short_description VARCHAR(500),
    description     TEXT,
    property_type   VARCHAR(50)     NOT NULL,
    star_category   INT             CHECK (star_category BETWEEN 1 AND 5),
    status          property_status NOT NULL DEFAULT 'ACTIVE',
    address_line_1  VARCHAR(255)    NOT NULL,
    address_line_2  VARCHAR(255),
    city            VARCHAR(100)    NOT NULL,
    state           VARCHAR(100)    NOT NULL,
    country         VARCHAR(100)    NOT NULL,
    postal_code     VARCHAR(20),
    latitude        DECIMAL(10, 7),
    longitude       DECIMAL(10, 7),
    average_rating  DECIMAL(2, 1)   DEFAULT 0.0,
    review_count    INT             DEFAULT 0,
    booking_count   INT             DEFAULT 0,
    created_at      TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP       NOT NULL DEFAULT NOW()
);

-- Indexes for property search
CREATE INDEX idx_property_city ON property(city);
CREATE INDEX idx_property_status ON property(status);
CREATE INDEX idx_property_status_city ON property(status, city);
CREATE INDEX idx_property_coordinates ON property(latitude, longitude);

-- ----------------------------------------------------------------------------
-- Table: property_image
-- Gallery images associated with a property.
-- ----------------------------------------------------------------------------
CREATE TABLE property_image (
    image_id        BIGSERIAL       PRIMARY KEY,
    property_id     BIGINT          NOT NULL REFERENCES property(property_id),
    image_url       VARCHAR(500)    NOT NULL,
    caption         VARCHAR(255),
    display_order   INT             NOT NULL,
    is_thumbnail    BOOLEAN         DEFAULT FALSE,
    created_at      TIMESTAMP       NOT NULL DEFAULT NOW(),
    UNIQUE (property_id, display_order)
);

CREATE INDEX idx_property_image_property_id ON property_image(property_id);

-- ----------------------------------------------------------------------------
-- Table: amenity
-- Lookup table of available amenities.
-- ----------------------------------------------------------------------------
CREATE TABLE amenity (
    amenity_id      BIGSERIAL       PRIMARY KEY,
    name            VARCHAR(100)    NOT NULL UNIQUE,
    icon            VARCHAR(255),
    category        VARCHAR(50),
    created_at      TIMESTAMP       NOT NULL DEFAULT NOW()
);

-- ----------------------------------------------------------------------------
-- Table: property_amenity
-- Bridge table for property-amenity many-to-many relationship.
-- ----------------------------------------------------------------------------
CREATE TABLE property_amenity (
    property_id     BIGINT          NOT NULL REFERENCES property(property_id),
    amenity_id      BIGINT          NOT NULL REFERENCES amenity(amenity_id),
    PRIMARY KEY (property_id, amenity_id)
);

CREATE INDEX idx_property_amenity_amenity_id ON property_amenity(amenity_id);

-- ----------------------------------------------------------------------------
-- Table: property_policy
-- One-to-one relationship storing stay policies for a property.
-- ----------------------------------------------------------------------------
CREATE TABLE property_policy (
    policy_id               BIGSERIAL       PRIMARY KEY,
    property_id             BIGINT          NOT NULL UNIQUE REFERENCES property(property_id),
    check_in_time           TIME            NOT NULL,
    check_out_time          TIME            NOT NULL,
    pets_allowed            BOOLEAN         DEFAULT FALSE,
    smoking_allowed         BOOLEAN         DEFAULT FALSE,
    minimum_age             INT             DEFAULT 18,
    free_cancellation_hours INT             DEFAULT 48,
    extra_bed_available     BOOLEAN         DEFAULT FALSE,
    created_at              TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMP       NOT NULL DEFAULT NOW()
);

-- ----------------------------------------------------------------------------
-- Table: property_contact
-- Contact information for property management.
-- ----------------------------------------------------------------------------
CREATE TABLE property_contact (
    contact_id      BIGSERIAL       PRIMARY KEY,
    property_id     BIGINT          NOT NULL REFERENCES property(property_id),
    contact_name    VARCHAR(255)    NOT NULL,
    designation     VARCHAR(100),
    email           VARCHAR(255),
    phone           VARCHAR(20)     NOT NULL,
    is_primary      BOOLEAN         DEFAULT FALSE,
    created_at      TIMESTAMP       NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_property_contact_property_id ON property_contact(property_id);

-- ----------------------------------------------------------------------------
-- Table: room
-- Represents bookable rooms within a property.
-- ----------------------------------------------------------------------------
CREATE TABLE room (
    room_id         BIGSERIAL       PRIMARY KEY,
    property_id     BIGINT          NOT NULL REFERENCES property(property_id),
    room_number     VARCHAR(20),
    room_name       VARCHAR(255)    NOT NULL,
    description     TEXT,
    room_type       VARCHAR(50)     NOT NULL,
    capacity        INT             NOT NULL,
    bed_count       INT             NOT NULL,
    bed_type        VARCHAR(50)     NOT NULL,
    bathroom_count  INT             DEFAULT 1,
    base_price      DECIMAL(10, 2)  NOT NULL,
    weekend_price   DECIMAL(10, 2),
    cleaning_fee    DECIMAL(10, 2)  DEFAULT 0,
    extra_guest_fee DECIMAL(10, 2)  DEFAULT 0,
    area_sqft       INT,
    floor_number    INT,
    status          room_status     NOT NULL DEFAULT 'ACTIVE',
    created_at      TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP       NOT NULL DEFAULT NOW(),
    UNIQUE (property_id, room_number)
);

CREATE INDEX idx_room_property_id ON room(property_id);
CREATE INDEX idx_room_property_status ON room(property_id, status);

-- ----------------------------------------------------------------------------
-- Table: room_image
-- Images associated with a specific room.
-- ----------------------------------------------------------------------------
CREATE TABLE room_image (
    room_image_id   BIGSERIAL       PRIMARY KEY,
    room_id         BIGINT          NOT NULL REFERENCES room(room_id),
    image_url       VARCHAR(500)    NOT NULL,
    caption         VARCHAR(255),
    display_order   INT             NOT NULL,
    is_thumbnail    BOOLEAN         DEFAULT FALSE,
    UNIQUE (room_id, display_order)
);

CREATE INDEX idx_room_image_room_id ON room_image(room_id);

-- ----------------------------------------------------------------------------
-- Table: room_availability
-- Tracks room availability for date ranges (owner blocks, maintenance, etc.).
-- ----------------------------------------------------------------------------
CREATE TABLE room_availability (
    availability_id BIGSERIAL       PRIMARY KEY,
    room_id         BIGINT          NOT NULL REFERENCES room(room_id),
    start_date      DATE            NOT NULL,
    end_date        DATE            NOT NULL,
    status          VARCHAR(20)     NOT NULL, -- AVAILABLE, BLOCKED, MAINTENANCE, RESERVED
    reason          VARCHAR(255),
    created_by      VARCHAR(100)    NOT NULL,
    created_at      TIMESTAMP       NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_room_avail_room_dates ON room_availability(room_id, start_date, end_date);

-- ============================================================================
-- USER AGGREGATE
-- ============================================================================

-- ----------------------------------------------------------------------------
-- Table: "user"
-- Core user entity. Stores authentication credentials and profile information.
-- Note: "user" is quoted as it is a reserved word in PostgreSQL.
-- ----------------------------------------------------------------------------
CREATE TABLE "user" (
    user_id         BIGSERIAL       PRIMARY KEY,
    email           VARCHAR(255)    NOT NULL UNIQUE,
    password_hash   VARCHAR(255)    NOT NULL,
    first_name      VARCHAR(100)    NOT NULL,
    last_name       VARCHAR(100)    NOT NULL,
    phone           VARCHAR(20),
    profile_picture VARCHAR(500),
    is_verified     BOOLEAN         DEFAULT FALSE,
    status          VARCHAR(20)     NOT NULL DEFAULT 'ACTIVE', -- ACTIVE, INACTIVE, SUSPENDED, DELETED
    last_login      TIMESTAMP,
    created_at      TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP       NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_user_phone ON "user"(phone);

-- ----------------------------------------------------------------------------
-- Table: role
-- Lookup table for user roles.
-- ----------------------------------------------------------------------------
CREATE TABLE role (
    role_id         BIGSERIAL       PRIMARY KEY,
    name            VARCHAR(50)     NOT NULL UNIQUE,
    description     VARCHAR(255)
);

-- ----------------------------------------------------------------------------
-- Table: user_role
-- Bridge table for many-to-many user-role assignment.
-- ----------------------------------------------------------------------------
CREATE TABLE user_role (
    user_id         BIGINT          NOT NULL REFERENCES "user"(user_id),
    role_id         BIGINT          NOT NULL REFERENCES role(role_id),
    PRIMARY KEY (user_id, role_id)
);

CREATE INDEX idx_user_role_role_id ON user_role(role_id);

-- ----------------------------------------------------------------------------
-- Table: refresh_token
-- Stores hashed refresh tokens for JWT authentication.
-- ----------------------------------------------------------------------------
CREATE TABLE refresh_token (
    refresh_token_id BIGSERIAL      PRIMARY KEY,
    user_id          BIGINT         NOT NULL REFERENCES "user"(user_id),
    token_hash       VARCHAR(255)   NOT NULL,
    expires_at       TIMESTAMP      NOT NULL,
    revoked          BOOLEAN        DEFAULT FALSE,
    device_name      VARCHAR(255),
    device_type      VARCHAR(50),
    created_at       TIMESTAMP      NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_refresh_token_user_id ON refresh_token(user_id);

-- ----------------------------------------------------------------------------
-- Table: user_preference
-- One-to-one relationship storing user preferences.
-- ----------------------------------------------------------------------------
CREATE TABLE user_preference (
    user_id             BIGINT          PRIMARY KEY REFERENCES "user"(user_id),
    preferred_currency  VARCHAR(3)      DEFAULT 'INR',
    preferred_language  VARCHAR(10)     DEFAULT 'en',
    preferred_city      VARCHAR(100),
    theme               VARCHAR(20)     DEFAULT 'LIGHT',
    marketing_emails    BOOLEAN         DEFAULT TRUE,
    email_notifications BOOLEAN         DEFAULT TRUE,
    push_notifications  BOOLEAN         DEFAULT TRUE,
    sms_notifications   BOOLEAN         DEFAULT FALSE
);

-- ----------------------------------------------------------------------------
-- Table: wishlist
-- User's saved/favorite properties.
-- ----------------------------------------------------------------------------
CREATE TABLE wishlist (
    wishlist_id     BIGSERIAL       PRIMARY KEY,
    user_id         BIGINT          NOT NULL REFERENCES "user"(user_id),
    property_id     BIGINT          NOT NULL REFERENCES property(property_id),
    created_at      TIMESTAMP       NOT NULL DEFAULT NOW(),
    UNIQUE (user_id, property_id)
);

CREATE INDEX idx_wishlist_property_id ON wishlist(property_id);

-- ----------------------------------------------------------------------------
-- Table: user_activity
-- Append-only analytics table for tracking user behavior.
-- ----------------------------------------------------------------------------
CREATE TABLE user_activity (
    activity_id     BIGSERIAL       PRIMARY KEY,
    user_id         BIGINT          NOT NULL REFERENCES "user"(user_id),
    activity_type   VARCHAR(50)     NOT NULL,
    entity_type     VARCHAR(50),
    entity_id       BIGINT,
    ip_address      VARCHAR(45),
    user_agent      TEXT,
    city            VARCHAR(100),
    country         VARCHAR(100),
    created_at      TIMESTAMP       NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_user_activity_user_id ON user_activity(user_id);
CREATE INDEX idx_user_activity_created_at ON user_activity(created_at);

-- ============================================================================
-- BOOKING AGGREGATE
-- ============================================================================

-- ----------------------------------------------------------------------------
-- Table: booking
-- Root entity of the Booking aggregate. Supports entire-property and room bookings.
-- ----------------------------------------------------------------------------
CREATE TABLE booking (
    booking_id          BIGSERIAL       PRIMARY KEY,
    booking_reference   VARCHAR(20)     NOT NULL UNIQUE,
    user_id             BIGINT          NOT NULL REFERENCES "user"(user_id),
    property_id         BIGINT          NOT NULL REFERENCES property(property_id),
    room_id             BIGINT          REFERENCES room(room_id), -- NULL = entire property
    booking_type        booking_type    NOT NULL,
    check_in            DATE            NOT NULL,
    check_out           DATE            NOT NULL,
    guest_count         INT             NOT NULL,
    booking_status      booking_status  NOT NULL DEFAULT 'PENDING',
    payment_status      payment_status  NOT NULL DEFAULT 'UNPAID',
    booking_source      booking_source  NOT NULL DEFAULT 'WEBSITE',
    special_requests    TEXT,
    total_amount        DECIMAL(10, 2)  NOT NULL,
    created_at          TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMP       NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_booking_user_id ON booking(user_id);
CREATE INDEX idx_booking_property_id ON booking(property_id);
CREATE INDEX idx_booking_status ON booking(booking_status);
CREATE INDEX idx_booking_dates ON booking(check_in, check_out);

-- ----------------------------------------------------------------------------
-- Table: booking_guest
-- Details of each guest associated with a booking.
-- ----------------------------------------------------------------------------
CREATE TABLE booking_guest (
    guest_id        BIGSERIAL       PRIMARY KEY,
    booking_id      BIGINT          NOT NULL REFERENCES booking(booking_id),
    full_name       VARCHAR(255)    NOT NULL,
    age             INT,
    gender          VARCHAR(10),
    government_id   VARCHAR(50),
    is_primary_guest BOOLEAN        DEFAULT FALSE
);

CREATE INDEX idx_booking_guest_booking_id ON booking_guest(booking_id);

-- ----------------------------------------------------------------------------
-- Table: payment
-- Payment records for bookings. Supports multiple payments per booking.
-- ----------------------------------------------------------------------------
CREATE TABLE payment (
    payment_id          BIGSERIAL       PRIMARY KEY,
    booking_id          BIGINT          NOT NULL REFERENCES booking(booking_id),
    amount              DECIMAL(10, 2)  NOT NULL,
    currency            VARCHAR(3)      NOT NULL DEFAULT 'INR',
    payment_method      VARCHAR(50)     NOT NULL,
    provider            VARCHAR(50)     NOT NULL,
    transaction_reference VARCHAR(255),
    status              VARCHAR(20)     NOT NULL DEFAULT 'PENDING', -- PENDING, SUCCESS, FAILED, REFUNDED
    paid_at             TIMESTAMP,
    created_at          TIMESTAMP       NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_payment_booking_id ON payment(booking_id);

-- ----------------------------------------------------------------------------
-- Table: payment_transaction
-- Immutable gateway transaction logs for audit trail.
-- ----------------------------------------------------------------------------
CREATE TABLE payment_transaction (
    transaction_id          BIGSERIAL       PRIMARY KEY,
    payment_id              BIGINT          NOT NULL REFERENCES payment(payment_id),
    gateway                 VARCHAR(50)     NOT NULL,
    gateway_transaction_id  VARCHAR(255),
    gateway_response        TEXT,
    status                  VARCHAR(20)     NOT NULL, -- SUCCESS, FAILED, PENDING
    created_at              TIMESTAMP       NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_payment_txn_payment_id ON payment_transaction(payment_id);

-- ----------------------------------------------------------------------------
-- Table: refund
-- Tracks refund requests and processing. Supports partial refunds.
-- ----------------------------------------------------------------------------
CREATE TABLE refund (
    refund_id       BIGSERIAL       PRIMARY KEY,
    payment_id      BIGINT          NOT NULL REFERENCES payment(payment_id),
    amount          DECIMAL(10, 2)  NOT NULL,
    reason          VARCHAR(500)    NOT NULL,
    status          VARCHAR(20)     NOT NULL DEFAULT 'PENDING', -- PENDING, APPROVED, PROCESSED, FAILED
    processed_at    TIMESTAMP,
    created_at      TIMESTAMP       NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_refund_payment_id ON refund(payment_id);

-- ----------------------------------------------------------------------------
-- Table: invoice
-- Tax invoice generated for completed bookings.
-- ----------------------------------------------------------------------------
CREATE TABLE invoice (
    invoice_id      BIGSERIAL       PRIMARY KEY,
    booking_id      BIGINT          NOT NULL REFERENCES booking(booking_id),
    invoice_number  VARCHAR(50)     NOT NULL UNIQUE,
    pdf_url         VARCHAR(500),
    subtotal        DECIMAL(10, 2)  NOT NULL,
    tax             DECIMAL(10, 2)  DEFAULT 0,
    discount        DECIMAL(10, 2)  DEFAULT 0,
    grand_total     DECIMAL(10, 2)  NOT NULL,
    status          VARCHAR(20)     NOT NULL DEFAULT 'DRAFT', -- DRAFT, ISSUED, PAID, CANCELLED
    issued_at       TIMESTAMP
);

CREATE INDEX idx_invoice_booking_id ON invoice(booking_id);

-- ----------------------------------------------------------------------------
-- Table: booking_timeline
-- Append-only event history for complete audit trail of booking lifecycle.
-- ----------------------------------------------------------------------------
CREATE TABLE booking_timeline (
    timeline_id     BIGSERIAL       PRIMARY KEY,
    booking_id      BIGINT          NOT NULL REFERENCES booking(booking_id),
    event_type      VARCHAR(50)     NOT NULL,
    performed_by    VARCHAR(100)    NOT NULL,
    remarks         TEXT,
    created_at      TIMESTAMP       NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_booking_timeline_booking ON booking_timeline(booking_id, created_at);

-- ============================================================================
-- REVIEW AGGREGATE
-- ============================================================================

-- ----------------------------------------------------------------------------
-- Table: review
-- Guest reviews for properties. One review per completed booking.
-- ----------------------------------------------------------------------------
CREATE TABLE review (
    review_id       BIGSERIAL       PRIMARY KEY,
    booking_id      BIGINT          NOT NULL UNIQUE REFERENCES booking(booking_id),
    property_id     BIGINT          NOT NULL REFERENCES property(property_id),
    user_id         BIGINT          NOT NULL REFERENCES "user"(user_id),
    rating          DECIMAL(2, 1)   NOT NULL CHECK (rating >= 1.0 AND rating <= 5.0),
    title           VARCHAR(255),
    review_text     TEXT,
    status          VARCHAR(20)     NOT NULL DEFAULT 'PENDING', -- PENDING, APPROVED, REJECTED, FLAGGED
    created_at      TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP       NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_review_property_id ON review(property_id);
CREATE INDEX idx_review_user_id ON review(user_id);

-- ----------------------------------------------------------------------------
-- Table: review_image
-- Images attached to reviews.
-- ----------------------------------------------------------------------------
CREATE TABLE review_image (
    review_image_id BIGSERIAL       PRIMARY KEY,
    review_id       BIGINT          NOT NULL REFERENCES review(review_id),
    image_url       VARCHAR(500)    NOT NULL,
    display_order   INT             NOT NULL
);

CREATE INDEX idx_review_image_review_id ON review_image(review_id);

-- ============================================================================
-- NOTIFICATION AGGREGATE
-- ============================================================================

-- ----------------------------------------------------------------------------
-- Table: notification_template
-- Reusable notification templates for different channels.
-- ----------------------------------------------------------------------------
CREATE TABLE notification_template (
    template_id     BIGSERIAL       PRIMARY KEY,
    name            VARCHAR(100)    NOT NULL UNIQUE,
    channel         notification_channel NOT NULL,
    subject         VARCHAR(255),
    body            TEXT            NOT NULL,
    variables       TEXT, -- JSON list of expected variables
    created_at      TIMESTAMP       NOT NULL DEFAULT NOW()
);

-- ----------------------------------------------------------------------------
-- Table: notification
-- Individual notification records sent to users.
-- ----------------------------------------------------------------------------
CREATE TABLE notification (
    notification_id BIGSERIAL       PRIMARY KEY,
    user_id         BIGINT          NOT NULL REFERENCES "user"(user_id),
    template_id     BIGINT          NOT NULL REFERENCES notification_template(template_id),
    channel         notification_channel NOT NULL,
    recipient       VARCHAR(255)    NOT NULL,
    status          VARCHAR(20)     NOT NULL DEFAULT 'PENDING', -- PENDING, SENT, FAILED
    scheduled_at    TIMESTAMP,
    sent_at         TIMESTAMP,
    created_at      TIMESTAMP       NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_notification_user_id ON notification(user_id);

-- ----------------------------------------------------------------------------
-- Table: notification_log
-- Immutable delivery history for audit trail.
-- ----------------------------------------------------------------------------
CREATE TABLE notification_log (
    log_id              BIGSERIAL       PRIMARY KEY,
    notification_id     BIGINT          NOT NULL REFERENCES notification(notification_id),
    provider            VARCHAR(50)     NOT NULL,
    provider_response   TEXT,
    status              VARCHAR(20)     NOT NULL, -- SENT, FAILED, BOUNCED, OPENED
    created_at          TIMESTAMP       NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_notification_log_notification ON notification_log(notification_id);
