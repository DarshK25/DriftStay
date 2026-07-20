-- ============================================================================
-- DriftStay Database Schema V1
-- Flyway Migration: V1__initial_schema.sql
-- Description: Creates all core tables, constraints, indexes, and relationships
-- ============================================================================

-- ============================================================================
-- PROPERTY AGGREGATE
-- ============================================================================

CREATE TABLE property (
    property_id         BIGSERIAL               PRIMARY KEY,
    public_id           VARCHAR(26)             NOT NULL,
    version             BIGINT                  NOT NULL DEFAULT 0,
    slug                VARCHAR(255)            NOT NULL,
    name                VARCHAR(255)            NOT NULL,
    short_description   VARCHAR(500),
    description         TEXT,
    property_type       VARCHAR(50)             NOT NULL,
    star_category       INT,
    status              VARCHAR(20)             NOT NULL DEFAULT 'ACTIVE',
    address_line_1      VARCHAR(255)            NOT NULL,
    address_line_2      VARCHAR(255),
    landmark            VARCHAR(255),
    city                VARCHAR(100)            NOT NULL,
    state               VARCHAR(100)            NOT NULL,
    country             VARCHAR(100)            NOT NULL,
    postal_code         VARCHAR(20),
    latitude            DECIMAL(10, 7),
    longitude           DECIMAL(10, 7),
    average_rating      DECIMAL(2, 1)           DEFAULT 0,
    review_count        INT                     DEFAULT 0,
    booking_count       INT                     DEFAULT 0,
    created_at          TIMESTAMPTZ             NOT NULL,
    updated_at          TIMESTAMPTZ             NOT NULL,

    CONSTRAINT uk_property_public_id UNIQUE (public_id),
    CONSTRAINT uk_property_slug UNIQUE (slug),
    CONSTRAINT ck_property_status CHECK (status IN ('ACTIVE', 'INACTIVE', 'ARCHIVED', 'DELETED')),
    CONSTRAINT ck_property_star_category CHECK (star_category IS NULL OR (star_category BETWEEN 1 AND 5)),
    CONSTRAINT ck_property_latitude CHECK (latitude IS NULL OR (latitude BETWEEN -90 AND 90)),
    CONSTRAINT ck_property_longitude CHECK (longitude IS NULL OR (longitude BETWEEN -180 AND 180))
);

CREATE INDEX idx_property_city ON property(city);
CREATE INDEX idx_property_status ON property(status);
CREATE INDEX idx_property_status_city ON property(status, city);
CREATE INDEX idx_property_geo ON property(latitude, longitude);

CREATE TABLE property_image (
    image_id            BIGSERIAL               PRIMARY KEY,
    version             BIGINT                  NOT NULL DEFAULT 0,
    property_id         BIGINT                  NOT NULL,
    image_url           VARCHAR(500)            NOT NULL,
    alt_text            VARCHAR(255),
    caption             VARCHAR(255),
    display_order       INT                     NOT NULL,
    is_thumbnail        BOOLEAN                 DEFAULT FALSE,
    created_at          TIMESTAMPTZ             NOT NULL,
    updated_at          TIMESTAMPTZ             NOT NULL,

    CONSTRAINT fk_property_image_property FOREIGN KEY (property_id) REFERENCES property(property_id) ON DELETE CASCADE,
    CONSTRAINT uk_property_image_order UNIQUE (property_id, display_order)
);

CREATE INDEX idx_property_image_property ON property_image(property_id);

CREATE TABLE amenity (
    amenity_id          BIGSERIAL               PRIMARY KEY,
    version             BIGINT                  NOT NULL DEFAULT 0,
    name                VARCHAR(100)            NOT NULL,
    icon                VARCHAR(255),
    category            VARCHAR(50),
    created_at          TIMESTAMPTZ             NOT NULL,
    updated_at          TIMESTAMPTZ             NOT NULL,

    CONSTRAINT uk_amenity_name UNIQUE (name)
);

CREATE TABLE property_amenity (
    id                  BIGSERIAL               PRIMARY KEY,
    version             BIGINT                  NOT NULL DEFAULT 0,
    property_id         BIGINT                  NOT NULL,
    amenity_id          BIGINT                  NOT NULL,
    created_at          TIMESTAMPTZ             NOT NULL,
    updated_at          TIMESTAMPTZ             NOT NULL,

    CONSTRAINT fk_property_amenity_property FOREIGN KEY (property_id) REFERENCES property(property_id) ON DELETE CASCADE,
    CONSTRAINT fk_property_amenity_amenity FOREIGN KEY (amenity_id) REFERENCES amenity(amenity_id) ON DELETE RESTRICT,
    CONSTRAINT uk_property_amenity UNIQUE (property_id, amenity_id)
);

