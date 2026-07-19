-- ============================================================================
-- DriftStay Database Schema v1.0
-- Flyway Migration: V1__initial_schema.sql
-- Description: Creates all core tables, indexes, and relationships
-- ============================================================================

-- ============================================================================
-- PROPERTY AGGREGATE
-- ============================================================================

CREATE TABLE property (
    property_id         BIGSERIAL           PRIMARY KEY,
    public_id           VARCHAR(26)         NOT NULL,
    version             BIGINT              NOT NULL DEFAULT 0,
    slug                VARCHAR(255)        NOT NULL,
    name                VARCHAR(255)        NOT NULL,
    short_description   VARCHAR(500),
    description         TEXT,
    property_type       VARCHAR(50)         NOT NULL,
    star_category       INT,
    status              VARCHAR(20)         NOT NULL DEFAULT 'ACTIVE',
    address_line_1      VARCHAR(255)        NOT NULL,
    address_line_2      VARCHAR(255),
    landmark            VARCHAR(255),
    city                VARCHAR(100)        NOT NULL,
    state               VARCHAR(100)        NOT NULL,
    country             VARCHAR(100)        NOT NULL,
    postal_code         VARCHAR(20),
    latitude            DECIMAL(10, 7),
    longitude           DECIMAL(10, 7),
    average_rating      DECIMAL(2, 1)       DEFAULT 0,
    review_count        INT                 DEFAULT 0,
    booking_count       INT                 DEFAULT 0,
    created_at          TIMESTAMP           NOT NULL,
    updated_at          TIMESTAMP           NOT NULL
);

CREATE UNIQUE INDEX idx_property_public_id ON property(public_id);
CREATE UNIQUE INDEX idx_property_slug ON property(slug);
CREATE INDEX idx_property_city ON property(city);
CREATE INDEX idx_property_status ON property(status);
CREATE INDEX idx_property_status_city ON property(status, city);
CREATE INDEX idx_property_geo ON property(latitude, longitude);

CREATE TABLE property_image (
    image_id            BIGSERIAL           PRIMARY KEY,
    version             BIGINT              NOT NULL DEFAULT 0,
    property_id         BIGINT              NOT NULL REFERENCES property(property_id),
    image_url           VARCHAR(500)        NOT NULL,
    alt_text            VARCHAR(255),
    caption             VARCHAR(255),
    display_order       INT                 NOT NULL,
    is_thumbnail        BOOLEAN             DEFAULT FALSE,
    created_at          TIMESTAMP           NOT NULL,
    updated_at          TIMESTAMP           NOT NULL,
    UNIQUE (property_id, display_order)
);

CREATE INDEX idx_property_image_property_order ON property_image(property_id, display_order);
CREATE INDEX idx_property_image_property ON property_image(property_id);

CREATE TABLE amenity (
    amenity_id          BIGSERIAL           PRIMARY KEY,
    version             BIGINT              NOT NULL DEFAULT 0,
    name                VARCHAR(100)        NOT NULL,
    icon                VARCHAR(255),
    category            VARCHAR(50),
    created_at          TIMESTAMP           NOT NULL,
    updated_at          TIMESTAMP           NOT NULL
);

CREATE UNIQUE INDEX uk_amenity_name ON amenity(name);

CREATE TABLE property_amenity (
    id                  BIGSERIAL           PRIMARY KEY,
    version             BIGINT              NOT NULL DEFAULT 0,
    property_id         BIGINT              NOT NULL REFERENCES property(property_id),
    amenity_id          BIGINT              NOT NULL REFERENCES amenity(amenity_id),
    created_at          TIMESTAMP           NOT NULL,
    updated_at          TIMESTAMP           NOT NULL
);

CREATE UNIQUE INDEX uk_property_amenity ON property_amenity(property_id, amenity_id);
CREATE INDEX idx_property_amenity_property ON property_amenity(property_id);
CREATE INDEX idx_property_amenity_amenity ON property_amenity(amenity_id);

