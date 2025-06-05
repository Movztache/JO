# ================================
# DOCKERFILE MULTI-STAGE OPTIMISE
# ================================

# Stage 1: Build de l'application
FROM maven:3.9.9-eclipse-temurin-23 AS builder

# Definir le repertoire de travail
WORKDIR /app

# Copier les fichiers de configuration Maven
COPY pom.xml .
COPY .mvn/ .mvn/
COPY mvnw mvnw.cmd ./

# Telecharger les dependances (mise en cache Docker)
RUN ./mvnw dependency:go-offline -B

# Copier le code source
COPY src ./src

# Build de l'application (skip tests pour accelerer)
RUN ./mvnw clean package -DskipTests -B

# Stage 2: Runtime optimise
FROM eclipse-temurin:23-jre-alpine

# Metadonnees de l'image
LABEL maintainer="votre-email@example.com"
LABEL description="Application Jeux Olympiques - Spring Boot"
LABEL version="1.0.0"

# Creer un utilisateur non-root pour la securite
RUN addgroup -g 1001 -S appgroup && \
    adduser -u 1001 -S appuser -G appgroup

# Installer curl pour les health checks
RUN apk add --no-cache curl

# Creer le repertoire de l'application
WORKDIR /app

# Copier le JAR depuis le stage de build
COPY --from=builder /app/target/vibe-tickets-*.jar app.jar

# Changer le proprietaire des fichiers
RUN chown -R appuser:appgroup /app

# Utiliser l'utilisateur non-root
USER appuser

# Exposer le port de l'application
EXPOSE 8080

# Variables d'environnement par defaut
ENV JAVA_OPTS="-Xmx512m -Xms256m" \
    SPRING_PROFILES_ACTIVE=docker

# Health check pour verifier que l'application fonctionne
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

# Point d'entree de l'application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
