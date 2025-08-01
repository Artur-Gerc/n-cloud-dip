FROM openjdk:17-jdk-slim

WORKDIR /app

COPY target/n-cloud-0.0.1-SNAPSHOT.jar cloud-storage.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "cloud-storage.jar"]