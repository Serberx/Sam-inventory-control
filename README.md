![SAMIC - SAM Inventory Control](frontend/themes/samic/components/logo_samic.svg)

# SAM - Inventory Control

A prototype [web application](http://188.245.126.226)  built as part of our diploma project for our project partner, who
needed a web application to list, track, reserve, add, and move their hardware stored at various locations, including
customers.

# Table of Content

- [Requirements](#requirements)
- [Running the application](#running-the-application)
- [Build production build](#build-production-build)
- [Checkout an already running instance](#checkout-an-already-running-instance)
- [Appliaction credentials](#appliaction-credentials)

## Requirements

- Docker (Application tests use [testcontainers](https://testcontainers.com/) that start database containers upon
  testing)
- Oracle database (eg. gvenzl/oracle-xe)
  ```bash
  docker run -d -p 1521:1521 -e ORACLE_PASSWORD=oracle gvenzl/oracle-xe
  ```
- Java

## Running the application

```bash
mvn exec:java -Dexec.mainClass="com.samic.samic.TestApplication" -Dexec.classpathScope="test"
```

which automatically pulls and starts a docker container containing an oracle database.

## Build production build

> If the tests are not skipped when building for production, a oracle database like in section
> [Running the Application](#Running the application) is required.
> Use -DskipTests to skip the tests. eg.
>```bash
> mvn clean package -Pproduction
>```
>or
>```bash
> mvn clean package -Pproduction -DskipTests
>```
>to save some time


To create a production build, call `mvnw clean package -Pproduction` (Windows),
or `./mvnw clean package -Pproduction` (Mac & Linux).
This will build a JAR file with all the dependencies and front-end resources,
ready to be deployed. The file can be found in the `target` folder after the build completes.

Once the JAR file is built, you can run it using
`java -jar target/samic-1.0-SNAPSHOT.jar`

### Docker Container with production build

To build the Dockerized version of the project, run

```bash
mvn clean package -Pproduction
docker build . -t samic:latest
```

Once the Docker image is correctly built, you can test it locally using

```bash
docker network create samic
docker run -d -p 1521:1521 --net=samic -e ORACLE_PASSWORD=oracle --name database --hostname 
database 
gvenzl/oracle-xe
docker run --network=samic --name=samic -d -p 8080:8080 samic:lates
```

Then access the application on http://localhost:8080

#### Remove all containers and network

```bash
docker stop samic database
docker network rm samic
docker rm samic database
docker rm gvenzl/oracle-xe samic
```

## Checkout an already running instance

[https://samic-kuzu.dev](https://samic-kuzu.dev/login)

## Appliaction credentials

The application makes use of multiple roles that have different privileges.
The account below has admin privileges, thus access to all features. Further accounts are available in the data.sql file
which is in folder ````src/main/resources/````

PetHar:admin

## Run containers with "[docker-compose](https://docs.docker.com/reference/compose-file/version-and-name/).yml"

This guide describes the steps required to run two Docker containers, where one container should wait until the other (a
database) is fully up and listening on a specific port. This setup ensures proper coordination between the Oracle
Database and the application, preventing failure due to startup race conditions.

### Overview

The goal of this setup is to ensure the samic-app container only starts after the oracle-db container is fully
initialized and ready to accept connections. This is achieved through the use of a shell script, a properly configured
Docker Compose file, and a systemd service to automate the startup on system boot.

Steps:

1. [Adjust Dockerfile](#adjust-dockerfile)

2. [Create Wait Script for Oracle DB](#create-wait-script-for-oracle-db)

3. [Build Docker Image with Updated Dockerfile](#build-docker-image-with-updated-dockerfile)

4. [Create docker-compose.yml File](#create-docker-composeyml-file)

5. [Create docker-compose Systemd Service](#create-docker-compose-systemd-service)

## Adjust Dockerfile

Create a **Dockerfile** that includes a wait script to delay the application's startup until the Oracle DB is ready.
Below
is the content for the Dockerfile:

```
FROM eclipse-temurin:17-jre
RUN apt-get update && apt-get install -y netcat-openbsd
COPY wait-for-oracle.sh /opt/wait-for-oracle.sh
RUN chmod +x /opt/wait-for-oracle.sh
COPY target/my-app-1.0-SNAPSHOT.jar /opt/app.jar
EXPOSE 8080
ENTRYPOINT ["/opt/wait-for-oracle.sh"]

```

> ### Netcat-openbsd
> In this **Dockerfile**, the following command is included:\
```RUN apt-get update && apt-get install -y netcat-openbsd```\
>
> This command, specifically ```apt-get install -y netcat-openbsd```, ensures that ***netcat-openbsd*** is installed in
> the
> container image during the build process. This allows the nc command to be used in the next step via the script to
> verify the availability of **port 1521** and, consequently, the availability of the ***database***.

## Create Wait Script for Oracle DB

Create a script named **wait-for-oracle.sh** that waits for the Oracle Database to be reachable before starting the
application. This ensures that the application will not attempt to connect before the database on **port 1521** is
ready.

```bash
#!/bin/bash

echo "Waiting for Oracle Database..."

# As long as Oracle is not reachable, retry every 30 seconds
while ! nc -z database 1521; do
echo "Database not available yet, waiting..."
sleep 15
done

echo "Oracle Database is ready, starting application..."
exec java -jar /opt/app.jar
```

Ensure this script is executable. (The **wait-for-oracle.sh** script is located in the root directory and will be
included
in the Docker image during the build process.)

## Build Docker Image with Updated Dockerfile

Build the Docker image with the updated Dockerfile:

```
docker build -t samic-app-v1 .
```

## Create docker-compose.yml File

The `docker-compose.yml` file orchestrates the setup of the Oracle DB and application containers, defining dependencies
and network configurations.

The network is not mandatory; however, a network was configured here to allow both containers to operate within it. The
network name is customizable, and a /28 subnet from [RFC 1918](https://www.rfc-editor.org/rfc/rfc1918) was selected. The container and network names were
explicitly assigned to prevent Docker from appending the directory name where the docker-compose file is located as a
prefix.

Content of `docker-compose.yml`:

```
version: "3"

networks:
  samic-net-1:
    name: samic-net-1
    driver: bridge
    ipam:
      config:
        - subnet: 10.0.10.0/28
          gateway: 10.0.10.1

services:
  oracle-db:
    image: gvenzl/oracle-xe:21
    container_name: database
    environment:
      ORACLE_PASSWORD: test
    ports:
      - 1521:1521
    networks:
      - samic-net-1
    restart: always

  samic-app:
    image: samic-app-v1
    container_name: samic-app
    ports:
      - 80:8080
    networks:
      - samic-net-1
    restart: always

```

## Create docker-compose Systemd Service

To ensure the services start automatically after a server reboot, create a systemd service file at
```/etc/systemd/system/docker-compose-app.service```\
Content of `docker-compose-app.service`:

```
Description=Docker Compose Application Service
After=docker.service
Requires=docker.service

[Service]
Type=simple
RemainAfterExit=true
WorkingDirectory=/root/docker_dir
ExecStart=/usr/bin/docker-compose up -d
ExecStop=/usr/bin/docker-compose down
TimeoutStartSec=0
Restart=always
RestartSec=10

[Install]
WantedBy=multi-user.target
```

Enable and start the systemd service:

```bash
sudo systemctl daemon-reload
sudo systemctl enable docker-compose-app.service
sudo systemctl start docker-compose-app.service
```
