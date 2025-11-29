#!/bin/bash

echo "🐳 TechBank Docker Compose Teardown"
echo "===================================="

PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

docker-compose -f "$PROJECT_ROOT/docker-compose.yml" down -v

echo "✅ Docker Compose services stopped and cleaned up!"
echo ""
echo "To restart: bash scripts/docker-compose-up.sh"
