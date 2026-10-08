# Stage 1: Build stage
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Copy pom.xml and source tree
COPY pom.xml .
COPY src ./src

# Build the application jar skipping tests
RUN mvn -DskipTests package

# Stage 2: Runtime stage
FROM eclipse-temurin:21-jre
WORKDIR /app

# Run as a non-root user
RUN useradd -m appuser && \
    chown -R appuser:appuser /app

USER appuser

# Copy the packaged jar from build stage
COPY --from=build --chown=appuser:appuser /app/target/*.jar app.jar

# Application port configured in application.properties
EXPOSE 8082

ENTRYPOINT ["java", "-jar", "app.jar"]