CREATE TABLE property_policy (
    policy_id               BIGSERIAL       PRIMARY KEY,
    version                 BIGINT          NOT NULL DEFAULT 0,
    property_id             BIGINT          NOT NULL UNIQUE REFERENCES property(property_id),
    check_in_time           TIME            NOT NULL,
    check_out_time          TIME            NOT NULL,
    quiet_hours_start       TIME,
    quiet_hours_end         TIME,
    no_parties              BOOLEAN         DEFAULT FALSE,
    visitors_allowed        BOOLEAN         DEFAULT TRUE,
    pets_allowed            BOOLEAN         DEFAULT FALSE,
    smoking_allowed         BOOLEAN         DEFAULT FALSE,
    minimum_age             INT             DEFAULT 18,
    free_cancellation_hours INT             DEFAULT 48,
    extra_bed_available     BOOLEAN         DEFAULT FALSE,
    created_at              TIMESTAMP       NOT NULL,
    updated_at              TIMESTAMP       NOT NULL
);

CREATE UNIQUE INDEX uk_property_policy_property ON property_policy(property_id);

CREATE TABLE property_contact (
    contact_id          BIGSERIAL           PRIMARY KEY,
    version             BIGINT              NOT NULL DEFAULT 0,
    property_id         BIGINT              NOT NULL REFERENCES property(property_id),
    contact_name        VARCHAR(255)        NOT NULL,
    designation         VARCHAR(100),
    email               VARCHAR(255),
    phone               VARCHAR(20)         NOT NULL,
    is_primary          BOOLEAN             DEFAULT FALSE,
    created_at          TIMESTAMP           NOT NULL,
    updated_at          TIMESTAMP           NOT NULL
);

CREATE INDEX idx_property_contact_property ON property_contact(property_id);
CREATE INDEX idx_property_contact_primary ON property_contact(property_id, is_primary);

CREATE TABLE room (
    room_id             BIGSERIAL           PRIMARY KEY,
    public_id           VARCHAR(26)         NOT NULL,
    version             BIGINT              NOT NULL DEFAULT 0,
    property_id         BIGINT              NOT NULL REFERENCES property(property_id),
    room_number         VARCHAR(20),
    room_name           VARCHAR(255)        NOT NULL,
    description         TEXT,
    room_type           VARCHAR(50)         NOT NULL,
    capacity            INT                 NOT NULL,
    bed_count           INT                 NOT NULL,
    bed_type            VARCHAR(50)         NOT NULL,
    bathroom_count      INT,
    base_price          DECIMAL(12, 2)      NOT NULL,
    weekend_price       DECIMAL(12, 2),
    cleaning_fee        DECIMAL(12, 2),
    extra_guest_fee     DECIMAL(12, 2),
    area_sqft           INT,
    floor_number        INT,
    status              VARCHAR(20)         NOT NULL DEFAULT 'ACTIVE',
    created_at          TIMESTAMP           NOT NULL,
    updated_at          TIMESTAMP           NOT NULL
);

CREATE UNIQUE INDEX idx_room_public_id ON room(public_id);
CREATE INDEX idx_room_property_status ON room(property_id, status);
CREATE UNIQUE INDEX uk_room_property_number ON room(property_id, room_number);

CREATE TABLE room_image (
    room_image_id       BIGSERIAL           PRIMARY KEY,
    version             BIGINT              NOT NULL DEFAULT 0,
    room_id             BIGINT              NOT NULL REFERENCES room(room_id),
    image_url           VARCHAR(500)        NOT NULL,
    alt_text            VARCHAR(255),
    caption             VARCHAR(255),
    display_order       INT                 NOT NULL,
    is_thumbnail        BOOLEAN             DEFAULT FALSE,
    created_at          TIMESTAMP           NOT NULL,
    updated_at          TIMESTAMP           NOT NULL
);

CREATE UNIQUE INDEX idx_room_image_room_order ON room_image(room_id, display_order);
CREATE INDEX idx_room_image_room ON room_image(room_id);

CREATE TABLE room_availability (
    availability_id     BIGSERIAL           PRIMARY KEY,
    version             BIGINT              NOT NULL DEFAULT 0,
    room_id             BIGINT              NOT NULL REFERENCES room(room_id),
    start_date          DATE                NOT NULL,
    end_date            DATE                NOT NULL,
    status              VARCHAR(20)         NOT NULL DEFAULT 'AVAILABLE',
    reason              VARCHAR(255),
    created_by          VARCHAR(100)        NOT NULL,
    created_at          TIMESTAMP           NOT NULL,
    updated_at          TIMESTAMP           NOT NULL
);