CREATE INDEX idx_property_amenity_amenity ON property_amenity(amenity_id);

CREATE TABLE property_policy (
    policy_id               BIGSERIAL           PRIMARY KEY,
    version                 BIGINT              NOT NULL DEFAULT 0,
    property_id             BIGINT              NOT NULL,
    check_in_time           TIME                NOT NULL,
    check_out_time          TIME                NOT NULL,
    quiet_hours_start       TIME,
    quiet_hours_end         TIME,
    no_parties              BOOLEAN             DEFAULT FALSE,
    visitors_allowed        BOOLEAN             DEFAULT TRUE,
    pets_allowed            BOOLEAN             DEFAULT FALSE,
    smoking_allowed         BOOLEAN             DEFAULT FALSE,
    minimum_age             INT                 DEFAULT 18,
    free_cancellation_hours INT                 DEFAULT 48,
    extra_bed_available     BOOLEAN             DEFAULT FALSE,
    created_at              TIMESTAMPTZ         NOT NULL,
    updated_at              TIMESTAMPTZ         NOT NULL,

    CONSTRAINT fk_property_policy_property FOREIGN KEY (property_id) REFERENCES property(property_id) ON DELETE CASCADE,
    CONSTRAINT uk_property_policy_property UNIQUE (property_id),
    CONSTRAINT ck_property_policy_min_age CHECK (minimum_age >= 0),
    CONSTRAINT ck_property_policy_cancel_hours CHECK (free_cancellation_hours >= 0)
);

CREATE TABLE property_contact (
    contact_id          BIGSERIAL               PRIMARY KEY,
    version             BIGINT                  NOT NULL DEFAULT 0,
    property_id         BIGINT                  NOT NULL,
    contact_name        VARCHAR(255)            NOT NULL,
    designation         VARCHAR(100),
    email               VARCHAR(255),
    phone               VARCHAR(20)             NOT NULL,
    is_primary          BOOLEAN                 DEFAULT FALSE,
    created_at          TIMESTAMPTZ             NOT NULL,
    updated_at          TIMESTAMPTZ             NOT NULL,

    CONSTRAINT fk_property_contact_property FOREIGN KEY (property_id) REFERENCES property(property_id) ON DELETE CASCADE
);

CREATE INDEX idx_property_contact_property ON property_contact(property_id);
CREATE UNIQUE INDEX idx_property_contact_primary ON property_contact(property_id) WHERE is_primary = TRUE;

CREATE TABLE room (
    room_id             BIGSERIAL               PRIMARY KEY,
    public_id           VARCHAR(26)             NOT NULL,
    version             BIGINT                  NOT NULL DEFAULT 0,
    property_id         BIGINT                  NOT NULL,
    room_number         VARCHAR(20),
    room_name           VARCHAR(255)            NOT NULL,
    description         TEXT,
    room_type           VARCHAR(50)             NOT NULL,
    capacity            INT                     NOT NULL,
    bed_count           INT                     NOT NULL,
    bed_type            VARCHAR(50)             NOT NULL,
    bathroom_count      INT,
    base_price          DECIMAL(12, 2)          NOT NULL,
    weekend_price       DECIMAL(12, 2),
    cleaning_fee        DECIMAL(12, 2),
    extra_guest_fee     DECIMAL(12, 2),
    area_sqft           INT,
    floor_number        INT,
    status              VARCHAR(20)             NOT NULL DEFAULT 'ACTIVE',
    created_at          TIMESTAMPTZ             NOT NULL,
    updated_at          TIMESTAMPTZ             NOT NULL,

    CONSTRAINT fk_room_property FOREIGN KEY (property_id) REFERENCES property(property_id) ON DELETE CASCADE,
    CONSTRAINT uk_room_public_id UNIQUE (public_id),
    CONSTRAINT uk_room_property_number UNIQUE (property_id, room_number),
    CONSTRAINT ck_room_status CHECK (status IN ('ACTIVE', 'INACTIVE', 'MAINTENANCE', 'DELETED')),
    CONSTRAINT ck_room_capacity CHECK (capacity > 0),
    CONSTRAINT ck_room_base_price CHECK (base_price >= 0),
    CONSTRAINT ck_room_weekend_price CHECK (weekend_price IS NULL OR weekend_price >= 0),
    CONSTRAINT ck_room_cleaning_fee CHECK (cleaning_fee IS NULL OR cleaning_fee >= 0),
    CONSTRAINT ck_room_extra_guest_fee CHECK (extra_guest_fee IS NULL OR extra_guest_fee >= 0)
);

