# Build stage
FROM eclipse-temurin:21-jdk AS builder

WORKDIR /app

# Copy pom.xml first for better layer caching
COPY pom.xml .

# Copy Maven wrapper from the project root (we'll download it if not available)
# Since mvnw might not be in backend directory, we'll use the Maven Docker image approach
FROM maven:3.9.6-eclipse-temurin-21 AS maven-builder

WORKDIR /app

# Copy pom.xml first for better layer caching
COPY pom.xml .

# Download dependencies (this layer will be cached if pom.xml doesn't change)
RUN mvn dependency:go-offline -B

# Copy source code
COPY src ./src

# Build the application (skip tests and checkstyle since we don't have config)
RUN mvn clean package -DskipTests -Dcheckstyle.skip=true

# Runtime stage
FROM eclipse-temurin:21-jre

WORKDIR /app

# Install curl for health checks
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

# Copy the built JAR from the build stage
COPY --from=maven-builder /app/target/season-champions-0.0.1-SNAPSHOT.jar ./app.jar

# Expose the port
EXPOSE 8080

# Add health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=40s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

# Run the application
CMD ["java", "-jar", "app.jar"]
