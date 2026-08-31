# syntax=docker/dockerfile:1

# ─────────────────────────────────────────────────────────────
# Étape 1 — build Maven (tests ignorés : ils exigent Postgres + Keycloak)
# ─────────────────────────────────────────────────────────────
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Dépendances (cache tant que pom.xml ne change pas)
COPY pom.xml .
RUN mvn -B -q dependency:go-offline

# Code + package
COPY src ./src
RUN mvn -B -q clean package -DskipTests

# ─────────────────────────────────────────────────────────────
# Étape 2 — image d'exécution (JRE seul)
# ─────────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Utilisateur non-root
RUN addgroup -S lbs && adduser -S lbs -G lbs

COPY --from=build /app/target/*.jar app.jar
RUN chown -R lbs:lbs /app
USER lbs

EXPOSE 8082

ENV JAVA_OPTS="-XX:MaxRAMPercentage=75 -XX:+UseContainerSupport"
ENV SPRING_PROFILES_ACTIVE=prod

# Actuator health (spring-boot-starter-actuator est déjà présent)
HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=5 \
  CMD wget -qO- http://localhost:8082/actuator/health | grep -q '"status":"UP"' || exit 1

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar app.jar"]
