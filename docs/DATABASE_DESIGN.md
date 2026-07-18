# DriftStay Database Architecture v1.0

> **Document Version:** 1.0  
> **Last Updated:** July 18, 2026  
> **Status:** Finalized

---

## Table of Contents

1. [Design Principles](#1-design-principles)
2. [Aggregate Overview](#2-aggregate-overview)
3. [Property Aggregate](#3-property-aggregate)
4. [User Aggregate](#4-user-aggregate)
5. [Booking Aggregate](#5-booking-aggregate)
6. [Review Aggregate](#6-review-aggregate)
7. [Notification Aggregate](#7-notification-aggregate)
8. [Enums & Lookup Tables](#8-enums--lookup-tables)
9. [Indexing Strategy](#9-indexing-strategy)
10. [Entity-Relationship Summary](#10-entity-relationship-summary)

---

## 1. Design Principles

The entire schema is designed around the following principles:

### 1.1 ACID Compliance

All critical business operations are wrapped in database transactions to ensure atomicity, consistency, isolation, and durability. The following operations are transactional:

- **User Registration** — Creates user record, assigns default role, initializes preferences
- **Booking Creation** — Validates availability, creates booking, updates room availability
- **Booking Cancellation** — Updates booking status, releases availability, initiates refund workflow
- **Payment Completion** — Records payment, updates booking payment status, creates invoice
- **Refund Processing** — Creates refund record, updates payment status, logs transaction

Each operation either completes entirely or rolls back completely, with no partial state.

### 1.2 Third Normal Form (3NF)

The schema follows 3NF with strict adherence:

| Rule | Application |
|------|-------------|
| **1NF** — Atomic columns, no repeating groups | Each column holds a single value; amenities are modeled as separate rows in a bridge table |
| **2NF** — No partial dependencies on composite keys | All non-key columns depend on the full primary key |
| **3NF** — No transitive dependencies | Derived data like `average_rating` is explicitly managed through controlled denormalization |

**Example:** Rather than storing amenity flags (`wifi`, `parking`, `gym`, `pool`) as columns on the `property` table, we use:

```
Property ── PropertyAmenity ── Amenity
```

This allows amenities to be added dynamically without schema changes and enables efficient querying.

### 1.3 Controlled Denormalization

Frequently queried computed values are cached on the parent entity to avoid expensive aggregate queries:

| Column | Parent Table | Source | Updated When |
|--------|-------------|--------|--------------|
| `average_rating` | `property` | `AVG(review.rating)` | Review created/updated/deleted |
| `review_count` | `property` | `COUNT(review.review_id)` | Review created/deleted |
| `booking_count` | `property` | `COUNT(booking.booking_id)` | Booking confirmed |

These values are updated **transactionally** within the same database transaction that modifies the source data, ensuring consistency.

### 1.4 Aggregate-Oriented Design (DDD)

The schema is divided into bounded contexts, each owned by a single service:

| Aggregate | Owner Service | Description |
|-----------|--------------|-------------|
| **Property** | `PropertyService` | Properties, rooms, amenities, policies |
| **User** | `UserService` | Users, roles, authentication, preferences |
| **Booking** | `BookingService` | Bookings, payments, invoices, timeline |
| **Review** | `ReviewService` | Reviews, review images |
| **Notification** | `NotificationService` | Notifications, templates, logs |

No service directly owns or modifies another aggregate's internal tables. Cross-aggregate communication happens through service calls, not direct data access.

### 1.5 Auditability

Every transactional table contains:

- `created_at` — Timestamp of record creation (populated automatically via JPA Auditing)
- `updated_at` — Timestamp of last modification (populated automatically via JPA Auditing)

Critical business tables additionally maintain immutable history through timeline/event tables:

- **Booking** → `booking_timeline` (append-only event log)
- **Payment** → `payment_transaction` (immutable gateway logs)
- **Notification** → `notification_log` (immutable delivery history)

### 1.6 Soft Delete Strategy

Critical records are never physically deleted. Instead, a `status` or `deleted_at` column is used:

| Table | Strategy | Values |
|-------|----------|--------|
| `property` | `status` ENUM | `ACTIVE`, `INACTIVE`, `ARCHIVED`, `DELETED` |
| `room` | `status` ENUM | `ACTIVE`, `INACTIVE`, `MAINTENANCE`, `DELETED` |
| `booking` | `booking_status` ENUM | `PENDING`, `CONFIRMED`, `CHECKED_IN`, `CHECKED_OUT`, `CANCELLED`, `NO_SHOW` |
| `review` | `status` ENUM | `PENDING`, `APPROVED`, `REJECTED`, `FLAGGED` |
| `user` | `status` ENUM | `ACTIVE`, `INACTIVE`, `SUSPENDED`, `DELETED` |

This preserves historical integrity and enables undo operations where appropriate.

---

## 2. Aggregate Overview

```
┌─────────────────────────────┐
│     PROPERTY AGGREGATE      │
│  ┌──────────┐               │
│  │ Property │──┐            │
│  └──────────┘  │            │
│        │       │            │
│  ┌─────┴──────┐│            │
│  │  Room      ││            │
│  └─────┬──────┘│            │
│        │       │            │
│  ┌─────┴──────┐│            │
│  │RoomImage   ││            │
│  └────────────┘│            │
│        │       │            │
│  ┌─────┴──────┐│            │
│  │RoomAvail.  ││            │
│  └────────────┘│            │
│        │       │            │
│  ┌─────┴──────┐│            │
│  │Property-   ││            │
│  │Image       ││            │
│  └────────────┘│            │
│        │       │            │
│  ┌─────┴──────┐│   ┌──────┐│
│  │Property-   │├───│Amenity││
│  │Amenity     ││   └──────┘│
│  └────────────┘│           │
│        │       │           │
│  ┌─────┴──────┐│           │
│  │Property-   ││           │
│  │Policy      ││           │
│  └────────────┘│           │
│        │       │           │
│  ┌─────┴──────┐│           │
│  │Property-   ││           │
│  │Contact     ││           │
│  └────────────┘│           │
└────────────────┴───────────┘

┌──────────────────────────┐
│     USER AGGREGATE       │
│  ┌──────────┐            │
│  │   User   │──┐         │
│  └──────────┘  │         │
│        │       │ ┌──────┐│
│        ├───────┼─│ Role ││
│        │       │ └──────┘│
│  ┌─────┴──────┐│         │
│  │ UserRole   ││         │
│  └────────────┘│         │
│        │       │         │
│  ┌─────┴──────┐│         │
│  │RefreshToken││         │
│  └────────────┘│         │
│        │       │         │
│  ┌─────┴──────┐│         │
│  │UserPref.   ││         │
│  └────────────┘│         │
│        │       │         │
│  ┌─────┴──────┐│         │
│  │UserActivity││         │
│  └────────────┘│         │
│        │       │         │
│  ┌─────┴──────┐│         │
│  │ Wishlist   ││         │
│  └────────────┘│         │
└────────────────┴─────────┘

┌──────────────────────────────┐
│     BOOKING AGGREGATE        │
│  ┌──────────┐                │
│  │ Booking  │──┐             │
│  └──────────┘  │             │
│        │       │             │
│  ┌─────┴──────┐│ ┌──────────┐│
│  │BookingGuest││ │ Invoice  ││
│  └────────────┘│ └──────────┘│
│        │       │             │
│  ┌─────┴──────┐│ ┌──────────┐│
│  │  Payment   │├─│ Refund   ││
│  └─────┬──────┘│ └──────────┘│
│        │       │             │
│  ┌─────┴──────┐│ ┌──────────┐│
│  │PaymentTx   ││ │Booking-  ││
│  └────────────┘│ │Timeline  ││
│                │ └──────────┘│
└────────────────┴─────────────┘

┌──────────────────────────────┐
│    REVIEW AGGREGATE          │
│  ┌──────────┐                │
│  │  Review  │──┐             │
│  └──────────┘  │             │
│        │       │             │
│  ┌─────┴──────┐│             │
│  │ReviewImage ││             │
│  └────────────┘│             │
└────────────────┴─────────────┘

┌──────────────────────────────────┐
│    NOTIFICATION AGGREGATE        │
│  ┌──────────────┐                │
│  │Notification  │──┐             │
│  └──────────────┘  │             │
│        │           │ ┌──────────┐│
│  ┌─────┴──────────┐│ │Notify-   ││
│  │NotificationLog ││ │Template  ││
│  └────────────────┘│ └──────────┘│
└────────────────────┴─────────────┘
```

---

## 3. Property Aggregate

The Property aggregate is the largest and most complex aggregate. It represents accommodations — villas, cottages, apartments, resorts, and entire homestays — along with their rooms, amenities, policies, and availability.

### 3.1 Property

The root entity of the Property aggregate. Represents an accommodation listing.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `property_id` | `BIGSERIAL` | `PK` | Primary identifier |
| `slug` | `VARCHAR(255)` | `UNIQUE`, `NOT NULL` | URL-friendly identifier (e.g., "sea-view-villa-goa") |
| `name` | `VARCHAR(255)` | `NOT NULL` | Display name of the property |
| `short_description` | `VARCHAR(500)` | | Brief tagline for cards |
| `description` | `TEXT` | | Detailed description |
| `property_type` | `VARCHAR(50)` | `NOT NULL` | e.g., VILLA, COTTAGE, APARTMENT, RESORT, HOMESTAY |
| `star_category` | `INT` | `CHECK(1-5)` | Star rating category |
| `status` | `VARCHAR(20)` | `NOT NULL`, `DEFAULT 'ACTIVE'` | ACTIVE, INACTIVE, ARCHIVED, DELETED |
| `address_line_1` | `VARCHAR(255)` | `NOT NULL` | Street address |
| `address_line_2` | `VARCHAR(255)` | | Apartment/suite number |
| `city` | `VARCHAR(100)` | `NOT NULL` | City |
| `state` | `VARCHAR(100)` | `NOT NULL` | State/Province |
| `country` | `VARCHAR(100)` | `NOT NULL` | Country |
| `postal_code` | `VARCHAR(20)` | | ZIP/Postal code |
| `latitude` | `DECIMAL(10,7)` | | For geospatial search |
| `longitude` | `DECIMAL(10,7)` | | For geospatial search |
| `average_rating` | `DECIMAL(2,1)` | `DEFAULT 0.0` | Cached average rating |
| `review_count` | `INT` | `DEFAULT 0` | Cached count of reviews |
| `booking_count` | `INT` | `DEFAULT 0` | Cached count of confirmed bookings |
| `created_at` | `TIMESTAMP` | `NOT NULL` | Auto-populated |
| `updated_at` | `TIMESTAMP` | `NOT NULL` | Auto-populated |

**Indexes:**
- `UNIQUE` on `slug` — for fast URL lookups
- `INDEX` on `(latitude, longitude)` — for geospatial proximity queries
- `INDEX` on `city` — for location-based filtering
- `INDEX` on `status` — for filtering active properties
- `INDEX` on `(status, city)` — for common query patterns

### 3.2 PropertyImage

Gallery images associated with a property.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `image_id` | `BIGSERIAL` | `PK` | Primary identifier |
| `property_id` | `BIGINT` | `FK → property.property_id`, `NOT NULL` | Parent property |
| `image_url` | `VARCHAR(500)` | `NOT NULL` | S3/Cloud storage URL |
| `caption` | `VARCHAR(255)` | | Image caption |
| `display_order` | `INT` | `NOT NULL` | Sorting order |
| `is_thumbnail` | `BOOLEAN` | `DEFAULT FALSE` | Whether this is the primary thumbnail |
| `created_at` | `TIMESTAMP` | `NOT NULL` | Auto-populated |

**Indexes:** `UNIQUE(property_id, display_order)`

### 3.3 Amenity

A lookup table of available amenities.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `amenity_id` | `BIGSERIAL` | `PK` | Primary identifier |
| `name` | `VARCHAR(100)` | `UNIQUE`, `NOT NULL` | e.g., "Free WiFi", "Swimming Pool" |
| `icon` | `VARCHAR(255)` | | Icon class/URL |
| `category` | `VARCHAR(50)` | | e.g., "BASIC", "LUXURY", "SAFETY" |
| `created_at` | `TIMESTAMP` | `NOT NULL` | Auto-populated |

### 3.4 PropertyAmenity

Bridge table joining properties with amenities (many-to-many).

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `property_id` | `BIGINT` | `FK → property.property_id`, `NOT NULL` | Parent property |
| `amenity_id` | `BIGINT` | `FK → amenity.amenity_id`, `NOT NULL` | Associated amenity |

**Primary Key:** Composite `(property_id, amenity_id)`

### 3.5 PropertyPolicy

One-to-one relationship with property for stay policies.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `policy_id` | `BIGSERIAL` | `PK` | Primary identifier |
| `property_id` | `BIGINT` | `FK → property.property_id`, `UNIQUE`, `NOT NULL` | Parent property (one-to-one) |
| `check_in_time` | `TIME` | `NOT NULL` | Standard check-in time |
| `check_out_time` | `TIME` | `NOT NULL` | Standard check-out time |
| `pets_allowed` | `BOOLEAN` | `DEFAULT FALSE` | Whether pets are permitted |
| `smoking_allowed` | `BOOLEAN` | `DEFAULT FALSE` | Whether smoking is permitted |
| `minimum_age` | `INT` | `DEFAULT 18` | Minimum guest age |
| `free_cancellation_hours` | `INT` | `DEFAULT 48` | Cancellation window in hours |
| `extra_bed_available` | `BOOLEAN` | `DEFAULT FALSE` | Whether extra beds can be arranged |
| `created_at` | `TIMESTAMP` | `NOT NULL` | Auto-populated |
| `updated_at` | `TIMESTAMP` | `NOT NULL` | Auto-populated |

### 3.6 PropertyContact

Contact information for property management.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `contact_id` | `BIGSERIAL` | `PK` | Primary identifier |
| `property_id` | `BIGINT` | `FK → property.property_id`, `NOT NULL` | Parent property |
| `contact_name` | `VARCHAR(255)` | `NOT NULL` | Contact person name |
| `designation` | `VARCHAR(100)` | | Job title |
| `email` | `VARCHAR(255)` | | Email address |
| `phone` | `VARCHAR(20)` | `NOT NULL` | Phone number |
| `is_primary` | `BOOLEAN` | `DEFAULT FALSE` | Whether this is the primary contact |
| `created_at` | `TIMESTAMP` | `NOT NULL` | Auto-populated |

### 3.7 Room

Represents bookable rooms within a property. Supports both whole-property and individual room booking.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `room_id` | `BIGSERIAL` | `PK` | Primary identifier |
| `property_id` | `BIGINT` | `FK → property.property_id`, `NOT NULL` | Parent property |
| `room_number` | `VARCHAR(20)` | | Room number/identifier |
| `room_name` | `VARCHAR(255)` | `NOT NULL` | e.g., "Ocean Suite", "Garden View" |
| `description` | `TEXT` | | Room description |
| `room_type` | `VARCHAR(50)` | `NOT NULL` | e.g., STANDARD, DELUXE, SUITE, PENTHOUSE |
| `capacity` | `INT` | `NOT NULL` | Maximum occupancy (adults) |
| `bed_count` | `INT` | `NOT NULL` | Number of beds |
| `bed_type` | `VARCHAR(50)` | `NOT NULL` | e.g., KING, QUEEN, TWIN, SINGLE, SOFA_BED |
| `bathroom_count` | `INT` | `DEFAULT 1` | Number of bathrooms |
| `base_price` | `DECIMAL(10,2)` | `NOT NULL` | Standard nightly rate |
| `weekend_price` | `DECIMAL(10,2)` | | Weekend/nightly rate |
| `cleaning_fee` | `DECIMAL(10,2)` | `DEFAULT 0` | One-time cleaning fee |
| `extra_guest_fee` | `DECIMAL(10,2)` | `DEFAULT 0` | Per-guest fee beyond capacity |
| `area_sqft` | `INT` | | Room area in square feet |
| `floor_number` | `INT` | | Which floor the room is on |
| `status` | `VARCHAR(20)` | `NOT NULL`, `DEFAULT 'ACTIVE'` | ACTIVE, INACTIVE, MAINTENANCE, DELETED |
| `created_at` | `TIMESTAMP` | `NOT NULL` | Auto-populated |
| `updated_at` | `TIMESTAMP` | `NOT NULL` | Auto-populated |

**Indexes:**
- `UNIQUE(property_id, room_number)` — prevents duplicate room numbers within a property
- `INDEX(property_id, status)` — for active room queries

### 3.8 RoomImage

Images associated with a specific room.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `room_image_id` | `BIGSERIAL` | `PK` | Primary identifier |
| `room_id` | `BIGINT` | `FK → room.room_id`, `NOT NULL` | Parent room |
| `image_url` | `VARCHAR(500)` | `NOT NULL` | S3/Cloud storage URL |
| `caption` | `VARCHAR(255)` | | Image caption |
| `display_order` | `INT` | `NOT NULL` | Sorting order |
| `is_thumbnail` | `BOOLEAN` | `DEFAULT FALSE` | Whether this is the primary image |

**Indexes:** `UNIQUE(room_id, display_order)`

### 3.9 RoomAvailability

Tracks room availability for specific date ranges. Supports owner blocks, maintenance periods, and reservations.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `availability_id` | `BIGSERIAL` | `PK` | Primary identifier |
| `room_id` | `BIGINT` | `FK → room.room_id`, `NOT NULL` | Parent room |
| `start_date` | `DATE` | `NOT NULL` | Start of period |
| `end_date` | `DATE` | `NOT NULL` | End of period |
| `status` | `VARCHAR(20)` | `NOT NULL` | AVAILABLE, BLOCKED, MAINTENANCE, RESERVED |
| `reason` | `VARCHAR(255)` | | Reason for block/maintenance |
| `created_by` | `VARCHAR(100)` | `NOT NULL` | Who created this record |
| `created_at` | `TIMESTAMP` | `NOT NULL` | Auto-populated |

**Indexes:** `INDEX(room_id, start_date, end_date)` — for availability range queries

---

## 4. User Aggregate

The User aggregate handles authentication, authorization, user preferences, and activity tracking.

### 4.1 User

Core user entity. Stores authentication credentials and profile information.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `user_id` | `BIGSERIAL` | `PK` | Primary identifier |
| `email` | `VARCHAR(255)` | `UNIQUE`, `NOT NULL` | Login email |
| `password_hash` | `VARCHAR(255)` | `NOT NULL` | BCrypt hashed password |
| `first_name` | `VARCHAR(100)` | `NOT NULL` | First name |
| `last_name` | `VARCHAR(100)` | `NOT NULL` | Last name |
| `phone` | `VARCHAR(20)` | | Phone number |
| `profile_picture` | `VARCHAR(500)` | | Profile image URL |
| `is_verified` | `BOOLEAN` | `DEFAULT FALSE` | Email verification status |
| `status` | `VARCHAR(20)` | `NOT NULL`, `DEFAULT 'ACTIVE'` | ACTIVE, INACTIVE, SUSPENDED, DELETED |
| `last_login` | `TIMESTAMP` | | Last successful login timestamp |
| `created_at` | `TIMESTAMP` | `NOT NULL` | Auto-populated |
| `updated_at` | `TIMESTAMP` | `NOT NULL` | Auto-populated |

**Security Note:** Only `password_hash` is stored — never plain-text passwords.

**Indexes:**
- `UNIQUE(email)` — for fast login lookups
- `INDEX(phone)` — for support lookups

### 4.2 Role

Lookup table for user roles.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `role_id` | `BIGSERIAL` | `PK` | Primary identifier |
| `name` | `VARCHAR(50)` | `UNIQUE`, `NOT NULL` | e.g., CUSTOMER, ADMIN, PROPERTY_MANAGER, SUPER_ADMIN |
| `description` | `VARCHAR(255)` | | Human-readable description |

### 4.3 UserRole

Bridge table for many-to-many user-role assignment.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `user_id` | `BIGINT` | `FK → user.user_id`, `NOT NULL` | User reference |
| `role_id` | `BIGINT` | `FK → role.role_id`, `NOT NULL` | Role reference |

**Primary Key:** Composite `(user_id, role_id)`

### 4.4 RefreshToken

Stores hashed refresh tokens for JWT authentication. Tokens are stored as hashes, never in plain text.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `refresh_token_id` | `BIGSERIAL` | `PK` | Primary identifier |
| `user_id` | `BIGINT` | `FK → user.user_id`, `NOT NULL` | Token owner |
| `token_hash` | `VARCHAR(255)` | `NOT NULL` | SHA-256 hash of the actual token |
| `expires_at` | `TIMESTAMP` | `NOT NULL` | Token expiration timestamp |
| `revoked` | `BOOLEAN` | `DEFAULT FALSE` | Whether token has been revoked |
| `device_name` | `VARCHAR(255)` | | Device friendly name |
| `device_type` | `VARCHAR(50)` | | e.g., MOBILE, TABLET, DESKTOP |
| `created_at` | `TIMESTAMP` | `NOT NULL` | Auto-populated |

**Security Note:** Store SHA-256 hash of the raw token, never the raw token itself.

### 4.5 UserPreference

One-to-one relationship storing user preferences.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `user_id` | `BIGINT` | `PK`, `FK → user.user_id` | Primary key and user reference (one-to-one) |
| `preferred_currency` | `VARCHAR(3)` | `DEFAULT 'INR'` | e.g., INR, USD, EUR |
| `preferred_language` | `VARCHAR(10)` | `DEFAULT 'en'` | e.g., en, hi, fr |
| `preferred_city` | `VARCHAR(100)` | | Default search city |
| `theme` | `VARCHAR(20)` | `DEFAULT 'LIGHT'` | LIGHT, DARK, SYSTEM |
| `marketing_emails` | `BOOLEAN` | `DEFAULT TRUE` | Opt-in marketing |
| `email_notifications` | `BOOLEAN` | `DEFAULT TRUE` | Transactional emails |
| `push_notifications` | `BOOLEAN` | `DEFAULT TRUE` | Push notifications |
| `sms_notifications` | `BOOLEAN` | `DEFAULT FALSE` | SMS notifications |

### 4.6 Wishlist

User's saved/favorite properties.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `wishlist_id` | `BIGSERIAL` | `PK` | Primary identifier |
| `user_id` | `BIGINT` | `FK → user.user_id`, `NOT NULL` | User reference |
| `property_id` | `BIGINT` | `FK → property.property_id`, `NOT NULL` | Property reference |
| `created_at` | `TIMESTAMP` | `NOT NULL` | Auto-populated |

**Indexes:** `UNIQUE(user_id, property_id)` — prevents duplicate saves

### 4.7 UserActivity

Append-only analytics table for tracking user behavior.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `activity_id` | `BIGSERIAL` | `PK` | Primary identifier |
| `user_id` | `BIGINT` | `FK → user.user_id`, `NOT NULL` | User reference |
| `activity_type` | `VARCHAR(50)` | `NOT NULL` | e.g., PAGE_VIEW, SEARCH, BOOKING_STARTED |
| `entity_type` | `VARCHAR(50)` | | Entity type being acted upon |
| `entity_id` | `BIGINT` | | Entity ID being acted upon |
| `ip_address` | `VARCHAR(45)` | | User's IP address (supports IPv6) |
| `user_agent` | `TEXT` | | Browser user agent string |
| `city` | `VARCHAR(100)` | | Coarse location from IP |
| `country` | `VARCHAR(100)` | | Coarse location from IP |
| `created_at` | `TIMESTAMP` | `NOT NULL` | Auto-populated |

**Purpose:** Analytics, recommendation engine, fraud detection, security auditing.

**Privacy Note:** We intentionally store only coarse location (city, country) derived from IP address at the time of activity. Continuous GPS history is **not** collected. For personalized recommendations, the application should request the user's current device location at query time rather than relying on stored historical GPS data. This provides more accurate suggestions while respecting user privacy.

---

## 5. Booking Aggregate

The Booking aggregate is the core business domain. It manages the entire booking lifecycle including payments, invoices, refunds, and event history.

### 5.1 Booking

The root entity of the Booking aggregate. Supports both entire-property and individual room bookings.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `booking_id` | `BIGSERIAL` | `PK` | Primary identifier |
| `booking_reference` | `VARCHAR(20)` | `UNIQUE`, `NOT NULL` | Human-readable reference (e.g., "DRF-2026-00001") |
| `user_id` | `BIGINT` | `FK → user.user_id`, `NOT NULL` | Customer reference |
| `property_id` | `BIGINT` | `FK → property.property_id`, `NOT NULL` | Property reference |
| `room_id` | `BIGINT` | `FK → room.room_id`, `NULLABLE` | Room reference (NULL = entire property) |
| `booking_type` | `VARCHAR(20)` | `NOT NULL` | PROPERTY (entire property) or ROOM (individual room) |
| `check_in` | `DATE` | `NOT NULL` | Check-in date |
| `check_out` | `DATE` | `NOT NULL` | Check-out date |
| `guest_count` | `INT` | `NOT NULL` | Number of guests |
| `booking_status` | `VARCHAR(20)` | `NOT NULL`, `DEFAULT 'PENDING'` | PENDING, CONFIRMED, CHECKED_IN, CHECKED_OUT, CANCELLED, NO_SHOW |
| `payment_status` | `VARCHAR(20)` | `NOT NULL`, `DEFAULT 'UNPAID'` | UNPAID, PARTIALLY_PAID, PAID, REFUNDED, PARTIALLY_REFUNDED |
| `booking_source` | `VARCHAR(20)` | `NOT NULL`, `DEFAULT 'WEBSITE'` | WEBSITE, MOBILE_APP, ADMIN, AGENCY |
| `special_requests` | `TEXT` | | Guest special requests |
| `total_amount` | `DECIMAL(10,2)` | `NOT NULL` | Total booking amount |
| `created_at` | `TIMESTAMP` | `NOT NULL` | Auto-populated |
| `updated_at` | `TIMESTAMP` | `NOT NULL` | Auto-populated |

**Design Note:** `room_id` is nullable. When `NULL`, the entire property is booked. When set, only that specific room is booked.

**Indexes:**
- `UNIQUE(booking_reference)` — for fast reference lookups
- `INDEX(user_id)` — for user's booking history queries
- `INDEX(property_id)` — for property booking queries
- `INDEX(booking_status)` — for status-based filtering
- `INDEX(check_in, check_out)` — for availability queries

### 5.2 BookingGuest

Stores details of each guest associated with a booking.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `guest_id` | `BIGSERIAL` | `PK` | Primary identifier |
| `booking_id` | `BIGINT` | `FK → booking.booking_id`, `NOT NULL` | Parent booking |
| `full_name` | `VARCHAR(255)` | `NOT NULL` | Guest's full name |
| `age` | `INT` | | Guest's age |
| `gender` | `VARCHAR(10)` | | MALE, FEMALE, OTHER |
| `government_id` | `VARCHAR(50)` | | ID document number |
| `is_primary_guest` | `BOOLEAN` | `DEFAULT FALSE` | Primary contact for this booking |

### 5.3 Payment

Tracks payment records for bookings. Supports multiple payment attempts per booking.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `payment_id` | `BIGSERIAL` | `PK` | Primary identifier |
| `booking_id` | `BIGINT` | `FK → booking.booking_id`, `NOT NULL` | Parent booking |
| `amount` | `DECIMAL(10,2)` | `NOT NULL` | Payment amount |
| `currency` | `VARCHAR(3)` | `NOT NULL`, `DEFAULT 'INR'` | Currency code |
| `payment_method` | `VARCHAR(50)` | `NOT NULL` | CREDIT_CARD, DEBIT_CARD, UPI, NET_BANKING, WALLET |
| `provider` | `VARCHAR(50)` | `NOT NULL` | Payment gateway name |
| `transaction_reference` | `VARCHAR(255)` | | Provider transaction ID |
| `status` | `VARCHAR(20)` | `NOT NULL`, `DEFAULT 'PENDING'` | PENDING, SUCCESS, FAILED, REFUNDED |
| `paid_at` | `TIMESTAMP` | | When payment was completed |
| `created_at` | `TIMESTAMP` | `NOT NULL` | Auto-populated |

### 5.4 PaymentTransaction

Immutable gateway transaction logs for audit trail.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `transaction_id` | `BIGSERIAL` | `PK` | Primary identifier |
| `payment_id` | `BIGINT` | `FK → payment.payment_id`, `NOT NULL` | Parent payment |
| `gateway` | `VARCHAR(50)` | `NOT NULL` | Gateway name |
| `gateway_transaction_id` | `VARCHAR(255)` | | Gateway's transaction ID |
| `gateway_response` | `TEXT` | | Raw gateway response (JSON) |
| `status` | `VARCHAR(20)` | `NOT NULL` | SUCCESS, FAILED, PENDING |
| `created_at` | `TIMESTAMP` | `NOT NULL` | Auto-populated |

### 5.5 Refund

Tracks refund requests and processing. Supports partial refunds.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `refund_id` | `BIGSERIAL` | `PK` | Primary identifier |
| `payment_id` | `BIGINT` | `FK → payment.payment_id`, `NOT NULL` | Parent payment |
| `amount` | `DECIMAL(10,2)` | `NOT NULL` | Refund amount |
| `reason` | `VARCHAR(500)` | `NOT NULL` | Reason for refund |
| `status` | `VARCHAR(20)` | `NOT NULL`, `DEFAULT 'PENDING'` | PENDING, APPROVED, PROCESSED, FAILED |
| `processed_at` | `TIMESTAMP` | | When refund was processed |
| `created_at` | `TIMESTAMP` | `NOT NULL` | Auto-populated |

### 5.6 Invoice

Tax invoice generated for completed bookings.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `invoice_id` | `BIGSERIAL` | `PK` | Primary identifier |
| `booking_id` | `BIGINT` | `FK → booking.booking_id`, `NOT NULL` | Parent booking |
| `invoice_number` | `VARCHAR(50)` | `UNIQUE`, `NOT NULL` | Human-readable invoice number |
| `pdf_url` | `VARCHAR(500)` | | URL to generated PDF |
| `subtotal` | `DECIMAL(10,2)` | `NOT NULL` | Amount before tax/discount |
| `tax` | `DECIMAL(10,2)` | `DEFAULT 0` | Tax amount |
| `discount` | `DECIMAL(10,2)` | `DEFAULT 0` | Discount amount |
| `grand_total` | `DECIMAL(10,2)` | `NOT NULL` | Final amount payable |
| `status` | `VARCHAR(20)` | `NOT NULL`, `DEFAULT 'DRAFT'` | DRAFT, ISSUED, PAID, CANCELLED |
| `issued_at` | `TIMESTAMP` | | When invoice was issued |

### 5.7 BookingTimeline

Append-only event history for complete audit trail of booking lifecycle.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `timeline_id` | `BIGSERIAL` | `PK` | Primary identifier |
| `booking_id` | `BIGINT` | `FK → booking.booking_id`, `NOT NULL` | Parent booking |
| `event_type` | `VARCHAR(50)` | `NOT NULL` | Type of event |
| `performed_by` | `VARCHAR(100)` | `NOT NULL` | User/system that performed the action |
| `remarks` | `TEXT` | | Additional notes |
| `created_at` | `TIMESTAMP` | `NOT NULL` | Auto-populated |

**Event Types:** BOOKING_CREATED, PAYMENT_RECEIVED, CONFIRMED, CHECKED_IN, CHECKED_OUT, CANCELLED, REFUND_INITIATED, REFUND_COMPLETED, REVIEW_SUBMITTED, NO_SHOW

**Indexes:** `INDEX(booking_id, created_at)` — for chronological timeline queries

---

## 6. Review Aggregate

### 6.1 Review

Guest reviews for properties. One review per completed booking.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `review_id` | `BIGSERIAL` | `PK` | Primary identifier |
| `booking_id` | `BIGINT` | `UNIQUE`, `FK → booking.booking_id`, `NOT NULL` | Parent booking (one review per booking) |
| `property_id` | `BIGINT` | `FK → property.property_id`, `NOT NULL` | Reviewed property |
| `user_id` | `BIGINT` | `FK → user.user_id`, `NOT NULL` | Review author |
| `rating` | `DECIMAL(2,1)` | `NOT NULL`, `CHECK(1.0-5.0)` | Numerical rating |
| `title` | `VARCHAR(255)` | | Review title |
| `review_text` | `TEXT` | | Detailed review |
| `status` | `VARCHAR(20)` | `NOT NULL`, `DEFAULT 'PENDING'` | PENDING, APPROVED, REJECTED, FLAGGED |
| `created_at` | `TIMESTAMP` | `NOT NULL` | Auto-populated |
| `updated_at` | `TIMESTAMP` | `NOT NULL` | Auto-populated |

**Constraint:** `UNIQUE(booking_id)` — ensures one review per completed booking.

### 6.2 ReviewImage

Images attached to reviews.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `review_image_id` | `BIGSERIAL` | `PK` | Primary identifier |
| `review_id` | `BIGINT` | `FK → review.review_id`, `NOT NULL` | Parent review |
| `image_url` | `VARCHAR(500)` | `NOT NULL` | Image URL |
| `display_order` | `INT` | `NOT NULL` | Sorting order |

---

## 7. Notification Aggregate

### 7.1 NotificationTemplate

Reusable notification templates for different channels.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `template_id` | `BIGSERIAL` | `PK` | Primary identifier |
| `name` | `VARCHAR(100)` | `UNIQUE`, `NOT NULL` | Template identifier (e.g., "booking_confirmation") |
| `channel` | `VARCHAR(20)` | `NOT NULL` | EMAIL, SMS, PUSH, IN_APP |
| `subject` | `VARCHAR(255)` | | Subject line (email/push) |
| `body` | `TEXT` | `NOT NULL` | Template body with variable placeholders |
| `variables` | `TEXT` | | JSON list of expected variables |
| `created_at` | `TIMESTAMP` | `NOT NULL` | Auto-populated |

### 7.2 Notification

Individual notification records sent to users.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `notification_id` | `BIGSERIAL` | `PK` | Primary identifier |
| `user_id` | `BIGINT` | `FK → user.user_id`, `NOT NULL` | Recipient user |
| `template_id` | `BIGINT` | `FK → notification_template.template_id`, `NOT NULL` | Template used |
| `channel` | `VARCHAR(20)` | `NOT NULL` | EMAIL, SMS, PUSH, IN_APP |
| `recipient` | `VARCHAR(255)` | `NOT NULL` | Recipient address (email/phone/device token) |
| `status` | `VARCHAR(20)` | `NOT NULL`, `DEFAULT 'PENDING'` | PENDING, SENT, FAILED |
| `scheduled_at` | `TIMESTAMP` | | When to send (null = immediate) |
| `sent_at` | `TIMESTAMP` | | When it was actually sent |
| `created_at` | `TIMESTAMP` | `NOT NULL` | Auto-populated |

### 7.3 NotificationLog

Immutable delivery history for audit trail.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `log_id` | `BIGSERIAL` | `PK` | Primary identifier |
| `notification_id` | `BIGINT` | `FK → notification.notification_id`, `NOT NULL` | Parent notification |
| `provider` | `VARCHAR(50)` | `NOT NULL` | Delivery provider |
| `provider_response` | `TEXT` | | Raw provider response |
| `status` | `VARCHAR(20)` | `NOT NULL` | SENT, FAILED, BOUNCED, OPENED |
| `created_at` | `TIMESTAMP` | `NOT NULL` | Auto-populated |

---

## 8. Enums & Lookup Tables

### 8.1 PostgreSQL ENUMs (Stable Concepts)

```sql
CREATE TYPE property_status AS ENUM ('ACTIVE', 'INACTIVE', 'ARCHIVED', 'DELETED');
CREATE TYPE room_status AS ENUM ('ACTIVE', 'INACTIVE', 'MAINTENANCE', 'DELETED');
CREATE TYPE booking_status AS ENUM ('PENDING', 'CONFIRMED', 'CHECKED_IN', 'CHECKED_OUT', 'CANCELLED', 'NO_SHOW');
CREATE TYPE payment_status AS ENUM ('UNPAID', 'PARTIALLY_PAID', 'PAID', 'REFUNDED', 'PARTIALLY_REFUNDED');
CREATE TYPE booking_type AS ENUM ('PROPERTY', 'ROOM');
CREATE TYPE booking_source AS ENUM ('WEBSITE', 'MOBILE_APP', 'ADMIN', 'AGENCY');
CREATE TYPE notification_channel AS ENUM ('EMAIL', 'SMS', 'PUSH', 'IN_APP');
```

### 8.2 Lookup Tables (Evolving Concepts)

These use regular tables (not ENUMs) because they evolve over time:

| Table | Reason for Lookup Table |
|-------|------------------------|
| `role` | Roles may be added (e.g., SUPER_ADMIN, ANALYST) |
| `amenity` | New amenities may be added dynamically |
| `notification_template` | Templates are created dynamically by admins |

---

## 9. Indexing Strategy

### 9.1 Primary Indexes (UNIQUE)

| Table | Column(s) | Purpose |
|-------|-----------|---------|
| `user` | `email` | Fast login lookups |
| `property` | `slug` | Fast URL lookups |
| `booking` | `booking_reference` | Fast reference lookups |
| `amenity` | `name` | Prevent duplicate amenities |
| `invoice` | `invoice_number` | Fast invoice lookups |

### 9.2 Foreign Key Indexes

Every foreign key column should be indexed:

| Column | Parent Table |
|--------|-------------|
| `property_id` | `property_image`, `property_amenity`, `property_policy`, `property_contact`, `room`, `booking`, `review`, `wishlist` |
| `room_id` | `room_image`, `room_availability`, `booking` |
| `user_id` | `booking`, `review`, `wishlist`, `refresh_token`, `user_preference`, `user_activity`, `notification` |
| `booking_id` | `booking_guest`, `payment`, `invoice`, `booking_timeline` |
| `payment_id` | `payment_transaction`, `refund` |

### 9.3 Search & Filter Indexes

| Table | Column(s) | Query Pattern |
|-------|-----------|--------------|
| `property` | `city` | "Show properties in Goa" |
| `property` | `status` | "Show active properties" |
| `property` | `(latitude, longitude)` | "Show nearby properties" |
| `property` | `(status, city)` | "Show active properties in Goa" |
| `booking` | `(booking_status, user_id)` | "Show user's confirmed bookings" |

### 9.4 Composite Indexes

| Table | Columns | Purpose |
|-------|---------|---------|
| `property_image` | `(property_id, display_order)` | Uniqueness + ordering |
| `room` | `(property_id, room_number)` | Uniqueness per property |
| `room_image` | `(room_id, display_order)` | Uniqueness + ordering |
| `room_availability` | `(room_id, start_date, end_date)` | Availability range queries |
| `booking` | `(check_in, check_out)` | Availability search |
| `wishlist` | `(user_id, property_id)` | Prevent duplicates |
| `booking_timeline` | `(booking_id, created_at)` | Chronological timeline queries |

---

## 10. Entity-Relationship Summary

### Legend

```
1 → *  : One-to-many
* → *  : Many-to-many
1 → 1  : One-to-one
```

### Relationships

```
User 1 → * Booking
User 1 → * Review
User 1 → * RefreshToken
User 1 → 1 UserPreference
User 1 → * Wishlist
User 1 → * UserActivity
User * → * Role (via UserRole)

Property 1 → * PropertyImage
Property 1 → * Room
Property 1 → 1 PropertyPolicy
Property 1 → * PropertyContact
Property * → * Amenity (via PropertyAmenity)
Property 1 → * Booking
Property 1 → * Review

Room 1 → * RoomImage
Room 1 → * RoomAvailability
Room 1 → * Booking

Booking 1 → * BookingGuest
Booking 1 → * Payment
Booking 1 → * BookingTimeline
Booking 1 → 1 Invoice
Booking 1 → 1 Review

Payment 1 → * PaymentTransaction
Payment 1 → * Refund

Review 1 → * ReviewImage

NotificationTemplate 1 → * Notification
Notification 1 → * NotificationLog
```

---

*This document is maintained as the authoritative source for the DriftStay database schema. All schema changes must be reflected here before implementation.*
