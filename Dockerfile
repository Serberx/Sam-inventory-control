FROM eclipse-temurin:17-jre
RUN apt-get update && apt-get install -y netcat-openbsd
COPY wait-for-oracle.sh /opt/wait-for-oracle.sh
RUN chmod +x /opt/wait-for-oracle.sh
COPY target/my-app-1.0-SNAPSHOT.jar /opt/app.jar
EXPOSE 8080
ENTRYPOINT ["/opt/wait-for-oracle.sh"]
