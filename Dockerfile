# Stage 1: Build the application
FROM eclipse-temurin:21-jdk as builder
WORKDIR /app

# Copy gradle wrapper files first to leverage Docker cache
COPY gradlew .
COPY gradle ./gradle

# Copy build configuration files
COPY build.gradle .
# Uncomment the next line if you have a settings.gradle file
COPY settings.gradle .

# Download dependencies (optional, but can speed up subsequent builds if dependencies don't change often)
# RUN ./gradlew dependencies --no-daemon

# Copy the source code
COPY src ./src

# Build the application and create the fat JAR
# Use --no-daemon for cleaner exit in container/CI environments
RUN ./gradlew build shadowJar --no-daemon

# Stage 2: Create the final runtime image
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

# Copy the fat JAR from the builder stage
# Adjust the JAR name pattern if your shadowJar task produces a different name
COPY --from=builder /app/build/libs/mocker-1.3-SNAPSHOT-fat.jar app.jar

# Expose the port the application will run on (from your start.sh)
EXPOSE 9090

# Define the entry point for running the application
# The port (e.g., 9090) and mock path (e.g., /data/mock) should be passed as arguments
# when running the container (docker run <image> 9090 /data/mock)
ENTRYPOINT ["java", "-jar", "/app/app.jar"]

# Default arguments if none are provided to 'docker run'
# You should replace "/path/to/mock/data" with the actual path you intend to use inside the container
# This path will likely need to be mounted as a volume during 'docker run'
CMD ["9090", "/data/mock"]