CREATE INDEX idx_room_property_status ON room(property_id, status);
CREATE INDEX idx_room_property_status_capacity ON room(property_id, status, capacity);

CREATE TABLE room_image (
    room_image_id       BIGSERIAL               PRIMARY KEY,
    version             BIGINT                  NOT NULL DEFAULT 0,
    room_id             BIGINT                  NOT NULL,
    image_url           VARCHAR(500)            NOT NULL,
    alt_text            VARCHAR(255),
    caption             VARCHAR(255),
    display_order       INT                     NOT NULL,
    is_thumbnail        BOOLEAN                 DEFAULT FALSE,
    created_at          TIMESTAMPTZ             NOT NULL,
    updated_at          TIMESTAMPTZ             NOT NULL,

    CONSTRAINT fk_room_image_room FOREIGN KEY (room_id) REFERENCES room(room_id) ON DELETE CASCADE,
    CONSTRAINT uk_room_image_order UNIQUE (room_id, display_order)
);

CREATE INDEX idx_room_image_room ON room_image(room_id);

CREATE TABLE room_availability (
    availability_id     BIGSERIAL               PRIMARY KEY,
    version             BIGINT                  NOT NULL DEFAULT 0,
    room_id             BIGINT                  NOT NULL,
    start_date          DATE                    NOT NULL,
    end_date            DATE                    NOT NULL,
    status              VARCHAR(20)             NOT NULL DEFAULT 'AVAILABLE',
    reason              VARCHAR(255),
    created_by          VARCHAR(100)            NOT NULL,
    created_at          TIMESTAMPTZ             NOT NULL,
    updated_at          TIMESTAMPTZ             NOT NULL,

    CONSTRAINT fk_room_avail_room FOREIGN KEY (room_id) REFERENCES room(room_id) ON DELETE CASCADE,
    CONSTRAINT ck_room_avail_status CHECK (status IN ('AVAILABLE', 'BLOCKED', 'MAINTENANCE', 'RESERVED')),
    CONSTRAINT ck_room_avail_dates CHECK (end_date >= start_date)
);

CREATE INDEX idx_room_avail_room_dates ON room_availability(room_id, start_date, end_date);
CREATE INDEX idx_room_avail_room ON room_availability(room_id);
CREATE INDEX idx_room_avail_dates ON room_availability(start_date, end_date);
CREATE INDEX idx_room_avail_status ON room_availability(status);

-- ============================================================================
-- USER AGGREGATE
-- ============================================================================

CREATE TABLE "user" (
    user_id             BIGSERIAL               PRIMARY KEY,
    public_id           VARCHAR(26)             NOT NULL,
    version             BIGINT                  NOT NULL DEFAULT 0,
    email               VARCHAR(255)            NOT NULL,
    password_hash       VARCHAR(255)            NOT NULL,
    first_name          VARCHAR(100)            NOT NULL,
    last_name           VARCHAR(100)            NOT NULL,
    phone               VARCHAR(20),
    profile_picture     VARCHAR(500),
    is_verified         BOOLEAN                 DEFAULT FALSE,
    status              VARCHAR(20)             NOT NULL DEFAULT 'ACTIVE',
    last_login          TIMESTAMPTZ,
    created_at          TIMESTAMPTZ             NOT NULL,
    updated_at          TIMESTAMPTZ             NOT NULL,

    CONSTRAINT uk_user_public_id UNIQUE (public_id),
    CONSTRAINT uk_user_email UNIQUE (email),
    CONSTRAINT ck_user_status CHECK (status IN ('ACTIVE', 'INACTIVE', 'SUSPENDED', 'DELETED'))
);

CREATE INDEX idx_user_phone ON "user"(phone);
CREATE INDEX idx_user_status ON "user"(status);

CREATE TABLE role (
    role_id             BIGSERIAL               PRIMARY KEY,
    name                VARCHAR(50)             NOT NULL,
    description         VARCHAR(255),

    CONSTRAINT uk_role_name UNIQUE (name)
);

