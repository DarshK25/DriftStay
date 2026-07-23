FROM maven:3.9-eclipse-temurin-21-alpine AS builder
WORKDIR /build
COPY backend/pom.xml .
RUN mvn dependency:go-offline -q --no-transfer-progress
COPY backend/src ./src
RUN mvn package -DskipTests -q --no-transfer-progress

FROM eclipse-temurin:21-jre-alpine
RUN addgroup -S driftstay && adduser -S driftstay -G driftstay
WORKDIR /app
COPY --from=builder /build/target/*.jar app.jar
EXPOSE 8000
USER driftstay
ENTRYPOINT ["java", "-jar", "app.jar"]
