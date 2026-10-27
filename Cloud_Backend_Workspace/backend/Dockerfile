# ==============================================================================
# Stage 1: Build the Spring Boot application using Eclipse Temurin 21 (Alpine)
# ==============================================================================
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app

# Install utility packages required by Maven Wrapper on Alpine
RUN apk add --no-cache bash curl unzip

# Copy Maven wrapper scripts and descriptor
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x mvnw

# Pre-fetch and cache project dependencies (failsafe cache warm-up)
RUN ./mvnw dependency:go-offline -B || true

# Copy application source code and compile fat JAR
COPY src ./src
RUN ./mvnw clean package -DskipTests -B

# ==============================================================================
# Stage 2: Minimal Production Runtime Image
# ==============================================================================
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Prepare storage directory
RUN mkdir -p /app/cloud_storage && chmod 777 /app/cloud_storage

# Copy compiled fat JAR from builder stage
COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8080
VOLUME ["/app/cloud_storage"]

# Fast startup with secure random seed
ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "/app/app.jar"]