CREATE TABLE user_role (
    user_id             BIGINT                  NOT NULL,
    role_id             BIGINT                  NOT NULL,

    CONSTRAINT fk_user_role_user FOREIGN KEY (user_id) REFERENCES "user"(user_id) ON DELETE CASCADE,
    CONSTRAINT fk_user_role_role FOREIGN KEY (role_id) REFERENCES role(role_id) ON DELETE RESTRICT,
    PRIMARY KEY (user_id, role_id)
);

CREATE INDEX idx_user_role_role ON user_role(role_id);

CREATE TABLE refresh_token (
    refresh_token_id    BIGSERIAL               PRIMARY KEY,
    version             BIGINT                  NOT NULL DEFAULT 0,
    user_id             BIGINT                  NOT NULL,
    token_hash          VARCHAR(255)            NOT NULL,
    expires_at          TIMESTAMPTZ             NOT NULL,
    revoked             BOOLEAN                 NOT NULL DEFAULT FALSE,
    device_name         VARCHAR(255),
    device_type         VARCHAR(50),
    created_at          TIMESTAMPTZ             NOT NULL,
    updated_at          TIMESTAMPTZ             NOT NULL,

    CONSTRAINT fk_refresh_token_user FOREIGN KEY (user_id) REFERENCES "user"(user_id) ON DELETE CASCADE
);

CREATE INDEX idx_refresh_token_user ON refresh_token(user_id);
CREATE INDEX idx_refresh_token_expires ON refresh_token(expires_at);
CREATE INDEX idx_refresh_token_revoked ON refresh_token(revoked);
CREATE INDEX idx_refresh_token_hash ON refresh_token(token_hash);

CREATE TABLE user_preference (
    user_id                 BIGINT              PRIMARY KEY,
    version                 BIGINT              NOT NULL DEFAULT 0,
    preferred_currency      VARCHAR(3)          DEFAULT 'INR',
    preferred_language      VARCHAR(10)         DEFAULT 'en',
    preferred_city          VARCHAR(100),
    theme                   VARCHAR(20)         DEFAULT 'LIGHT',
    marketing_emails        BOOLEAN             DEFAULT TRUE,
    email_notifications     BOOLEAN             DEFAULT TRUE,
    push_notifications      BOOLEAN             DEFAULT TRUE,
    sms_notifications       BOOLEAN             DEFAULT FALSE,
    created_at              TIMESTAMPTZ         NOT NULL,
    updated_at              TIMESTAMPTZ         NOT NULL,

    CONSTRAINT fk_user_preference_user FOREIGN KEY (user_id) REFERENCES "user"(user_id) ON DELETE CASCADE
);

CREATE TABLE wishlist (
    wishlist_id         BIGSERIAL               PRIMARY KEY,
    version             BIGINT                  NOT NULL DEFAULT 0,
    user_id             BIGINT                  NOT NULL,
    property_id         BIGINT                  NOT NULL,
    created_at          TIMESTAMPTZ             NOT NULL,
    updated_at          TIMESTAMPTZ             NOT NULL,

    CONSTRAINT fk_wishlist_user FOREIGN KEY (user_id) REFERENCES "user"(user_id) ON DELETE CASCADE,
    CONSTRAINT fk_wishlist_property FOREIGN KEY (property_id) REFERENCES property(property_id) ON DELETE CASCADE,
    CONSTRAINT uk_wishlist_user_property UNIQUE (user_id, property_id)
);

CREATE INDEX idx_wishlist_property ON wishlist(property_id);

CREATE TABLE user_activity (
    activity_id         BIGSERIAL               PRIMARY KEY,
    version             BIGINT                  NOT NULL DEFAULT 0,
    user_id             BIGINT                  NOT NULL,
    activity_type       VARCHAR(50)             NOT NULL,
    entity_type         VARCHAR(50),
    entity_id           BIGINT,
    metadata            JSONB,
    ip_address          VARCHAR(45),
    user_agent          TEXT,
    city                VARCHAR(100),
    country             VARCHAR(100),
    created_at          TIMESTAMPTZ             NOT NULL,
    updated_at          TIMESTAMPTZ             NOT NULL,

    CONSTRAINT fk_user_activity_user FOREIGN KEY (user_id) REFERENCES "user"(user_id) ON DELETE CASCADE
);

CREATE INDEX idx_user_activity_user ON user_activity(user_id);
CREATE INDEX idx_user_activity_type ON user_activity(activity_type);
CREATE INDEX idx_user_activity_entity ON user_activity(entity_type, entity_id);
CREATE INDEX idx_user_activity_created ON user_activity(created_at);

