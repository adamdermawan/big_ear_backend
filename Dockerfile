FROM openjdk:21-jdk-slim
COPY build/libs/bigear-backend-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "/app.jar"]