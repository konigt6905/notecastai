# ---- Build stage ----
FROM gradle:8.10.2-jdk17-alpine AS build
WORKDIR /workspace
COPY . .
# Produce a fat jar
RUN gradle --no-daemon clean bootJar

# ---- Runtime stage ----
FROM eclipse-temurin:17-jre-alpine
ENV APP_HOME=/app \
    JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75 -XX:InitialRAMPercentage=50 -XX:+HeapDumpOnOutOfMemoryError -Djava.security.egd=file:/dev/./urandom"
WORKDIR ${APP_HOME}

# Copy jar
COPY --from=build /workspace/build/libs/*-SNAPSHOT.jar app.jar

# Spring Boot listens on 8080
EXPOSE 8080

# Health endpoint for platforms
HEALTHCHECK --interval=30s --timeout=3s --retries=5 CMD wget -qO- http://127.0.0.1:8080/actuator/health || exit 1

ENTRYPOINT ["sh","-c","java $JAVA_OPTS -jar app.jar"]