CREATE TABLE search_history (
    search_id           BIGSERIAL               PRIMARY KEY,
    version             BIGINT                  NOT NULL DEFAULT 0,
    user_id             BIGINT                  NOT NULL,
    city                VARCHAR(100),
    check_in            DATE,
    check_out           DATE,
    guests              INT                     NOT NULL,
    filters             JSONB,
    created_at          TIMESTAMPTZ             NOT NULL,
    updated_at          TIMESTAMPTZ             NOT NULL,

    CONSTRAINT fk_search_history_user FOREIGN KEY (user_id) REFERENCES "user"(user_id) ON DELETE CASCADE,
    CONSTRAINT ck_search_history_guests CHECK (guests > 0)
);

CREATE INDEX idx_search_history_user ON search_history(user_id);
CREATE INDEX idx_search_history_created ON search_history(created_at);

-- ============================================================================
-- BOOKING AGGREGATE
-- ============================================================================

CREATE TABLE booking (
    booking_id              BIGSERIAL           PRIMARY KEY,
    public_id               VARCHAR(26)         NOT NULL,
    version                 BIGINT              NOT NULL DEFAULT 0,
    booking_reference       VARCHAR(20)         NOT NULL,
    user_id                 BIGINT              NOT NULL,
    property_id             BIGINT              NOT NULL,
    room_id                 BIGINT,
    booking_type            VARCHAR(20)         NOT NULL,
    check_in                DATE                NOT NULL,
    check_out               DATE                NOT NULL,
    guest_count             INT                 NOT NULL,
    booking_status          VARCHAR(20)         NOT NULL DEFAULT 'PENDING',
    payment_status          VARCHAR(20)         NOT NULL DEFAULT 'UNPAID',
    booking_source          VARCHAR(20)         NOT NULL DEFAULT 'WEBSITE',
    special_requests        TEXT,
    base_price_snapshot     DECIMAL(12, 2),
    cleaning_fee_snapshot   DECIMAL(12, 2),
    tax_snapshot            DECIMAL(12, 2),
    discount_snapshot       DECIMAL(12, 2),
    total_amount            DECIMAL(12, 2)      NOT NULL,
    created_at              TIMESTAMPTZ         NOT NULL,
    updated_at              TIMESTAMPTZ         NOT NULL,

    CONSTRAINT fk_booking_user FOREIGN KEY (user_id) REFERENCES "user"(user_id) ON DELETE RESTRICT,
    CONSTRAINT fk_booking_property FOREIGN KEY (property_id) REFERENCES property(property_id) ON DELETE RESTRICT,
    CONSTRAINT fk_booking_room FOREIGN KEY (room_id) REFERENCES room(room_id) ON DELETE SET NULL,
    CONSTRAINT uk_booking_public_id UNIQUE (public_id),
    CONSTRAINT uk_booking_reference UNIQUE (booking_reference),
    CONSTRAINT ck_booking_type CHECK (booking_type IN ('PROPERTY', 'ROOM')),
    CONSTRAINT ck_booking_status CHECK (booking_status IN ('PENDING', 'CONFIRMED', 'CHECKED_IN', 'CHECKED_OUT', 'CANCELLED', 'NO_SHOW')),
    CONSTRAINT ck_booking_payment_status CHECK (payment_status IN ('UNPAID', 'PARTIALLY_PAID', 'PAID', 'REFUNDED', 'PARTIALLY_REFUNDED')),
    CONSTRAINT ck_booking_source CHECK (booking_source IN ('WEBSITE', 'MOBILE_APP', 'ADMIN', 'AGENCY')),
    CONSTRAINT ck_booking_guest_count CHECK (guest_count > 0),
    CONSTRAINT ck_booking_dates CHECK (check_out > check_in),
    CONSTRAINT ck_booking_total CHECK (total_amount >= 0),
    CONSTRAINT ck_booking_base_price CHECK (base_price_snapshot IS NULL OR base_price_snapshot >= 0),
    CONSTRAINT ck_booking_cleaning_fee CHECK (cleaning_fee_snapshot IS NULL OR cleaning_fee_snapshot >= 0),
    CONSTRAINT ck_booking_tax CHECK (tax_snapshot IS NULL OR tax_snapshot >= 0),
    CONSTRAINT ck_booking_discount CHECK (discount_snapshot IS NULL OR discount_snapshot >= 0)
);

CREATE INDEX idx_booking_user ON booking(user_id);
CREATE INDEX idx_booking_property ON booking(property_id);
CREATE INDEX idx_booking_property_room ON booking(property_id, room_id);
CREATE INDEX idx_booking_status ON booking(booking_status);
CREATE INDEX idx_booking_dates ON booking(check_in, check_out);