CREATE INDEX idx_room_availability_room_dates ON room_availability(room_id, start_date, end_date);
CREATE INDEX idx_room_availability_room ON room_availability(room_id);
CREATE INDEX idx_room_availability_dates ON room_availability(start_date, end_date);
CREATE INDEX idx_room_availability_status ON room_availability(status);

-- ============================================================================
-- USER AGGREGATE
-- ============================================================================

CREATE TABLE "user" (
    user_id             BIGSERIAL           PRIMARY KEY,
    public_id           VARCHAR(26)         NOT NULL,
    version             BIGINT              NOT NULL DEFAULT 0,
    email               VARCHAR(255)        NOT NULL,
    password_hash       VARCHAR(255)        NOT NULL,
    first_name          VARCHAR(100)        NOT NULL,
    last_name           VARCHAR(100)        NOT NULL,
    phone               VARCHAR(20),
    profile_picture     VARCHAR(500),
    is_verified         BOOLEAN             DEFAULT FALSE,
    status              VARCHAR(20)         NOT NULL DEFAULT 'ACTIVE',
    last_login          TIMESTAMP,
    created_at          TIMESTAMP           NOT NULL,
    updated_at          TIMESTAMP           NOT NULL
);

CREATE UNIQUE INDEX idx_user_public_id ON "user"(public_id);
CREATE UNIQUE INDEX idx_user_email ON "user"(email);
CREATE INDEX idx_user_phone ON "user"(phone);
CREATE INDEX idx_user_status ON "user"(status);

CREATE TABLE role (
    role_id             BIGSERIAL           PRIMARY KEY,
    name                VARCHAR(50)         NOT NULL,
    description         VARCHAR(255)
);

CREATE UNIQUE INDEX idx_role_name ON role(name);

CREATE TABLE user_role (
    user_id             BIGINT              NOT NULL REFERENCES "user"(user_id),
    role_id             BIGINT              NOT NULL REFERENCES role(role_id),
    PRIMARY KEY (user_id, role_id)
);

CREATE INDEX idx_user_role_user ON user_role(user_id);
CREATE INDEX idx_user_role_role ON user_role(role_id);

CREATE TABLE refresh_token (
    refresh_token_id    BIGSERIAL           PRIMARY KEY,
    version             BIGINT              NOT NULL DEFAULT 0,
    user_id             BIGINT              NOT NULL REFERENCES "user"(user_id),
    token_hash          VARCHAR(255)        NOT NULL,
    expires_at          TIMESTAMP           NOT NULL,
    revoked             BOOLEAN             NOT NULL DEFAULT FALSE,
    device_name         VARCHAR(255),
    device_type         VARCHAR(50),
    created_at          TIMESTAMP           NOT NULL,
    updated_at          TIMESTAMP           NOT NULL
);

CREATE INDEX idx_refresh_token_user ON refresh_token(user_id);
CREATE INDEX idx_refresh_token_expires ON refresh_token(expires_at);
CREATE INDEX idx_refresh_token_revoked ON refresh_token(revoked);

CREATE TABLE user_preference (
    user_id                 BIGINT          PRIMARY KEY REFERENCES "user"(user_id),
    version                 BIGINT          NOT NULL DEFAULT 0,
    preferred_currency      VARCHAR(3)      DEFAULT 'INR',
    preferred_language      VARCHAR(10)     DEFAULT 'en',
    preferred_city          VARCHAR(100),
    theme                   VARCHAR(20)     DEFAULT 'LIGHT',
    marketing_emails        BOOLEAN         DEFAULT TRUE,
    email_notifications     BOOLEAN         DEFAULT TRUE,
    push_notifications      BOOLEAN         DEFAULT TRUE,
    sms_notifications       BOOLEAN         DEFAULT FALSE,
    created_at              TIMESTAMP       NOT NULL,
    updated_at              TIMESTAMP       NOT NULL
);

CREATE TABLE wishlist (
    wishlist_id         BIGSERIAL           PRIMARY KEY,
    version             BIGINT              NOT NULL DEFAULT 0,
    user_id             BIGINT              NOT NULL REFERENCES "user"(user_id),
    property_id         BIGINT              NOT NULL,
    created_at          TIMESTAMP           NOT NULL,
    updated_at          TIMESTAMP           NOT NULL
);

