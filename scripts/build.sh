#!/bin/bash

set -e

echo "🏗️  TechBank Build Script"
echo "=========================="

#PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

# Build parent and shared libraries
echo "📦 Building parent POM and shared libraries..."
mvn clean install -f "$PROJECT_ROOT/pom.xml" -DskipTests

# Build each service
echo "🔨 Building Account Service..."
mvn clean package -f "$PROJECT_ROOT/services/account-service/pom.xml" -DskipTests

echo "🔨 Building Transaction Service..."
mvn clean package -f "$PROJECT_ROOT/services/transaction-service/pom.xml" -DskipTests

echo "🔨 Building API Gateway..."
mvn clean package -f "$PROJECT_ROOT/services/api-gateway/pom.xml" -DskipTests

echo "✅ Build completed successfully!"
echo ""
echo "Next steps:"
echo "  1. For local development with Docker Compose: docker-compose up"
echo "  2. For Minikube deployment: bash scripts/deploy-minikube.sh"
