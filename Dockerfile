# ====== BUILD STAGE ======
FROM maven:3.9.11-eclipse-temurin-17 AS builder

WORKDIR /build

# Copy pom.xml và các file cần thiết
COPY pom.xml .
COPY mvnw .
COPY mvnw.cmd .
COPY .mvn .mvn

# Copy source code
COPY src ./src

# Build ứng dụng
RUN mvn clean package -DskipTests


# ====== RUNTIME STAGE ======
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Copy jar file từ builder stage
COPY --from=builder /build/target/trendy_chat-0.0.1-SNAPSHOT.jar app.jar

# Expose port
EXPOSE 8080

# Health check - chỉ dùng nếu bật Actuator trong application.properties
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# Run application
ENTRYPOINT ["java", "-jar", "app.jar"]