CREATE UNIQUE INDEX uk_wishlist_user_property ON wishlist(user_id, property_id);
CREATE INDEX idx_wishlist_user ON wishlist(user_id);
CREATE INDEX idx_wishlist_property ON wishlist(property_id);

CREATE TABLE user_activity (
    activity_id         BIGSERIAL           PRIMARY KEY,
    version             BIGINT              NOT NULL DEFAULT 0,
    user_id             BIGINT              NOT NULL REFERENCES "user"(user_id),
    activity_type       VARCHAR(50)         NOT NULL,
    entity_type         VARCHAR(50),
    entity_id           BIGINT,
    metadata            TEXT,
    ip_address          VARCHAR(45),
    user_agent          TEXT,
    city                VARCHAR(100),
    country             VARCHAR(100),
    created_at          TIMESTAMP           NOT NULL,
    updated_at          TIMESTAMP           NOT NULL
);

CREATE INDEX idx_user_activity_user ON user_activity(user_id);
CREATE INDEX idx_user_activity_type ON user_activity(activity_type);
CREATE INDEX idx_user_activity_entity ON user_activity(entity_type, entity_id);
CREATE INDEX idx_user_activity_created ON user_activity(created_at);

CREATE TABLE search_history (
    search_id           BIGSERIAL           PRIMARY KEY,
    version             BIGINT              NOT NULL DEFAULT 0,
    user_id             BIGINT              NOT NULL REFERENCES "user"(user_id),
    city                VARCHAR(100),
    check_in            DATE,
    check_out           DATE,
    guests              INT                 NOT NULL,
    filters             TEXT,
    created_at          TIMESTAMP           NOT NULL,
    updated_at          TIMESTAMP           NOT NULL
);

CREATE INDEX idx_search_history_user ON search_history(user_id);
CREATE INDEX idx_search_history_created ON search_history(created_at);

-- ============================================================================
-- BOOKING AGGREGATE
-- ============================================================================

CREATE TABLE booking (
    booking_id              BIGSERIAL       PRIMARY KEY,
    public_id               VARCHAR(26)     NOT NULL,
    version                 BIGINT          NOT NULL DEFAULT 0,
    booking_reference       VARCHAR(20)     NOT NULL,
    user_id                 BIGINT          NOT NULL,
    property_id             BIGINT          NOT NULL,
    room_id                 BIGINT,
    booking_type            VARCHAR(20)     NOT NULL,
    check_in                DATE            NOT NULL,
    check_out               DATE            NOT NULL,
    guest_count             INT             NOT NULL,
    booking_status          VARCHAR(20)     NOT NULL DEFAULT 'PENDING',
    payment_status          VARCHAR(20)     NOT NULL DEFAULT 'UNPAID',
    booking_source          VARCHAR(20)     NOT NULL DEFAULT 'WEBSITE',
    special_requests        TEXT,
    base_price_snapshot     DECIMAL(12, 2),
    cleaning_fee_snapshot   DECIMAL(12, 2),
    tax_snapshot            DECIMAL(12, 2),
    discount_snapshot       DECIMAL(12, 2),
    total_amount            DECIMAL(12, 2)  NOT NULL,
    created_at              TIMESTAMP       NOT NULL,
    updated_at              TIMESTAMP       NOT NULL
);

CREATE UNIQUE INDEX idx_booking_public_id ON booking(public_id);
CREATE UNIQUE INDEX idx_booking_ref ON booking(booking_reference);
CREATE INDEX idx_booking_user ON booking(user_id);
CREATE INDEX idx_booking_property ON booking(property_id);
CREATE INDEX idx_booking_status ON booking(booking_status);
CREATE INDEX idx_booking_dates ON booking(check_in, check_out);

CREATE TABLE booking_guest (
    guest_id            BIGSERIAL           PRIMARY KEY,
    version             BIGINT              NOT NULL DEFAULT 0,
    booking_id          BIGINT              NOT NULL REFERENCES booking(booking_id),
    full_name           VARCHAR(255)        NOT NULL,
    age                 INT,
    gender              VARCHAR(10),
    government_id       VARCHAR(50),
    is_primary_guest    BOOLEAN             DEFAULT FALSE,
    created_at          TIMESTAMP           NOT NULL,
    updated_at          TIMESTAMP           NOT NULL
);