CREATE TABLE booking_guest (
    guest_id            BIGSERIAL               PRIMARY KEY,
    version             BIGINT                  NOT NULL DEFAULT 0,
    booking_id          BIGINT                  NOT NULL,
    full_name           VARCHAR(255)            NOT NULL,
    age                 INT,
    gender              VARCHAR(10),
    government_id       VARCHAR(50),
    is_primary_guest    BOOLEAN                 DEFAULT FALSE,
    created_at          TIMESTAMPTZ             NOT NULL,
    updated_at          TIMESTAMPTZ             NOT NULL,

    CONSTRAINT fk_booking_guest_booking FOREIGN KEY (booking_id) REFERENCES booking(booking_id) ON DELETE CASCADE,
    CONSTRAINT ck_booking_guest_age CHECK (age IS NULL OR age >= 0)
);

CREATE INDEX idx_booking_guest_booking ON booking_guest(booking_id);
CREATE UNIQUE INDEX idx_booking_guest_primary ON booking_guest(booking_id) WHERE is_primary_guest = TRUE;

CREATE TABLE payment (
    payment_id              BIGSERIAL           PRIMARY KEY,
    version                 BIGINT              NOT NULL DEFAULT 0,
    booking_id              BIGINT              NOT NULL,
    amount                  DECIMAL(12, 2)      NOT NULL,
    currency                VARCHAR(3)          NOT NULL DEFAULT 'INR',
    payment_method          VARCHAR(50)         NOT NULL,
    provider                VARCHAR(50)         NOT NULL,
    provider_payment_id     VARCHAR(255),
    provider_order_id       VARCHAR(255),
    transaction_reference   VARCHAR(255),
    status                  VARCHAR(20)         NOT NULL DEFAULT 'PENDING',
    paid_at                 TIMESTAMPTZ,
    created_at              TIMESTAMPTZ         NOT NULL,
    updated_at              TIMESTAMPTZ         NOT NULL,

    CONSTRAINT fk_payment_booking FOREIGN KEY (booking_id) REFERENCES booking(booking_id) ON DELETE CASCADE,
    CONSTRAINT ck_payment_status CHECK (status IN ('PENDING', 'SUCCESS', 'FAILED', 'REFUNDED')),
    CONSTRAINT ck_payment_amount CHECK (amount >= 0)
);

CREATE INDEX idx_payment_booking ON payment(booking_id);
CREATE INDEX idx_payment_status ON payment(status);
CREATE INDEX idx_payment_provider_id ON payment(provider_payment_id);

CREATE TABLE payment_transaction (
    transaction_id          BIGSERIAL           PRIMARY KEY,
    version                 BIGINT              NOT NULL DEFAULT 0,
    payment_id              BIGINT              NOT NULL,
    gateway                 VARCHAR(50)         NOT NULL,
    gateway_transaction_id  VARCHAR(255),
    gateway_response        JSONB,
    status                  VARCHAR(20)         NOT NULL,
    created_at              TIMESTAMPTZ         NOT NULL,
    updated_at              TIMESTAMPTZ         NOT NULL,

    CONSTRAINT fk_payment_txn_payment FOREIGN KEY (payment_id) REFERENCES payment(payment_id) ON DELETE CASCADE,
    CONSTRAINT ck_payment_txn_status CHECK (status IN ('PENDING', 'SUCCESS', 'FAILED'))
);

CREATE INDEX idx_payment_txn_payment ON payment_transaction(payment_id);
CREATE INDEX idx_payment_txn_gateway ON payment_transaction(gateway);
CREATE INDEX idx_payment_txn_status ON payment_transaction(status);

CREATE TABLE refund (
    refund_id           BIGSERIAL               PRIMARY KEY,
    version             BIGINT                  NOT NULL DEFAULT 0,
    payment_id          BIGINT                  NOT NULL,
    refund_reference    VARCHAR(50)             NOT NULL,
    amount              DECIMAL(12, 2)          NOT NULL,
    reason              VARCHAR(500)            NOT NULL,
    status              VARCHAR(20)             NOT NULL DEFAULT 'PENDING',
    processed_at        TIMESTAMPTZ,
    created_at          TIMESTAMPTZ             NOT NULL,
    updated_at          TIMESTAMPTZ             NOT NULL,

    CONSTRAINT fk_refund_payment FOREIGN KEY (payment_id) REFERENCES payment(payment_id) ON DELETE CASCADE,
    CONSTRAINT uk_refund_reference UNIQUE (refund_reference),
    CONSTRAINT ck_refund_status CHECK (status IN ('PENDING', 'PROCESSED', 'FAILED', 'CANCELLED')),
    CONSTRAINT ck_refund_amount CHECK (amount >= 0)
);

