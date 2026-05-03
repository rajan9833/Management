# Multi-stage build for Spring Boot — deploy on Render as a Web Service (Docker).
# Build
FROM eclipse-temurin:17-jdk-jammy AS build
WORKDIR /app

COPY pom.xml .
COPY mvnw .
COPY .mvn .mvn
RUN chmod +x mvnw

COPY src src
RUN ./mvnw -B -q -DskipTests package \
    && mv target/employee-management-*.jar application.jar

# Run
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

RUN useradd --system --uid 1001 appuser \
    && mkdir -p uploads/photos \
    && chown -R appuser:appuser /app

COPY --from=build /app/application.jar application.jar

USER appuser

# Render sets PORT; keep a default for local docker run.
ENV PORT=8080
EXPOSE 8080

# Tuned for small containers on Render Free tier
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75.0 -Djava.security.egd=file:/dev/./urandom"

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/application.jar"]
