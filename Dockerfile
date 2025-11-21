## =====================================================
## Multi-stage build pour application Spring Boot
## Builder: Maven + Temurin JDK 17
## Runtime: Temurin JDK 17 (non-root)
## =====================================================

FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app

# Copier le pom et récupérer les dépendances (cache layer)
COPY pom.xml .
RUN mvn -q -B dependency:go-offline

# Copier le code source
COPY src ./src

# Construction du JAR (skip tests pour rapidité, ajuster si besoin)
RUN mvn -q -B clean package -DskipTests

FROM eclipse-temurin:17-jdk
WORKDIR /app

# Copier le JAR issu du build
COPY --from=build /app/target/*.jar app.jar

# Créer un utilisateur non-root pour la sécurité
RUN groupadd -r spring && useradd -r -g spring spring
USER spring

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]