CREATE INDEX idx_refund_payment ON refund(payment_id);
CREATE INDEX idx_refund_status ON refund(status);

CREATE TABLE invoice (
    invoice_id          BIGSERIAL               PRIMARY KEY,
    version             BIGINT                  NOT NULL DEFAULT 0,
    booking_id          BIGINT                  NOT NULL,
    invoice_number      VARCHAR(50)             NOT NULL,
    pdf_url             VARCHAR(500),
    subtotal            DECIMAL(12, 2)          NOT NULL,
    tax                 DECIMAL(12, 2),
    discount            DECIMAL(12, 2),
    grand_total         DECIMAL(12, 2)          NOT NULL,
    status              VARCHAR(20)             NOT NULL DEFAULT 'PENDING',
    issued_at           TIMESTAMPTZ,
    created_at          TIMESTAMPTZ             NOT NULL,
    updated_at          TIMESTAMPTZ             NOT NULL,

    CONSTRAINT fk_invoice_booking FOREIGN KEY (booking_id) REFERENCES booking(booking_id) ON DELETE CASCADE,
    CONSTRAINT uk_invoice_number UNIQUE (invoice_number),
    CONSTRAINT ck_invoice_status CHECK (status IN ('PENDING', 'PAID', 'CANCELLED', 'REFUNDED')),
    CONSTRAINT ck_invoice_subtotal CHECK (subtotal >= 0),
    CONSTRAINT ck_invoice_tax CHECK (tax IS NULL OR tax >= 0),
    CONSTRAINT ck_invoice_discount CHECK (discount IS NULL OR discount >= 0),
    CONSTRAINT ck_invoice_grand_total CHECK (grand_total >= 0)
);

CREATE INDEX idx_invoice_booking ON invoice(booking_id);

CREATE TABLE booking_timeline (
    timeline_id         BIGSERIAL               PRIMARY KEY,
    version             BIGINT                  NOT NULL DEFAULT 0,
    booking_id          BIGINT                  NOT NULL,
    event_type          VARCHAR(50)             NOT NULL,
    performed_by        VARCHAR(100)            NOT NULL,
    remarks             TEXT,
    created_at          TIMESTAMPTZ             NOT NULL,
    updated_at          TIMESTAMPTZ             NOT NULL,

    CONSTRAINT fk_booking_timeline_booking FOREIGN KEY (booking_id) REFERENCES booking(booking_id) ON DELETE CASCADE
);

CREATE INDEX idx_booking_timeline_booking ON booking_timeline(booking_id);
CREATE INDEX idx_booking_timeline_created ON booking_timeline(booking_id, created_at);

-- ============================================================================
-- REVIEW AGGREGATE
-- ============================================================================

CREATE TABLE review (
    review_id           BIGSERIAL               PRIMARY KEY,
    public_id           VARCHAR(26)             NOT NULL,
    version             BIGINT                  NOT NULL DEFAULT 0,
    booking_id          BIGINT                  NOT NULL,
    property_id         BIGINT                  NOT NULL,
    user_id             BIGINT                  NOT NULL,
    rating              NUMERIC(3, 2)           NOT NULL,
    title               VARCHAR(255),
    review_text         TEXT,
    status              VARCHAR(20)             NOT NULL DEFAULT 'PENDING',
    created_at          TIMESTAMPTZ             NOT NULL,
    updated_at          TIMESTAMPTZ             NOT NULL,

    CONSTRAINT fk_review_booking FOREIGN KEY (booking_id) REFERENCES booking(booking_id) ON DELETE RESTRICT,
    CONSTRAINT fk_review_property FOREIGN KEY (property_id) REFERENCES property(property_id) ON DELETE CASCADE,
    CONSTRAINT fk_review_user FOREIGN KEY (user_id) REFERENCES "user"(user_id) ON DELETE RESTRICT,
    CONSTRAINT uk_review_public_id UNIQUE (public_id),
    CONSTRAINT uk_review_booking UNIQUE (booking_id),
    CONSTRAINT ck_review_rating CHECK (rating >= 1.0 AND rating <= 5.0),
    CONSTRAINT ck_review_status CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED', 'HIDDEN'))
);

