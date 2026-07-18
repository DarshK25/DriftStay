# DriftStay

A scalable luxury homestay booking platform built using Spring Boot, PostgreSQL, Redis, and Docker.

DriftStay is a backend-first, production-inspired booking platform designed to demonstrate modern software engineering principles including layered architecture, secure authentication, transactional booking workflows, caching, concurrency control, and scalable system design.

---

# Table of Contents

- Overview
- Objectives
- Features
- Backend Engineering Highlights
- System Design Concepts
- Technology Stack
- Project Structure
- Architecture
- Database Design
- Authentication & Authorization
- API Design
- Documentation
- Development Roadmap
- Learning Outcomes
- Future Enhancements
- Contributors
- License

---

# Overview

DriftStay is a full-stack luxury homestay booking platform developed as part of the **Anethix Labs Internship Project #2**.

Although the internship requirements focus on building a booking website with an admin panel, this project is intentionally designed to go significantly beyond those requirements by incorporating production-level backend architecture and engineering practices.

The primary objective is not only to build a functional application but also to gain practical experience in backend development, database design, distributed systems fundamentals, API design, transaction management, caching, authentication, and scalable software architecture.

Many concepts implemented throughout the project are inspired by engineering challenges encountered in modern booking platforms.

---

# Objectives

The project focuses on achieving the following goals:

- Build a complete luxury homestay booking platform
- Design a scalable backend architecture
- Learn Spring Boot ecosystem and best practices
- Develop secure REST APIs
- Implement authentication and authorization
- Design a reliable booking workflow
- Learn relational database design
- Practice clean architecture principles
- Understand transaction management
- Explore caching strategies
- Improve software engineering practices
- Learn collaborative development using Git and GitHub

---

# Features

## Customer Features

- User Registration
- Secure Login
- Browse Luxury Properties
- View Property Details
- Explore Room Listings
- View Property Gallery
- Search Rooms
- Filter Properties
- Check Room Availability
- Submit Booking Requests
- Booking History
- Booking Status Tracking
- Contact Property Management
- View Nearby Attractions
- Manage User Profile

---

## Administrative Features

- Secure Administrator Dashboard
- Property Management
- Room Management
- Booking Management
- Gallery Management
- Customer Management
- Customer Enquiry Management
- Analytics Dashboard
- Occupancy Reports
- Revenue Insights

---

# Backend Engineering Highlights

The project emphasizes backend engineering over frontend implementation.

Key engineering concepts include:

- Feature-based package organization
- Layered architecture
- Clean separation of concerns
- DTO Pattern
- Repository Pattern
- Service Layer Pattern
- Dependency Injection
- Global Exception Handling
- Request Validation
- API Versioning
- Secure Authentication
- Role-Based Access Control
- Password Encryption
- Flyway Database Migrations
- Redis Caching
- Dockerized Development Environment
- Transaction Management
- Booking State Machine
- Optimistic Locking
- Background Job Scheduling
- Idempotent Operations
- Structured Logging
- Pagination
- Filtering
- Sorting

---

# System Design Concepts

The project serves as a practical implementation of several important backend and system design concepts.

These include:

- Booking Engine Design
- Inventory Management
- Room Availability Management
- Preventing Double Bookings
- Transaction Isolation
- Cache-Aside Pattern
- Event-Driven Workflows
- Background Processing
- Stateless Authentication
- Database Indexing
- Read and Write Optimization
- Service Modularization
- Scalability Principles

---

# Technology Stack

## Backend

- Java 21
- Spring Boot 3
- Spring Security
- Spring Data JPA
- Hibernate
- Maven

## Database

- PostgreSQL (Neon)
- Flyway

## Caching

- Redis

## Documentation

- OpenAPI
- Swagger UI

## DevOps

- Docker
- Docker Compose

## Testing

- JUnit
- Mockito
- Testcontainers (Planned)

---

# Project Structure

