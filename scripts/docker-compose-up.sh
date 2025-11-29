#!/bin/bash

set -e

echo "🐳 TechBank Docker Compose Setup"
echo "=================================="

PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

# Check if Docker is running
if ! docker info > /dev/null 2>&1; then
    echo "❌ Docker is not running. Please start Docker and try again."
    exit 1
fi

# Build Docker images
echo "🔨 Building Docker images..."
docker-compose -f "$PROJECT_ROOT/docker-compose.yml" build

# Start services
echo "🚀 Starting services with Docker Compose..."
docker-compose -f "$PROJECT_ROOT/docker-compose.yml" up -d

echo "✅ Services started!"
echo ""
echo "📋 Service URLs:"
echo "  - API Gateway: http://localhost:8000"
echo "  - Account Service: http://localhost:8001/api/v1/accounts"
echo "  - Transaction Service: http://localhost:8002/api/v1/transactions"
echo ""
echo "To view logs: docker-compose -f $PROJECT_ROOT/docker-compose.yml logs -f"
echo "To stop services: docker-compose -f $PROJECT_ROOT/docker-compose.yml down"
