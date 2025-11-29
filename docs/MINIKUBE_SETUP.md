# TechBank Minikube Setup Guide

Complete step-by-step instructions for deploying TechBank on Minikube locally.

## Table of Contents
1. [Prerequisites](#prerequisites)
2. [Architecture Overview](#architecture-overview)
3. [Step-by-Step Setup](#step-by-step-setup)
4. [Verification](#verification)
5. [Common Issues & Troubleshooting](#troubleshooting)
6. [Next Steps](#next-steps)

---

## Prerequisites

Before you begin, ensure you have all required tools installed and working:

### 1. Java 21
```bash
java -version
# Should output: openjdk version "21.x.x" or similar
```

**If not installed:**
- Linux (Arch): `sudo pacman -S jdk21-openjdk`
- macOS: `brew install openjdk@21`
- Windows: Download from [oracle.com](https://www.oracle.com/java/technologies/downloads/#java21)

### 2. Maven 3.9+
```bash
mvn -version
# Should output: Apache Maven 3.9.x or higher
```

**If not installed:**
- Linux (Arch): `sudo pacman -S maven`
- macOS: `brew install maven`
- Windows: Download from [maven.apache.org](https://maven.apache.org/)

### 3. Docker Desktop
```bash
docker --version
# Should output: Docker version 24.x.x or higher

docker info
# Verify Docker daemon is running
```

**If not installed:**
- Download [Docker Desktop](https://www.docker.com/products/docker-desktop)
- Ensure it's running before continuing

### 4. Minikube 1.30+
```bash
minikube version
# Should output: minikube version: v1.30.x or higher
```

**If not installed:**
- Linux: `curl -LO https://github.com/kubernetes/minikube/releases/latest/download/minikube-linux-amd64 && chmod +x minikube-linux-amd64 && sudo mv minikube-linux-amd64 /usr/local/bin/minikube`
- macOS: `brew install minikube`
- Windows: Download from [minikube.sigs.k8s.io](https://minikube.sigs.k8s.io/)

### 5. kubectl (Kubernetes CLI)
```bash
kubectl version --client
# Should output: Client version v1.x.x or higher
```

**If not installed:**
- Usually included with Docker Desktop
- Or: `brew install kubectl` (macOS) / `sudo pacman -S kubectl` (Arch Linux)

### Verification Checklist
```bash
# Run all these commands to verify your environment
echo "=== Java ===" && java -version
echo "=== Maven ===" && mvn -version
echo "=== Docker ===" && docker --version && docker info | grep "Server Version"
echo "=== Minikube ===" && minikube version
echo "=== kubectl ===" && kubectl version --client
```

All should output without errors before proceeding.

---

## Architecture Overview

Understanding the setup will help you troubleshoot issues:

```
┌─────────────────────────────────────────────────────┐
│           YOUR HOST MACHINE (Arch Linux)            │
│  ┌──────────────────────────────────────────────┐  │
│  │  Minikube VM (Lightweight Virtual Machine)   │  │
│  │                                              │  │
│  │  ┌────────────────────────────────────────┐ │  │
│  │  │  Kubernetes Cluster (Master & Worker)  │ │  │
│  │  │                                        │ │  │
│  │  │  ┌──────────────────────────────────┐ │ │  │
│  │  │  │  Docker Daemon (Container Mgmt)  │ │ │  │
│  │  │  │                                  │ │ │  │
│  │  │  │  ┌────────────────────────────┐ │ │ │  │
│  │  │  │  │ account-service (Pod)      │ │ │ │  │
│  │  │  │  └────────────────────────────┘ │ │ │  │
│  │  │  │  ┌────────────────────────────┐ │ │ │  │
│  │  │  │  │ transaction-service (Pod)  │ │ │ │  │
│  │  │  │  └────────────────────────────┘ │ │ │  │
│  │  │  │  ┌────────────────────────────┐ │ │ │  │
│  │  │  │  │ api-gateway (Pod)          │ │ │ │  │
│  │  │  │  └────────────────────────────┘ │ │ │  │
│  │  │  │  ┌────────────────────────────┐ │ │ │  │
│  │  │  │  │ postgres (Pod)             │ │ │ │  │
│  │  │  │  └────────────────────────────┘ │ │ │  │
│  │  │  └──────────────────────────────────┘ │ │  │
│  │  └────────────────────────────────────────┘ │  │
│  └──────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────┘
         ↑ kubectl communicates with Kubernetes
         ↑ (minikube docker-env) Docker commands reach Minikube's Docker
```

### Key Points:
- **Minikube** creates an isolated VM with Kubernetes
- **Kubernetes** orchestrates container deployment
- **Docker** (inside Minikube) provides the container runtime
- Your host communicates with this VM via `kubectl` and docker CLI

---

## Step-by-Step Setup

### Phase 1: Prepare Your Project

#### Step 1.1: Navigate to Project
```bash
cd /home/brandon/projects/TechBank
pwd
# Verify you're in: /home/brandon/projects/TechBank
```

#### Step 1.2: Verify Project Structure
```bash
ls -la
# Should see: pom.xml, docker-compose.yml, k8s/, scripts/, services/
```

#### Step 1.3: Build All Services (Maven)
This compiles Java code and creates JAR files needed for Docker images.

```bash
# Clean any previous builds and compile everything
bash scripts/build.sh

# Expected output:
# 📦 Building parent POM and shared libraries...
# 🔨 Building Account Service...
# 🔨 Building Transaction Service...
# 🔨 Building API Gateway...
# ✅ Build completed successfully!
```

**What this does:**
- Compiles Account Service → `target/account-service-1.0.0.jar`
- Compiles Transaction Service → `target/transaction-service-1.0.0.jar`
- Compiles API Gateway → `target/api-gateway-1.0.0.jar`

⏱️ **Time**: ~2-3 minutes (first run may take longer if downloading dependencies)

**If build fails:**
```bash
# Check Java is working
java -version

# Check Maven can reach repositories
mvn help:active-profiles

# Try building just one service to isolate issues
cd services/account-service
mvn clean package -DskipTests
```

---

### Phase 2: Start Minikube

#### Step 2.1: Start Minikube VM
```bash
minikube start --cpus=4 --memory=8192

# Expected output:
# 😄  minikube v1.30.x on Arch (amd64)
# ✨  Automatically selected the docker driver
# 📦  Docker is now configured to use minikube...
# ✅  Kubernetes 1.27.x is now running
```

**What this does:**
- Creates a virtual machine with Kubernetes inside
- Allocates 4 CPU cores and 8GB RAM to the VM
- Initializes Kubernetes cluster
- Configures `kubectl` to communicate with it

**Wait for it to complete** (1-2 minutes on first run)

#### Step 2.2: Verify Minikube is Running
```bash
minikube status
# Expected output:
# minikube
# type: Control Plane
# host: Running
# kubelet: Running
# apiserver: Running
# kubeconfig: Configured

kubectl version --client
# Should show client version

kubectl get nodes
# Should show: minikube (Status: Ready)
```

---

### Phase 3: Build Docker Images in Minikube

#### Step 3.1: Configure Docker to Use Minikube
```bash
# This is the magic step - tells Docker CLI to communicate with Minikube's Docker
eval $(minikube docker-env)

# Verify the configuration
echo $DOCKER_HOST
# Should show something like: unix:///var/folders/.../ or /run/user/.../docker.sock
```

**Important**: This configuration **only lasts for your current terminal session**. If you open a new terminal, you'll need to run this again.

#### Step 3.2: Build Docker Images Inside Minikube
```bash
# Navigate to project root
cd /home/brandon/projects/TechBank

# Build all Docker images INSIDE Minikube
docker-compose config  # Optional: verify docker-compose is working
docker build -t account-service:1.0.0 services/account-service
docker build -t transaction-service:1.0.0 services/transaction-service
docker build -t api-gateway:1.0.0 services/api-gateway

# Or use the helper script which does all three:
cd /home/brandon/projects/TechBank
eval $(minikube docker-env)  # Ensure Docker env is set
bash scripts/deploy-minikube.sh  # This builds and deploys
```

#### Step 3.3: Verify Images are in Minikube
```bash
docker images
# Should show:
# account-service      1.0.0    ...    XX MB
# transaction-service  1.0.0    ...    XX MB
# api-gateway          1.0.0    ...    XX MB
# postgres             15-alpine ...   XX MB (pulled automatically)
```

**Important**: These images are **inside Minikube**, not on your host machine. Verify:
```bash
# Inside Minikube
minikube ssh
docker images
exit

# NOT on your host (if you have Docker running separately)
# Open a NEW terminal without eval $(minikube docker-env)
docker images  # Won't see our custom images here
```

---

### Phase 4: Deploy to Kubernetes

#### Step 4.1: Create Kubernetes Namespace
```bash
kubectl apply -f k8s/namespace.yaml

# Verify
kubectl get namespaces
# Should see: default, kube-system, kube-public, kube-node-lease, techbank
```

#### Step 4.2: Create ConfigMaps (Application Configuration)
```bash
kubectl apply -f k8s/configmaps/

# Verify
kubectl get configmaps -n techbank
# Should see: account-service-config, api-gateway-config, postgres-config, transaction-service-config
```

#### Step 4.3: Deploy Database (PostgreSQL)
```bash
kubectl apply -f k8s/database/

# Verify - this may take a minute to initialize
kubectl get pods -n techbank -l app=postgres
# Should eventually show: postgres-0 (1/1 Running)

# Wait for database to be ready
kubectl wait --for=condition=ready pod -l app=postgres -n techbank --timeout=120s

# Verify database is accepting connections
kubectl exec -it postgres-0 -n techbank -- psql -U postgres -c "SELECT version();"
```

#### Step 4.4: Deploy Services (Account, Transaction, API Gateway)
```bash
kubectl apply -f k8s/services/
kubectl apply -f k8s/deployments/

# Verify services are created
kubectl get svc -n techbank
# Should see: account-service, api-gateway, postgres-service, transaction-service

# Verify deployments are created
kubectl get deployments -n techbank
# Should see: account-service, api-gateway, transaction-service
```

#### Step 4.5: Wait for All Pods to Be Ready
```bash
# Watch pods starting up
kubectl get pods -n techbank -w

# Expected sequence:
# NAME                                  READY   STATUS
# account-service-5c6d4f9b8b-xxxx     0/1     Pending
# account-service-5c6d4f9b8b-xxxx     0/1     ContainerCreating
# account-service-5c6d4f9b8b-xxxx     1/1     Running
# (same for transaction-service and api-gateway)

# Wait for all to be ready (press Ctrl+C to exit watch)
kubectl wait --for=condition=ready pod -l app=account-service -n techbank --timeout=120s
kubectl wait --for=condition=ready pod -l app=transaction-service -n techbank --timeout=120s
kubectl wait --for=condition=ready pod -l app=api-gateway -n techbank --timeout=120s
```

---

### Phase 5: Access Your Services

#### Step 5.1: Port Forward API Gateway (Main Entry Point)
```bash
# In one terminal, keep this running
kubectl port-forward -n techbank svc/api-gateway 8000:8000

# Output should show:
# Forwarding from 127.0.0.1:8000 -> 8000
# Forwarding from [::1]:8000 -> 8000
```

#### Step 5.2: Test API Gateway (In Another Terminal)
```bash
# Test if gateway is responding
curl http://localhost:8000/

# Create an account
curl -X POST "http://localhost:8000/api/v1/accounts/create?accountNumber=ACC001&accountHolder=John%20Doe"

# Expected response:
# {"accountNumber":"ACC001","accountHolder":"John Doe","balance":0.00,"status":"ACTIVE","createdAt":"2024-11-29T...","updatedAt":"2024-11-29T..."}

# Get account details
curl http://localhost:8000/api/v1/accounts/ACC001

# Deposit money
curl -X POST "http://localhost:8000/api/v1/accounts/ACC001/deposit?amount=1000"

# Check balance
curl http://localhost:8000/api/v1/accounts/ACC001/balance
# Should return: 1000
```

#### Step 5.3: Optional - Access Individual Services
```bash
# Terminal 1: Forward Account Service
kubectl port-forward -n techbank svc/account-service 8001:8001

# Terminal 2: Forward Transaction Service
kubectl port-forward -n techbank svc/transaction-service 8002:8002

# Terminal 3: Access database
kubectl port-forward -n techbank svc/postgres-service 5432:5432

# Now you can connect directly:
# Database: localhost:5432 (user: postgres, password: postgres)
```

---

## Verification

### Check Everything is Running

```bash
# 1. Verify all pods are running
kubectl get pods -n techbank
# All should show: READY 1/1, STATUS Running

# 2. Check deployment replicas
kubectl get deployments -n techbank
# Should show 2/2 READY for account-service, transaction-service, api-gateway

# 3. Check services
kubectl get svc -n techbank
# Should show all services with ClusterIP (except api-gateway: NodePort)

# 4. View logs
kubectl logs -n techbank -l app=api-gateway --tail=50
kubectl logs -n techbank -l app=account-service --tail=50

# 5. Test API endpoint
curl http://localhost:8000/api/v1/accounts

# 6. Check resource usage
kubectl top nodes
kubectl top pods -n techbank
```

### Full Health Check Script

Create a script `scripts/health-check.sh`:
```bash
#!/bin/bash

echo "=== TechBank Health Check ==="
echo ""

echo "1️⃣  Minikube Status:"
minikube status
echo ""

echo "2️⃣  Kubernetes Cluster:"
kubectl cluster-info
echo ""

echo "3️⃣  TechBank Pods:"
kubectl get pods -n techbank
echo ""

echo "4️⃣  Pod Details:"
kubectl describe pods -n techbank | grep -E "Name:|Ready:|Status:|Restarts:"
echo ""

echo "5️⃣  Recent Events:"
kubectl get events -n techbank --sort-by='.lastTimestamp' | tail -10
echo ""

echo "6️⃣  Service Endpoints:"
kubectl get endpoints -n techbank
```

---

## Troubleshooting

### Issue 1: Minikube Won't Start

**Symptom**: `minikube start` fails

**Solutions**:
```bash
# Check if previous Minikube is stuck
minikube status

# Stop and delete old Minikube
minikube stop
minikube delete

# Try starting again
minikube start --cpus=4 --memory=8192 -v=5  # -v=5 shows verbose output

# Check system resources
free -h  # Should have at least 16GB total RAM for Minikube + system
```

### Issue 2: Docker Images Not Found in Minikube

**Symptom**: Pods stuck in `ImagePullBackOff` or `ErrImagePull`

**Causes & Solutions**:
```bash
# 1. You didn't set the Docker environment
# Did you run: eval $(minikube docker-env) ?
echo $DOCKER_HOST  # Should NOT be empty

# 2. You opened a new terminal (Docker env is lost per-session)
eval $(minikube docker-env)
docker images  # Should now show our custom images

# 3. Build script didn't complete successfully
bash scripts/build.sh  # Rerun Maven build

# 4. Verify images actually exist in Minikube
minikube ssh
docker images
exit
```

### Issue 3: Pods Not Starting / CrashLoopBackOff

**Symptom**: `kubectl get pods -n techbank` shows CrashLoopBackOff

**Diagnosis**:
```bash
# Check pod logs
kubectl logs -n techbank <pod-name>

# Describe pod for more details
kubectl describe pod -n techbank <pod-name>

# Common issues:
# - Database connection: Check postgres is running
# - Port conflicts: Check application.properties port numbers
# - Memory issues: Check node resources
kubectl top nodes
kubectl top pods -n techbank

# Restart a pod by deleting it (Kubernetes will recreate it)
kubectl delete pod -n techbank <pod-name>
```

### Issue 4: Database Connection Errors

**Symptom**: Services failing to connect to PostgreSQL

**Diagnosis & Fix**:
```bash
# Verify PostgreSQL is running
kubectl get pods -n techbank -l app=postgres

# Check PostgreSQL logs
kubectl logs -n techbank postgres-0

# Verify database was initialized
kubectl exec -it postgres-0 -n techbank -- psql -U postgres -l
# Should show: techbank_accounts, techbank_transactions databases

# Test connection
kubectl exec -it postgres-0 -n techbank -- psql -U postgres -c "SELECT 1;"

# Verify ConfigMap has correct connection string
kubectl get configmap -n techbank account-service-config -o yaml
# Check: spring.datasource.url should be jdbc:postgresql://postgres-service:5432/techbank_accounts
```

### Issue 5: Can't Access API Gateway

**Symptom**: `curl http://localhost:8000` times out or refuses connection

**Solutions**:
```bash
# Is port-forward running?
# (Check terminal where you ran: kubectl port-forward)

# Start port-forward if not running
kubectl port-forward -n techbank svc/api-gateway 8000:8000

# Check if API Gateway pod is running
kubectl get pods -n techbank -l app=api-gateway

# Check pod logs for startup errors
kubectl logs -n techbank -l app=api-gateway

# Try connecting directly to pod (if it's running)
kubectl exec -it <api-gateway-pod-name> -n techbank -- curl http://localhost:8000/

# Verify service exists
kubectl get svc api-gateway -n techbank
kubectl describe svc api-gateway -n techbank
```

### Issue 6: Out of Memory / Disk Space

**Symptom**: Minikube is slow or pods won't start

**Solutions**:
```bash
# Check Minikube resource allocation
minikube profile list

# Stop and reconfigure with more resources
minikube stop
minikube delete

# Start with more resources (if system has them)
minikube start --cpus=6 --memory=12288  # 6 CPUs, 12GB RAM

# Inside Minikube, check disk usage
minikube ssh
df -h
du -sh /var/lib/docker
exit

# Clean up Minikube (remove unused images/containers)
minikube ssh
docker system prune -a
exit
```

---

## Next Steps

### 1. Scale Services
```bash
# Scale Account Service to 3 replicas
kubectl scale deployment account-service -n techbank --replicas=3

# Check replicas starting
kubectl get deployment account-service -n techbank -w

# Scale back to 1
kubectl scale deployment account-service -n techbank --replicas=1
```

### 2. Monitor in Real-time
```bash
# Use Kubernetes Dashboard (optional)
minikube dashboard
# Opens browser with visual monitoring

# Or use command-line monitoring
watch kubectl get pods -n techbank
watch kubectl top pods -n techbank
```

### 3. Update Services
```bash
# After code changes, rebuild and restart:

# 1. Rebuild Maven
bash scripts/build.sh

# 2. Set Docker environment
eval $(minikube docker-env)

# 3. Rebuild Docker image
docker build -t account-service:1.0.1 services/account-service

# 4. Update Kubernetes deployment
kubectl set image deployment/account-service \
  account-service=account-service:1.0.1 \
  -n techbank

# 5. Monitor rollout
kubectl rollout status deployment/account-service -n techbank
```

### 4. View Dashboard
```bash
# Interactive web dashboard
minikube dashboard

# Or view logs in real-time
kubectl logs -f -n techbank -l app=api-gateway
```

### 5. Cleanup When Done
```bash
# Stop Minikube (keeps VM state)
minikube stop

# Delete everything (clean slate)
minikube delete

# Delete just TechBank namespace
kubectl delete namespace techbank
```

---

## Quick Reference Commands

```bash
# Start everything
minikube start --cpus=4 --memory=8192
eval $(minikube docker-env)
bash scripts/build.sh
bash scripts/deploy-minikube.sh

# Access services
kubectl port-forward -n techbank svc/api-gateway 8000:8000

# Monitor
kubectl get pods -n techbank -w
kubectl logs -f -n techbank -l app=api-gateway

# Cleanup
kubectl delete namespace techbank
minikube stop
```

---

**Happy Deploying! 🚀**
