# Build stage
FROM eclipse-temurin:21-jdk AS builder

WORKDIR /app

# Copy Maven wrapper files
COPY mvnw .
COPY .mvn .mvn

# Copy checkstyle configuration (only for build stage)
COPY config ./config/

# Copy backend source code
COPY backend/ ./backend/

WORKDIR /app/backend

RUN chmod +x ../mvnw
RUN ../mvnw clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:21-jre

WORKDIR /app

# Only copy the built JAR from the build stage
COPY --from=builder /app/backend/target/season-champions-0.0.1-SNAPSHOT.jar ./app.jar

CMD ["java", "-jar", "app.jar"]
