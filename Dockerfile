FROM eclipse-temurin:17-jre
COPY target/my-app-1.0-SNAPSHOT.jar /opt/app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/opt/app.jar"]