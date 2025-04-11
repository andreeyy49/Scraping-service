FROM openjdk:23-oracle

WORKDIR /app

COPY target/discovery-0.0.1-SNAPSHOT.jar discovery.jar
COPY src/main/resources/application.yml ./application.yml

CMD ["java", "-Dspring.config.location=file:/app/application.yml", "-jar", "discovery.jar"]