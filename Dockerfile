# Multi-stage Dockerfile for SPEDO Analytics
# Stage 1: Build JAR with Maven & Eclipse Temurin 21
FROM maven:3.9.6-eclipse-temurin-21 AS builder

WORKDIR /build

# Copy Maven POM
COPY pom.xml .

# Download dependencies in cache layer
RUN mvn dependency:go-offline -B || true

# Copy source code and static assets
COPY src ./src

# Package production executable JAR without running tests
RUN mvn clean package -DskipTests -B

# Stage 2: Minimal, secure production runtime
FROM eclipse-temurin:21-jre-alpine

LABEL maintainer="SPEDO Analytics Team <support@spedo.io>"
LABEL description="SPEDO Social Media Analytics & Intelligence Engine"

# Create non-root system group and user
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

WORKDIR /app

# Create persistent data directory with proper ownership
RUN mkdir -p /app/data && chown -R appuser:appgroup /app

# Copy built artifact from builder stage
COPY --from=builder /build/target/spedo-analytics-1.0.0.jar app.jar
RUN chown appuser:appgroup /app/app.jar

# Switch to non-root user
USER appuser

# Declare volume for persistent H2/file storage
VOLUME ["/app/data"]

# Default exposed port (can be overridden via $PORT env var)
EXPOSE 8080

# Environment variables with production defaults
ENV PORT=8080 \
    SPRING_PROFILES_ACTIVE=prod \
    H2_CONSOLE_ENABLED=false \
    DEMO_MODE_ENABLED=false \
    JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:+ExitOnOutOfMemoryError"

# Container healthcheck
HEALTHCHECK --interval=30s --timeout=5s --start-period=40s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://127.0.0.1:${PORT}/login.html || exit 1

# Execute Spring Boot application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
