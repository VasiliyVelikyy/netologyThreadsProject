FROM eclipse-temurin:21

WORKDIR /app

COPY target/demo-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "--enable-preview", "-jar", "/app/app.jar"]