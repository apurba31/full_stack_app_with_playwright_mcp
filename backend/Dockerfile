# syntax=docker/dockerfile:1.6

# ---------- Stage 1: Build ----------
# Build context is the repo root (set by docker-compose) so we can access both
# `backend/` and `proto/` from a single context.
FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /workspace

# Copy pom first to leverage Docker layer caching for dependencies.
COPY backend/pom.xml ./backend/pom.xml
WORKDIR /workspace/backend
RUN mvn -B -q dependency:go-offline

# Copy backend sources and proto definitions (proto lives outside backend/).
WORKDIR /workspace
COPY backend/ ./backend/
COPY proto/ ./proto/

WORKDIR /workspace/backend
RUN mvn -B -DskipTests package

# ---------- Stage 2: Runtime ----------
FROM eclipse-temurin:21-jre-jammy AS runtime

# Install curl for the healthcheck and create a non-root user.
RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/* \
    && groupadd --system --gid 1001 app \
    && useradd  --system --uid 1001 --gid app --home /app --shell /usr/sbin/nologin app

WORKDIR /app

# Copy the produced fat jar. The build produces exactly one *.jar (excluding *original*).
COPY --from=build /workspace/backend/target/*.jar /app/app.jar
RUN chown -R app:app /app

USER app

EXPOSE 8080 9090

HEALTHCHECK --interval=15s --timeout=5s --start-period=60s --retries=10 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["java","-jar","/app/app.jar"]
