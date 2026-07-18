#!/usr/bin/env bash
set -e

cd "$(dirname "$0")/.."

# Set git config
git config user.name "Anethix Labs"
git config user.email "developer@anethixlabs.com"

# Commit 1: Root config files
git add .gitignore README.md
git commit -m "chore(project): initialize project structure with .gitignore and README

- Add comprehensive .gitignore for Java, Maven, and IDE artifacts
- Add project README with architecture, stack, and roadmap overview"

echo "✓ Commit 1 done"

# Commit 2: Maven build config
git add backend/pom.xml
git commit -m "chore(build): configure Maven with Spring Boot 3.4.7, JPA, Security, Flyway, Redis

- Set up Spring Boot 3.4.7 as parent with Java 21
- Add dependencies: Web, Security, Validation, Data JPA
- Add PostgreSQL driver with Flyway migrations
- Add Redis, Actuator, Swagger, MapStruct, Lombok
- Configure MapStruct and Lombok annotation processing"

echo "✓ Commit 2 done"

# Commit 3: Docker Compose
git add backend/compose.yaml
git commit -m "chore(docker): add Docker Compose for PostgreSQL and Redis services

- Configure PostgreSQL 16 with persistent volume
- Configure Redis for caching layer
- Map standard ports for local development"

echo "✓ Commit 3 done"

# Commit 4: Application configuration
git add backend/src/main/resources/application.yml
git commit -m "chore(config): configure application with PostgreSQL, Flyway, JPA, and Redis

- Set up PostgreSQL datasource with environment variables
- Configure JPA with Hibernate PostgreSQL dialect
- Enable Flyway migrations with baseline-on-migrate
- Configure Redis connection for caching
- Set server port 8000 with /api context path
- Configure Swagger/OpenAPI documentation"

echo "✓ Commit 4 done"

# Commit 5: Application entry point
git add backend/src/main/java/com/driftstay/DriftStayApplication.java
git commit -m "feat(app): initialize Spring Boot application entry point

- Add @SpringBootApplication main class
- Enable JPA Auditing for automatic timestamp management
- Enable async processing for background tasks
- Enable scheduled task support for batch operations"

echo "✓ Commit 5 done"

# Commit 6: Test configuration
git add backend/src/test/
git commit -m "chore(test): configure test setup with H2 database and Spring Boot test

- Add Spring Boot test class with active test profile
- Configure H2 in-memory database for fast test execution
- Disable Flyway for test profile
- Use create-drop DDL strategy for test isolation"

echo "✓ Commit 6 done"

# Verify
echo ""
echo "=== Commit History ==="
git log --oneline
echo ""
echo "✓ All initial commits completed successfully!"
