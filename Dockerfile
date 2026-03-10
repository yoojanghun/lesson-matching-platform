# Docker Image를 만들기 위한 step-by-step 설명서
FROM eclipse-temurin:21-jdk-alpine

WORKDIR /app

COPY build/libs/*-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
