#!/bin/bash

# Exit on error
set -e

echo "Building and starting Mocker application..."

# Navigate to the project directory (in case script is run from elsewhere)
cd "$(dirname "$0")"

# Build the project using Gradle wrapper
./gradlew clean build shadowJar

echo "Build complete. Starting application..."

# Run the application using the fat JAR
java -jar build/libs/mocker-1.3-SNAPSHOT-fat.jar 9090 /Users/ecacho/dev/mock