CREATE INDEX idx_booking_guest_booking ON booking_guest(booking_id);

CREATE TABLE payment (
    payment_id              BIGSERIAL       PRIMARY KEY,
    version                 BIGINT          NOT NULL DEFAULT 0,
    booking_id              BIGINT          NOT NULL REFERENCES booking(booking_id),
    amount                  DECIMAL(12, 2)  NOT NULL,
    currency                VARCHAR(3)      NOT NULL DEFAULT 'INR',
    payment_method          VARCHAR(50)     NOT NULL,
    provider                VARCHAR(50)     NOT NULL,
    provider_payment_id     VARCHAR(255),
    provider_order_id       VARCHAR(255),
    transaction_reference   VARCHAR(255),
    status                  VARCHAR(20)     NOT NULL DEFAULT 'PENDING',
    paid_at                 TIMESTAMP,
    created_at              TIMESTAMP       NOT NULL,
    updated_at              TIMESTAMP       NOT NULL
);

CREATE INDEX idx_payment_booking ON payment(booking_id);
CREATE INDEX idx_payment_status ON payment(status);

CREATE TABLE payment_transaction (
    transaction_id          BIGSERIAL       PRIMARY KEY,
    version                 BIGINT          NOT NULL DEFAULT 0,
    payment_id              BIGINT          NOT NULL REFERENCES payment(payment_id),
    gateway                 VARCHAR(50)     NOT NULL,
    gateway_transaction_id  VARCHAR(255),
    gateway_response        TEXT,
    status                  VARCHAR(20)     NOT NULL,
    created_at              TIMESTAMP       NOT NULL,
    updated_at              TIMESTAMP       NOT NULL
);

CREATE INDEX idx_payment_txn_payment ON payment_transaction(payment_id);
CREATE INDEX idx_payment_txn_gateway ON payment_transaction(gateway);
CREATE INDEX idx_payment_txn_status ON payment_transaction(status);

CREATE TABLE refund (
    refund_id           BIGSERIAL           PRIMARY KEY,
    version             BIGINT              NOT NULL DEFAULT 0,
    payment_id          BIGINT              NOT NULL REFERENCES payment(payment_id),
    refund_reference    VARCHAR(50)         NOT NULL,
    amount              DECIMAL(12, 2)      NOT NULL,
    reason              VARCHAR(500)        NOT NULL,
    status              VARCHAR(20)         NOT NULL DEFAULT 'PENDING',
    processed_at        TIMESTAMP,
    created_at          TIMESTAMP           NOT NULL,
    updated_at          TIMESTAMP           NOT NULL
);

CREATE UNIQUE INDEX idx_refund_reference ON refund(refund_reference);
CREATE INDEX idx_refund_payment ON refund(payment_id);
CREATE INDEX idx_refund_status ON refund(status);

CREATE TABLE invoice (
    invoice_id          BIGSERIAL           PRIMARY KEY,
    version             BIGINT              NOT NULL DEFAULT 0,
    booking_id          BIGINT              NOT NULL REFERENCES booking(booking_id),
    invoice_number      VARCHAR(50)         NOT NULL,
    pdf_url             VARCHAR(500),
    subtotal            DECIMAL(12, 2)      NOT NULL,
    tax                 DECIMAL(12, 2),
    discount            DECIMAL(12, 2),
    grand_total         DECIMAL(12, 2)      NOT NULL,
    status              VARCHAR(20)         NOT NULL DEFAULT 'PENDING',
    issued_at           TIMESTAMP,
    created_at          TIMESTAMP           NOT NULL,
    updated_at          TIMESTAMP           NOT NULL
);

CREATE UNIQUE INDEX idx_invoice_number ON invoice(invoice_number);
CREATE INDEX idx_invoice_booking ON invoice(booking_id);

CREATE TABLE booking_timeline (
    timeline_id         BIGSERIAL           PRIMARY KEY,
    version             BIGINT              NOT NULL DEFAULT 0,
    booking_id          BIGINT              NOT NULL REFERENCES booking(booking_id),
    event_type          VARCHAR(50)         NOT NULL,
    performed_by        VARCHAR(100)        NOT NULL,
    remarks             TEXT,
    created_at          TIMESTAMP           NOT NULL,
    updated_at          TIMESTAMP           NOT NULL
);

