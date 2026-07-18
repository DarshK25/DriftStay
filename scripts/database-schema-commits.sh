#!/usr/bin/env bash
# ============================================================================
# DriftStay Database Schema Git Workflow
# ============================================================================
# This script creates a professional commit history with feature branching
# for the database schema implementation.
#
# Prerequisites:
#   - Run from the project root directory
#   - Git user config already set up
#
# Usage: bash scripts/database-schema-commits.sh
# ============================================================================

set -e

echo "=== DriftStay Database Schema Git Workflow ==="
echo ""

# -------------------------------------------------------
# Step 1: First, commit the initial project configuration files
# -------------------------------------------------------
echo ">>> Step 1: Committing initial project configuration..."

# Commit 1: Root project config
cd "$(dirname "$0")/.."

# Create orphan branch for proper initial commit structure
git checkout --orphan main 2>/dev/null || true

# Commit 1: Root config
git add .gitignore README.md
git commit -m "chore(project): initialize project structure with .gitignore and README

- Add comprehensive .gitignore for Java, Maven, and IDE artifacts
- Add project README with architecture, tech stack, and roadmap"

echo "✓ Commit 1: Project root config committed"

# Commit 2: Maven build
git add backend/pom.xml
git commit -m "chore(build): configure Maven with Spring Boot 3.4.7, JPA, Security, Flyway, Redis

- Set up Spring Boot 3.4.7 as parent with Java 21
- Add dependencies: Web, Security, Validation, Data JPA
- Add PostgreSQL driver with Flyway migrations
- Add Redis, Actuator, Swagger, MapStruct, Lombok"

echo "✓ Commit 2: Build config committed"

# Commit 3: Docker config
git add backend/compose.yaml
git commit -m "chore(docker): add Docker Compose for PostgreSQL and Redis services

- Configure PostgreSQL 16 with persistent volume
- Configure Redis for caching layer
- Map standard ports for local development"

echo "✓ Commit 3: Docker config committed"

# Commit 4: Application config
git add backend/src/main/resources/application.yml
git commit -m "chore(config): configure application with PostgreSQL, Flyway, JPA, and Redis

- Set up PostgreSQL datasource with environment variables
- Configure JPA with Hibernate PostgreSQL dialect and ddl-auto: validate
- Enable Flyway migrations with baseline-on-migrate
- Configure Redis connection for caching
- Set server port 8000 with /api context path"

echo "✓ Commit 4: Application config committed"

# Commit 5: Application entry point
git add backend/src/main/java/com/driftstay/DriftStayApplication.java
git commit -m "feat(app): initialize Spring Boot application entry point

- Add @SpringBootApplication main class
- Enable JPA Auditing for automatic timestamp management
- Enable async processing for background tasks
- Enable scheduled task support for batch operations"

echo "✓ Commit 5: Application entry point committed"

# Commit 6: Test config
git add backend/src/test/
git commit -m "chore(test): configure test setup with H2 database and Spring Boot test

- Add Spring Boot test class with active test profile
- Configure H2 in-memory database for fast test execution
- Disable Flyway for test profile
- Use create-drop DDL strategy for test isolation"

echo "✓ Commit 6: Test config committed"

# -------------------------------------------------------
# Step 2: Create develop branch and feature branch
# -------------------------------------------------------
echo ""
echo ">>> Step 2: Creating feature branch for database architecture..."

git branch -M main
git checkout -b develop
git checkout -b feature/database-architecture

echo "✓ Feature branch 'feature/database-architecture' created from 'develop'"
echo ""

# -------------------------------------------------------
# Step 3: Commit database design documentation
# -------------------------------------------------------
echo ">>> Step 3: Committing database design documentation..."

git add docs/DATABASE_DESIGN.md
git commit -m "docs(database): add comprehensive database architecture design document

- Document complete schema design principles (ACID, 3NF, DDD)
- Define all 5 aggregates: Property, User, Booking, Review, Notification
- Specify all tables, columns, constraints, and relationships
- Document indexing strategy for performance optimization
- Include enum definitions and entity-relationship summary

This document serves as the authoritative source for the
DriftStay database schema going forward."

echo "✓ Commit 7: Database design docs committed"
echo ""

# -------------------------------------------------------
# Step 4: Commit Flyway migration
# -------------------------------------------------------
echo ">>> Step 4: Committing Flyway V1 migration..."

git add backend/src/main/resources/db/migration/V1__initial_schema.sql
git commit -m "feat(db): create Flyway V1 migration with initial database schema

- Create PostgreSQL ENUMs: property_status, room_status, booking_status,
  booking_type, payment_status, booking_source, notification_channel
- Create Property aggregate: property, property_image, amenity,
  property_amenity, property_policy, property_contact, room, room_image,
  room_availability
