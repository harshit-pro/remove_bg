# Stage 1: Build the application
FROM maven:3.9.6-eclipse-temurin-21-alpine AS build
WORKDIR /app

# Copy build configuration files
COPY pom.xml .
COPY .mvn/ .mvn
COPY mvnw .

# Download dependencies (cached layer)
RUN ./mvnw dependency:go-offline -B

# Copy source code
COPY src/ ./src

# Build application
RUN ./mvnw clean package -DskipTests -B

# Stage 2: Create runtime
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Create non-root user
RUN addgroup -S spring && adduser -S spring -G spring \
    && chown -R spring:spring /app
USER spring:spring

# Copy built artifact
COPY --from=build --chown=spring:spring /app/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]