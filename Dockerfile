
# ── Stage 1 : Build ──
FROM maven:3.9-eclipse-temurin-21 AS builder

WORKDIR /app

# Copie pom.xml en premier pour profiter du cache Docker
# (les dépendances Maven ne sont re-téléchargées que si pom.xml change)
COPY pom.xml .
RUN mvn dependency:go-offline -q

# Copie le code source et compile
COPY src ./src
RUN mvn clean package -DskipTests -q

# ── Stage 2 : Run ──
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Copie uniquement le JAR depuis le stage builder
COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8888

ENTRYPOINT ["java", "-jar", "app.jar"]