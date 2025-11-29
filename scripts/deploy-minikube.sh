#!/bin/bash

set -e

echo "☸️  TechBank Minikube Deployment Script"
echo "========================================"

PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

# Check if minikube is running
if ! minikube status > /dev/null 2>&1; then
    echo "❌ Minikube is not running. Starting Minikube..."
    minikube start --cpus=4 --memory=8192
fi

# Set Docker environment to use Minikube's Docker daemon
eval $(minikube docker-env)
echo "✅ Docker environment set to Minikube"

# Build services with Minikube Docker
echo "🔨 Building Docker images in Minikube..."
cd "$PROJECT_ROOT/services/account-service"
docker build -t account-service:1.0.0 .

cd "$PROJECT_ROOT/services/transaction-service"
docker build -t transaction-service:1.0.0 .

cd "$PROJECT_ROOT/services/api-gateway"
docker build -t api-gateway:1.0.0 .

echo "✅ Docker images built in Minikube"

# Apply Kubernetes manifests
echo "📋 Applying Kubernetes manifests..."
kubectl apply -f "$PROJECT_ROOT/k8s/namespace.yaml"
kubectl apply -f "$PROJECT_ROOT/k8s/configmaps/"
kubectl apply -f "$PROJECT_ROOT/k8s/database/"
kubectl apply -f "$PROJECT_ROOT/k8s/services/"
kubectl apply -f "$PROJECT_ROOT/k8s/deployments/"

echo "✅ Kubernetes manifests applied!"
echo ""
echo "⏳ Waiting for pods to be ready..."
kubectl wait --for=condition=ready pod -l app=postgres -n techbank --timeout=120s || true
kubectl wait --for=condition=ready pod -l app=account-service -n techbank --timeout=120s || true
kubectl wait --for=condition=ready pod -l app=transaction-service -n techbank --timeout=120s || true
kubectl wait --for=condition=ready pod -l app=api-gateway -n techbank --timeout=120s || true

echo "✅ Deployment completed!"
echo ""
echo "📊 Check pod status:"
echo "  kubectl get pods -n techbank"
echo ""
echo "📜 View logs:"
echo "  kubectl logs -n techbank -l app=account-service"
echo "  kubectl logs -n techbank -l app=transaction-service"
echo "  kubectl logs -n techbank -l app=api-gateway"
echo ""
echo "🌐 Access API Gateway:"
echo "  kubectl port-forward -n techbank svc/api-gateway 8000:8000"
echo "  Then visit http://localhost:8000"
echo ""
echo "🗑️  To teardown: bash scripts/teardown-minikube.sh"
