FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY target/gestor-tareas-ci-*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
