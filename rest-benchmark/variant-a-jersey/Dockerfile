FROM maven:3.9.6-amazoncorretto-21 AS builder
WORKDIR /app

# Copier les fichiers pom.xml
COPY pom.xml .
COPY common-entities/pom.xml common-entities/pom.xml
COPY variant-a-jersey/pom.xml variant-a-jersey/pom.xml
COPY variant-c-springmvc/pom.xml variant-c-springmvc/pom.xml
COPY variant-d-springdata/pom.xml variant-d-springdata/pom.xml

# Copier le code source de common-entities d'abord
COPY common-entities/src/ common-entities/src/

# Builder common-entities d'abord pour l'installer dans le repo local Maven
RUN mvn clean install -pl common-entities -DskipTests

# Maintenant télécharger les dépendances de variant-a-jersey
RUN mvn dependency:go-offline -pl variant-a-jersey || true

# Copier le code source de variant-a-jersey
COPY variant-a-jersey/src/ variant-a-jersey/src/

# Build du projet variant-a-jersey
RUN mvn clean package -pl variant-a-jersey -am -DskipTests

# Image finale
FROM amazoncorretto:21-al2023-jdk
WORKDIR /app

# curl-minimal est déjà installé par défaut

# Copier le JAR depuis l'étape de build
COPY --from=builder /app/variant-a-jersey/target/*.jar app.jar

EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=10s --retries=3 \
  CMD curl -f http://localhost:8080/api/categories || exit 1

CMD ["java", "-jar", "app.jar"]