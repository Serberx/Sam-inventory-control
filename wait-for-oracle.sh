#!/bin/bash

echo "Waiting for Oracle Database..."

# As long as oracle is not reachable, retry every 30 seconds
while ! nc -z database 1521; do
  echo "Database not available yet, waiting..."
  sleep 15
done

echo "Oracle Database is ready, starting application..."
exec java -jar /opt/app.jar
