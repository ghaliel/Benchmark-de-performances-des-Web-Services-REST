FROM maven:3.9.6-amazoncorretto-21 AS builder
WORKDIR /app

# Copier la structure Maven nécessaire
COPY pom.xml ./
COPY common-entities/pom.xml common-entities/pom.xml
COPY variant-a-jersey/pom.xml variant-a-jersey/pom.xml
COPY variant-c-springmvc/pom.xml variant-c-springmvc/pom.xml
COPY variant-d-springdata/pom.xml variant-d-springdata/pom.xml

# Copier et installer le module commun dans le repo Maven local
COPY common-entities/src/ common-entities/src/
RUN mvn clean install -pl common-entities -DskipTests

# Pré-télécharger les dépendances du module Spring MVC
RUN mvn dependency:go-offline -pl variant-c-springmvc -am || true

# Copier le code source du module Spring MVC et construire le jar
COPY variant-c-springmvc/src/ variant-c-springmvc/src/
RUN mvn clean package -pl variant-c-springmvc -am -DskipTests

# Image finale légère
FROM eclipse-temurin:21-jre
WORKDIR /app

# Installer curl pour les health checks
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

# Copier le JAR généré
COPY --from=builder /app/variant-c-springmvc/target/variant-c-springmvc-1.0.0.jar app.jar

EXPOSE 8080

# Health check pour Spring Actuator
HEALTHCHECK --interval=30s --timeout=10s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

# Variables d'environnement pour la configuration
ENV SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/benchmark_db
ENV SPRING_DATASOURCE_USERNAME=benchmark_user
ENV SPRING_DATASOURCE_PASSWORD=benchmark_pass

CMD ["java", "-jar", "app.jar"]