```
driftstay/

├── backend/
│   ├── src/
│   │   ├── main/
│   │   │
│   │   ├── java/
│   │   │   └── com/driftstay/
│   │   │
│   │   │       ├── auth/
│   │   │       ├── user/
│   │   │       ├── property/
│   │   │       ├── room/
│   │   │       ├── booking/
│   │   │       ├── payment/
│   │   │       ├── review/
│   │   │       ├── gallery/
│   │   │       ├── notification/
│   │   │       ├── analytics/
│   │   │       ├── admin/
│   │   │
│   │   │       ├── config/
│   │   │       ├── security/
│   │   │       ├── common/
│   │   │       ├── infrastructure/
│   │   │       ├── exception/
│   │   │       └── util/
│   │
│   │   └── resources/
│   │       ├── application.yml
│   │       ├── db/
│   │       │   └── migration/
│   │       ├── static/
│   │       └── templates/
│
├── frontend/
│
├── docs/
│
├── scripts/
│
├── load-tests/
│
└── README.md
```

---

# Planned Architecture

The application follows a layered architecture.

```
Client

↓

Controller Layer

↓

Service Layer

↓

Repository Layer

↓

PostgreSQL
```

Additional infrastructure services include:

- Redis
- Flyway
- Docker
- JWT Authentication
- Background Workers
- Logging

---

# Database Design

The primary domain entities include:

- User
- Role
- Property
- Room
- RoomImage
- Booking
- Payment
- Review
- Amenity
- Gallery
- ContactMessage
- Notification

A complete Entity Relationship Diagram (ERD) will be available under the `docs/` directory.

---

# Authentication & Authorization

Authentication will be implemented using Spring Security.

Features include:

- JWT Access Tokens
- Refresh Tokens
- BCrypt Password Hashing
- Stateless Authentication
- Protected REST APIs
- Role-Based Access Control
- Secure Password Storage

---

# API Design

The backend exposes RESTful APIs following versioned endpoints.

Example:

```
/api/v1/auth

/api/v1/users

/api/v1/properties

/api/v1/rooms

/api/v1/bookings

/api/v1/payments

/api/v1/admin
```

Interactive API documentation will be available through Swagger.

---

# Documentation

Project documentation will be maintained inside the `docs/` directory.

Documentation includes:

- Architecture Overview
- Database Schema
- Entity Relationship Diagram
- API Documentation
- Sequence Diagrams
- Deployment Guide
- Future Enhancements

---

# Development Roadmap

## Phase 1

- Repository Setup
- Spring Boot Configuration
- PostgreSQL Integration
- Docker Configuration
- Redis Configuration
- Flyway Migrations

## Phase 2

- Authentication
- Authorization
- User Management

## Phase 3

- Property Module
- Room Module
- Gallery Module

## Phase 4

- Booking Engine
- Availability Checking
- Booking Validation

## Phase 5

- Payment Module
- Notification System
- Email Services

## Phase 6

- Redis Caching
- Performance Optimization
- Analytics Dashboard

## Phase 7

- Background Processing
- Distributed Locking
- Production Optimizations

---

# Learning Outcomes

The project is designed to strengthen practical knowledge in:

- Backend Development
- Spring Boot
- REST API Development
- Database Design
- SQL Optimization
- Authentication & Authorization
- Object-Oriented Design
- Design Patterns
- Transaction Management
- Caching
- Clean Architecture
- System Design
- Scalability Principles
- Docker
- Collaborative Software Development

---

# Future Enhancements

Planned enhancements include:

- Online Payment Gateway
- Email Notifications
- Customer Reviews
- Google Maps Integration
- Geospatial Property Search
- Advanced Search Filters
- Booking Recommendations
- Booking Expiration Workers
- Distributed Locking
- Payment Idempotency
- ElasticSearch
- Prometheus Monitoring
- Grafana Dashboards
- AWS Deployment
- CI/CD Pipeline

---

# Contributors

Developed as part of:

**Anethix Labs Internship Project #2**

Project Name:

**DriftStay – Luxury Homestay Booking Platform**

---

# License

This project is developed for educational, internship, and learning purposes.