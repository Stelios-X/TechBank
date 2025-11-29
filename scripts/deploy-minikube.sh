#!/bin/bash

set -e

echo "☸️  TechBank Minikube Deployment Script"
echo "========================================"

PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

# Check if minikube is running
if ! minikube status > /dev/null 2>&1; then
    echo "❌ Minikube is not running. Starting Minikube..."
    echo "   (Starting Kubernetes cluster in a VM/local environment)"
    minikube start --cpus=4 --memory=8192
fi

# Set Docker environment to use Minikube's Docker daemon
# This tells your Docker client to build images INSIDE Minikube's Docker daemon
# instead of on your host machine. This is the key relationship:
# Your host → Minikube's Kubernetes → Minikube's Docker → Containers
eval $(minikube docker-env)
echo "✅ Docker environment configured to use Minikube's Docker daemon"
echo "   (Docker images will be built inside Minikube's Kubernetes cluster)"

# Build services with Minikube Docker daemon
echo "🔨 Building Docker images in Minikube's Docker daemon..."
echo "   (These images are built and stored INSIDE Minikube, not on your host)"
cd "$PROJECT_ROOT/services/account-service"
docker build -t account-service:1.0.0 .

cd "$PROJECT_ROOT/services/transaction-service"
docker build -t transaction-service:1.0.0 .

cd "$PROJECT_ROOT/services/api-gateway"
docker build -t api-gateway:1.0.0 .

echo "✅ Docker images built successfully in Minikube's Docker daemon"

# Apply Kubernetes manifests to orchestrate containers
echo "📋 Applying Kubernetes manifests..."
echo "   (Kubernetes will orchestrate Docker containers using the images we built)"
kubectl apply -f "$PROJECT_ROOT/k8s/namespace.yaml"
kubectl apply -f "$PROJECT_ROOT/k8s/configmaps/"
kubectl apply -f "$PROJECT_ROOT/k8s/database/"
kubectl apply -f "$PROJECT_ROOT/k8s/services/"
kubectl apply -f "$PROJECT_ROOT/k8s/deployments/"

echo "✅ Kubernetes manifests applied! Kubernetes is orchestrating your services."
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
