FROM openjdk:17-slim

WORKDIR /app

# Копируем JAR и конфиг
COPY target/lemmas-service-0.0.1-SNAPSHOT.jar lemmas-service.jar
COPY src/main/resources/application.yml ./application.yml

# Команда запуска
CMD ["java", "-Dspring.config.location=file:/app/application.yml", "-jar", "lemmas-service.jar"]