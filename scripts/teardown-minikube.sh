#!/bin/bash

set -e

echo "🧹 TechBank Teardown Script"
echo "============================"

PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

# Delete Kubernetes resources
echo "🗑️  Deleting Kubernetes resources..."
kubectl delete namespace techbank --ignore-not-found=true || true

echo "✅ Teardown completed!"
echo ""
echo "To restart: bash scripts/deploy-minikube.sh"