- Create User aggregate: user, role, user_role, refresh_token,
  user_preference, wishlist, user_activity
- Create Booking aggregate: booking, booking_guest, payment,
  payment_transaction, refund, invoice, booking_timeline
- Create Review aggregate: review, review_image
- Create Notification aggregate: notification_template, notification,
  notification_log
- Add all table constraints, foreign keys, and performance indexes"

echo "✓ Commit 8: Flyway migration committed"
echo ""

# -------------------------------------------------------
# Step 5: Commit common base entities and enums
# -------------------------------------------------------
echo ">>> Step 5: Committing base entities and enums..."

git add backend/src/main/java/com/driftstay/common/
git commit -m "feat(common): add base entity with JPA auditing and domain enums

- Create BaseEntity with @CreatedDate and @LastModifiedDate auditing
- Add all domain enums: PropertyStatus, RoomStatus, BookingStatus,
  BookingType, PaymentStatus, BookingSource, UserStatus,
  NotificationChannel"

echo "✓ Commit 9: Base entities and enums committed"
echo ""

# -------------------------------------------------------
# Step 6: Commit Property aggregate entities
# -------------------------------------------------------
echo ">>> Step 6: Committing Property aggregate entities..."

git add backend/src/main/java/com/driftstay/property/
git commit -m "feat(property): implement Property aggregate JPA entities

- Create Property entity with geolocation, cached ratings, and status
- Create PropertyImage for gallery management with display ordering
- Create Amenity lookup table with category classification
- Create PropertyPolicy one-to-one for check-in/out times and rules
- Create PropertyContact for management contact details
- Create Room entity with pricing, capacity, and room type
- Create RoomImage for room-specific photos
- Create RoomAvailability for date-range availability tracking"

echo "✓ Commit 10: Property aggregate committed"
echo ""

# -------------------------------------------------------
# Step 7: Commit User aggregate entities
# -------------------------------------------------------
echo ">>> Step 7: Committing User aggregate entities..."

git add backend/src/main/java/com/driftstay/user/
git commit -m "feat(user): implement User aggregate JPA entities

- Create User entity with BCrypt password storage and verification
- Create Role lookup table for RBAC authorization
- Create RefreshToken with hashed token storage and revocation
- Create UserPreference one-to-one for user settings
- Create Wishlist for saved/favorite properties
- Create UserActivity append-only log for analytics"

echo "✓ Commit 11: User aggregate committed"
echo ""

# -------------------------------------------------------
# Step 8: Commit Booking aggregate entities
# -------------------------------------------------------
echo ">>> Step 8: Committing Booking aggregate entities..."

git add backend/src/main/java/com/driftstay/booking/
git commit -m "feat(booking): implement Booking aggregate JPA entities

- Create Booking entity supporting both property and room bookings
- Create BookingGuest for detailed guest information
- Create Payment entity with multiple payment method support
- Create PaymentTransaction immutable log for gateway audit trail
- Create Refund entity supporting partial refunds
- Create Invoice entity with tax and discount breakdown
- Create BookingTimeline append-only event history"

echo "✓ Commit 12: Booking aggregate committed"
echo ""

# -------------------------------------------------------
# Step 9: Commit Review and Notification aggregate entities
# -------------------------------------------------------
echo ">>> Step 9: Committing Review and Notification aggregate entities..."

git add backend/src/main/java/com/driftstay/review/
git add backend/src/main/java/com/driftstay/notification/
git commit -m "feat(review,notification): implement Review and Notification aggregates

- Create Review entity with one-review-per-booking constraint
- Create ReviewImage entity for review photo attachments
- Create NotificationTemplate for reusable message templates
- Create Notification entity with scheduled sending support
- Create NotificationLog immutable delivery history"

echo "✓ Commit 13: Review and Notification aggregates committed"
echo ""

# -------------------------------------------------------
# Step 10: Merge feature branch to develop
# -------------------------------------------------------
echo ">>> Step 10: Merging feature branch to develop..."

git checkout develop
git merge feature/database-architecture --no-ff -m "feat(db): merge database schema implementation

- Complete database architecture documentation
- Flyway V1 migration with all 26 tables
- JPA entity classes for all 5 aggregates
- Base entity with auditing and domain enums"

echo "✓ Feature branch merged to develop"
echo ""

# -------------------------------------------------------
# Summary
# -------------------------------------------------------
echo ""
echo "=== Git History Summary ==="
git log --oneline --graph --all
echo ""
echo "=== Branches ==="
git branch -a
echo ""
echo "✓ Database schema workflow completed successfully!"
echo ""
echo "Next steps:"
echo "  1. Review the commit history: git log --oneline"
echo "  2. Merge develop to main when ready: git checkout main && git merge develop"
echo "  3. Build and test: cd backend && ./mvnw compile"
