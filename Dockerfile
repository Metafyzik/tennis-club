# Use an official Java runtime as a base image
FROM openjdk:21-jdk-slim

# Set working directory in container
WORKDIR /app

# Copy the built Spring Boot JAR file into the container
COPY target/tennisclub-0.0.1-SNAPSHOT.jar app.jar

# Expose the port your app runs on
EXPOSE 8080

# Run the JAR file
ENTRYPOINT ["java", "-jar", "app.jar"]
