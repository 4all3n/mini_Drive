# Multi-stage Dockerfile for Spring Boot Backend
FROM eclipse-temurin:17-jdk-alpine AS builder
WORKDIR /app

# Copy Maven wrapper and POM
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw dependency:go-offline -B

# Copy source code and package
COPY src ./src
RUN ./mvnw package -DskipTests -B

# Runtime Image
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Create storage directory
RUN mkdir -p /app/cloud_storage

# Copy fat JAR from builder stage
COPY --from=builder /app/target/backend-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080
VOLUME ["/app/cloud_storage"]

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