CREATE INDEX idx_review_property ON review(property_id);
CREATE INDEX idx_review_user ON review(user_id);
CREATE INDEX idx_review_status ON review(status);
CREATE INDEX idx_review_property_status ON review(property_id, status);

CREATE TABLE review_image (
    review_image_id     BIGSERIAL               PRIMARY KEY,
    version             BIGINT                  NOT NULL DEFAULT 0,
    review_id           BIGINT                  NOT NULL,
    image_url           VARCHAR(500)            NOT NULL,
    alt_text            VARCHAR(255),
    display_order       INT                     NOT NULL,
    created_at          TIMESTAMPTZ             NOT NULL,
    updated_at          TIMESTAMPTZ             NOT NULL,

    CONSTRAINT fk_review_image_review FOREIGN KEY (review_id) REFERENCES review(review_id) ON DELETE CASCADE,
    CONSTRAINT uk_review_image_order UNIQUE (review_id, display_order)
);

CREATE INDEX idx_review_image_review ON review_image(review_id);

-- ============================================================================
-- NOTIFICATION AGGREGATE
-- ============================================================================

CREATE TABLE notification_template (
    template_id         BIGSERIAL               PRIMARY KEY,
    version             BIGINT                  NOT NULL DEFAULT 0,
    name                VARCHAR(100)            NOT NULL,
    channel             VARCHAR(20)             NOT NULL,
    subject             VARCHAR(255),
    body                TEXT                    NOT NULL,
    variables           JSONB,
    created_at          TIMESTAMPTZ             NOT NULL,
    updated_at          TIMESTAMPTZ             NOT NULL,

    CONSTRAINT uk_notification_template_name UNIQUE (name),
    CONSTRAINT ck_notification_template_channel CHECK (channel IN ('EMAIL', 'SMS', 'PUSH', 'IN_APP'))
);

CREATE INDEX idx_notification_template_channel ON notification_template(channel);

CREATE TABLE notification (
    notification_id     BIGSERIAL               PRIMARY KEY,
    version             BIGINT                  NOT NULL DEFAULT 0,
    user_id             BIGINT                  NOT NULL,
    template_id         BIGINT                  NOT NULL,
    channel             VARCHAR(20)             NOT NULL,
    recipient           VARCHAR(255)            NOT NULL,
    status              VARCHAR(20)             NOT NULL DEFAULT 'PENDING',
    scheduled_at        TIMESTAMPTZ,
    sent_at             TIMESTAMPTZ,
    rendered_body       TEXT,
    created_at          TIMESTAMPTZ             NOT NULL,
    updated_at          TIMESTAMPTZ             NOT NULL,

    CONSTRAINT fk_notification_user FOREIGN KEY (user_id) REFERENCES "user"(user_id) ON DELETE CASCADE,
    CONSTRAINT fk_notification_template FOREIGN KEY (template_id) REFERENCES notification_template(template_id) ON DELETE RESTRICT,
    CONSTRAINT ck_notification_channel CHECK (channel IN ('EMAIL', 'SMS', 'PUSH', 'IN_APP')),
    CONSTRAINT ck_notification_status CHECK (status IN ('PENDING', 'SENT', 'FAILED', 'CANCELLED'))
);

CREATE INDEX idx_notification_user ON notification(user_id);
CREATE INDEX idx_notification_template ON notification(template_id);
CREATE INDEX idx_notification_status ON notification(status);
CREATE INDEX idx_notification_scheduled ON notification(scheduled_at);
CREATE INDEX idx_notification_status_scheduled ON notification(status, scheduled_at);

CREATE TABLE notification_log (
    log_id              BIGSERIAL               PRIMARY KEY,
    version             BIGINT                  NOT NULL DEFAULT 0,
    notification_id     BIGINT                  NOT NULL,
    provider            VARCHAR(50)             NOT NULL,
    provider_response   TEXT,
    status              VARCHAR(20)             NOT NULL,
    created_at          TIMESTAMPTZ             NOT NULL,
    updated_at          TIMESTAMPTZ             NOT NULL,

    CONSTRAINT fk_notification_log_notification FOREIGN KEY (notification_id) REFERENCES notification(notification_id) ON DELETE CASCADE,
    CONSTRAINT ck_notification_log_status CHECK (status IN ('PENDING', 'SENT', 'FAILED'))
);

CREATE INDEX idx_notification_log_notification ON notification_log(notification_id);
CREATE INDEX idx_notification_log_status ON notification_log(status);
CREATE INDEX idx_notification_log_provider ON notification_log(provider);
