#!/bin/bash

# Variabler
IMAGE_NAME="1straw/reviewservice"
TAG="latest"
DOCKERFILE_PATH="./Dockerfile" # Ändra vid behov
JAR_PATH="target/ReviewService-0.0.1-SNAPSHOT.jar"

# Bygg JAR-fil
echo "🔨 Bygger JAR..."
mvn clean package -DskipTests || { echo "❌ JAR-build misslyckades"; exit 1; }

# Kontrollera om JAR-filen skapades
if [[ ! -f "$JAR_PATH" ]]; then
    echo "❌ JAR-filen saknas! Kontrollera build-processen."
    exit 1
fi

# Bygg Docker-image
echo "🐳 Bygger Docker-image..."
docker build -t "$IMAGE_NAME:$TAG" . || { echo "❌ Docker-build misslyckades"; exit 1; }

# Logga in på Docker Hub
echo "🔑 Logga in på Docker Hub..."
docker login || { echo "❌ Docker Hub-login misslyckades"; exit 1; }

# Tagga Docker-image
echo "🏷️ Taggar imagen..."
docker tag "$IMAGE_NAME:$TAG" "$IMAGE_NAME:$TAG"

# Pusha till Docker Hub
echo "📤 Pushar till Docker Hub..."
docker push "$IMAGE_NAME:$TAG" || { echo "❌ Push till Docker Hub misslyckades"; exit 1; }

echo "✅ Allt klart! Din image finns nu på Docker Hub!"