CREATE INDEX idx_booking_timeline_booking ON booking_timeline(booking_id);
CREATE INDEX idx_booking_timeline_created ON booking_timeline(booking_id, created_at);

-- ============================================================================
-- REVIEW AGGREGATE
-- ============================================================================

CREATE TABLE review (
    review_id           BIGSERIAL           PRIMARY KEY,
    public_id           VARCHAR(26)         NOT NULL,
    version             BIGINT              NOT NULL DEFAULT 0,
    booking_id          BIGINT              NOT NULL,
    property_id         BIGINT              NOT NULL,
    user_id             BIGINT              NOT NULL,
    rating              DECIMAL(2, 1)       NOT NULL,
    title               VARCHAR(255),
    review_text         TEXT,
    status              VARCHAR(20)         NOT NULL DEFAULT 'PENDING',
    created_at          TIMESTAMP           NOT NULL,
    updated_at          TIMESTAMP           NOT NULL
);

CREATE UNIQUE INDEX idx_review_public_id ON review(public_id);
CREATE UNIQUE INDEX idx_review_booking ON review(booking_id);
CREATE INDEX idx_review_property ON review(property_id);
CREATE INDEX idx_review_user ON review(user_id);
CREATE INDEX idx_review_status ON review(status);

CREATE TABLE review_image (
    review_image_id     BIGSERIAL           PRIMARY KEY,
    version             BIGINT              NOT NULL DEFAULT 0,
    review_id           BIGINT              NOT NULL REFERENCES review(review_id),
    image_url           VARCHAR(500)        NOT NULL,
    alt_text            VARCHAR(255),
    display_order       INT                 NOT NULL,
    created_at          TIMESTAMP           NOT NULL,
    updated_at          TIMESTAMP           NOT NULL
);

CREATE INDEX idx_review_image_review ON review_image(review_id);
CREATE UNIQUE INDEX idx_review_image_order ON review_image(review_id, display_order);

-- ============================================================================
-- NOTIFICATION AGGREGATE
-- ============================================================================

CREATE TABLE notification_template (
    template_id         BIGSERIAL           PRIMARY KEY,
    version             BIGINT              NOT NULL DEFAULT 0,
    name                VARCHAR(100)        NOT NULL,
    channel             VARCHAR(20)         NOT NULL,
    subject             VARCHAR(255),
    body                TEXT                NOT NULL,
    variables           TEXT,
    created_at          TIMESTAMP           NOT NULL,
    updated_at          TIMESTAMP           NOT NULL
);

CREATE UNIQUE INDEX idx_notification_template_name ON notification_template(name);
CREATE INDEX idx_notification_template_channel ON notification_template(channel);

CREATE TABLE notification (
    notification_id     BIGSERIAL           PRIMARY KEY,
    version             BIGINT              NOT NULL DEFAULT 0,
    user_id             BIGINT              NOT NULL,
    template_id         BIGINT              NOT NULL,
    channel             VARCHAR(20)         NOT NULL,
    recipient           VARCHAR(255)        NOT NULL,
    status              VARCHAR(20)         NOT NULL DEFAULT 'PENDING',
    scheduled_at        TIMESTAMP,
    sent_at             TIMESTAMP,
    rendered_body       TEXT,
    created_at          TIMESTAMP           NOT NULL,
    updated_at          TIMESTAMP           NOT NULL
);

CREATE INDEX idx_notification_user ON notification(user_id);
CREATE INDEX idx_notification_template ON notification(template_id);
CREATE INDEX idx_notification_status ON notification(status);
CREATE INDEX idx_notification_scheduled ON notification(scheduled_at);

CREATE TABLE notification_log (
    log_id              BIGSERIAL           PRIMARY KEY,
    version             BIGINT              NOT NULL DEFAULT 0,
    notification_id     BIGINT              NOT NULL,
    provider            VARCHAR(50)         NOT NULL,
    provider_response   TEXT,
    status              VARCHAR(20)         NOT NULL,
    created_at          TIMESTAMP           NOT NULL,
    updated_at          TIMESTAMP           NOT NULL
);

CREATE INDEX idx_notification_log_notification ON notification_log(notification_id);
CREATE INDEX idx_notification_log_status ON notification_log(status);
CREATE INDEX idx_notification_log_provider ON notification_log(provider);
