# TechBank - Microservices Banking Platform

A scalable, production-ready banking system built with Java Spring Boot microservices, deployed on Kubernetes with Minikube support. This system demonstrates modern cloud-native architecture patterns including service discovery, API gateway routing, distributed transaction handling, and containerized deployments.

## 📋 Table of Contents

- [Architecture Overview](#architecture-overview)
- [Technology Stack](#technology-stack)
- [Project Structure](#project-structure)
- [Prerequisites](#prerequisites)
- [Quick Start](#quick-start)
- [Local Development](#local-development)
- [Minikube Deployment](#minikube-deployment)
- [API Documentation](#api-documentation)
- [Troubleshooting](#troubleshooting)

## 🏗️ Architecture Overview

TechBank is built as a microservices ecosystem with three core services:

```
┌─────────────────────────────────────────────────────────┐
│                    Client Applications                   │
└──────────────────────┬──────────────────────────────────┘
                       │
                       ▼
        ┌──────────────────────────────┐
        │      API Gateway (8000)      │
        │   (Spring Cloud Gateway)     │
        │  - Request Routing           │
        │  - Load Balancing            │
        │  - Cross-cutting Concerns    │
        └──────────────────────────────┘
                       │
        ┌──────────────┼──────────────┐
        │              │              │
        ▼              ▼              ▼
   ┌─────────────┐ ┌─────────────────────┐
   │   Account   │ │    Transaction      │
   │   Service   │ │      Service        │
   │   (8001)    │ │      (8002)         │
   └──────┬──────┘ └──────────┬──────────┘
          │                   │
          └───────────┬───────┘
                      │
                      ▼
              ┌───────────────┐
              │  PostgreSQL   │
              │   Database    │
              └───────────────┘
```

### Core Services

1. **Account Service** (Port 8001)
   - Manages bank account creation and operations
   - Handles deposits, withdrawals, balance inquiries
   - Provides account lifecycle management
   - Database: PostgreSQL (techbank_accounts)

2. **Transaction Service** (Port 8002)
   - Processes all financial transactions
   - Maintains transaction history
   - Supports pagination and filtering
   - Database: PostgreSQL (techbank_transactions)

3. **API Gateway** (Port 8000)
   - Single entry point for all client requests
   - Routes requests to appropriate microservices
   - Provides security layer and request filtering
   - Implements circuit breaking and resilience patterns

## 🛠️ Technology Stack

### Core Framework
- **Java 21** - Latest LTS version with modern features
- **Spring Boot 3.3.4** - Microservices foundation
- **Spring Cloud 2024.0.0** - Distributed systems patterns

### Key Libraries
- **Spring Cloud Gateway** - API routing and filtering
- **Spring Cloud OpenFeign** - Inter-service REST communication
- **Spring Cloud Kubernetes** - Kubernetes-native service discovery
- **Spring Data JPA** - ORM and database abstraction
- **PostgreSQL** - Relational database
- **Lombok** - Reduce boilerplate code

### DevOps & Infrastructure
- **Docker** - Containerization (multi-stage builds)
- **Kubernetes** - Container orchestration
- **Minikube** - Local Kubernetes development
- **Maven** - Build automation and dependency management

## 📁 Project Structure

```
TechBank/
├── pom.xml                           # Parent POM (manages versions)
├── docker-compose.yml                # Local development orchestration
├── .gitignore                        # Git ignore patterns
│
├── services/                         # Microservices
│   ├── account-service/
│   ├── transaction-service/
│   └── api-gateway/
│
├── shared-libs/                      # Shared code & models
│   └── common-models/
│
├── k8s/                              # Kubernetes manifests
│   ├── namespace.yaml
│   ├── ingress.yaml
│   ├── configmaps/
│   ├── services/
│   ├── deployments/
│   └── database/
│
├── scripts/                          # Automation scripts
│   ├── build.sh
│   ├── docker-compose-up.sh
│   ├── docker-compose-down.sh
│   ├── deploy-minikube.sh
│   ├── teardown-minikube.sh
│   └── init-db.sql
│
└── docs/                             # Documentation
```

## 📦 Prerequisites

### Required
- **Java 21+** - [Download](https://www.oracle.com/java/technologies/downloads/#java21)
- **Maven 3.9+** - [Download](https://maven.apache.org/)
- **Docker Desktop** - [Download](https://www.docker.com/products/docker-desktop)
- **Minikube 1.30+** - [Download](https://minikube.sigs.k8s.io/)
- **kubectl** - Included with Docker Desktop

### Verify Installation
```bash
java -version              # Should show Java 21
mvn -version               # Should show Maven 3.9+
docker --version           # Should show Docker version
minikube version           # Should show Minikube version
kubectl version --client   # Should show kubectl version
```

## 🚀 Quick Start

### Option 1: Local Development with Docker Compose (Fastest)

```bash
# Build all services
bash scripts/build.sh

# Start with Docker Compose
bash scripts/docker-compose-up.sh

# Verify services
docker-compose ps

# Test API Gateway
curl http://localhost:8000/

# Stop services
bash scripts/docker-compose-down.sh
```

### Option 2: Minikube Deployment (Production-like)

```bash
# Deploy to Minikube
bash scripts/deploy-minikube.sh

# Verify pods
kubectl get pods -n techbank

# Access API Gateway
kubectl port-forward -n techbank svc/api-gateway 8000:8000

# Teardown
bash scripts/teardown-minikube.sh
```

## 💻 Local Development

### Running Individual Services

```bash
# Terminal 1 - Start PostgreSQL
docker run --name postgres -e POSTGRES_PASSWORD=postgres -p 5432:5432 postgres:15-alpine

# Terminal 2 - Run Account Service
cd services/account-service && mvn spring-boot:run

# Terminal 3 - Run Transaction Service
cd services/transaction-service && mvn spring-boot:run

# Terminal 4 - Run API Gateway
cd services/api-gateway && mvn spring-boot:run
```

## ☸️ Minikube Deployment

### Architecture: Minikube → Kubernetes → Docker

Minikube creates a lightweight VM (or uses local environment) that runs Kubernetes. Inside that Kubernetes cluster, **Docker is used as the container runtime** to execute your services:

```
Your Host Machine
       ↓
Minikube VM/Local Environment (Virtual Machine)
       ↓
Kubernetes Cluster (Orchestrator)
       ↓
Docker Daemon (Container Runtime)
       ↓
Service Containers (account-service, transaction-service, api-gateway, postgres)
```

### Quick Start

```bash
# 1. Build all services (Maven)
bash scripts/build.sh

# 2. Start Minikube VM with Kubernetes inside
minikube start --cpus=4 --memory=8192

# 3. Deploy to Minikube (builds Docker images & applies Kubernetes manifests)
bash scripts/deploy-minikube.sh

# 4. Access API Gateway
kubectl port-forward -n techbank svc/api-gateway 8000:8000
# Visit http://localhost:8000
```

### Detailed Setup Guide

For comprehensive step-by-step instructions with troubleshooting, see:
📖 **[MINIKUBE_SETUP.md](docs/MINIKUBE_SETUP.md)**

Covers:
- Prerequisites verification
- Detailed architecture explanation
- Phase-by-phase setup walkthrough
- Verification commands
- Common issues & solutions
- Next steps (scaling, updating, monitoring)

### Visual Guides

Prefer visual representations? Check these:
🎯 **[MINIKUBE_FLOWCHARTS.md](docs/MINIKUBE_FLOWCHARTS.md)** - Visual process flows, architecture diagrams, and decision trees
- Setup process flowchart
- Docker environment configuration visualization
- Kubernetes object relationships
- Request flow example
- Pod startup sequence
- Troubleshooting decision tree

### Quick Reference

For a one-page cheatsheet of common commands:
📄 **[MINIKUBE_CHEATSHEET.md](docs/MINIKUBE_CHEATSHEET.md)**

### Common Tasks

```bash
# Monitor pod status (real-time)
kubectl get pods -n techbank -w

# View service logs
kubectl logs -n techbank -l app=api-gateway -f

# Access individual services
kubectl port-forward -n techbank svc/account-service 8001:8001
kubectl port-forward -n techbank svc/transaction-service 8002:8002

# Scale a service
kubectl scale deployment account-service -n techbank --replicas=3

# Open Kubernetes Dashboard
minikube dashboard

# Cleanup
kubectl delete namespace techbank
minikube stop  # or: minikube delete
```

### Scaling Services

```bash
# Scale Account Service to 3 replicas
kubectl scale deployment account-service -n techbank --replicas=3

# Check replicas
kubectl get deployment -n techbank
```

## 📡 API Endpoints

### Account Service
- `POST /api/v1/accounts/create?accountNumber=ACC001&accountHolder=John%20Doe`
- `GET /api/v1/accounts/{accountNumber}`
- `GET /api/v1/accounts`
- `POST /api/v1/accounts/{accountNumber}/deposit?amount=1000.00`
- `POST /api/v1/accounts/{accountNumber}/withdraw?amount=500.00`
- `GET /api/v1/accounts/{accountNumber}/balance`

### Transaction Service
- `POST /api/v1/transactions?sourceAccount=ACC001&destinationAccount=ACC002&amount=100.00`
- `GET /api/v1/transactions/{id}`
- `GET /api/v1/transactions/source/{accountNumber}`
- `GET /api/v1/transactions/destination/{accountNumber}`

## 🐛 Troubleshooting

### Quick Issues

| Error | Solution |
|-------|----------|
| Port 5432 already in use | `docker ps` then `docker stop <container_id>` |
| Connection refused | Wait for services to be ready, check `docker logs` |
| Pod CrashLoopBackOff | Check logs: `kubectl logs <pod> -n techbank` |
| ImagePullBackOff | Build image first: `docker build -t <service>:1.0.0 .` |

### Comprehensive Troubleshooting

For detailed troubleshooting with step-by-step solutions:
🔧 **[TROUBLESHOOTING.md](docs/TROUBLESHOOTING.md)**

Covers 15+ common issues:
- Maven build failures
- Java version mismatches
- Docker/Minikube startup issues
- ImagePullBackOff problems
- Pod crash loop debugging
- Database connection issues
- Network access problems
- Debugging techniques and workflows

## 📚 Additional Resources

- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [Spring Cloud Documentation](https://spring.io/projects/spring-cloud)
- [Kubernetes Documentation](https://kubernetes.io/docs/)
- [Docker Documentation](https://docs.docker.com/)

---

**Version**: 1.0.0 | **Status**: Production Ready | **Last Updated**: November 29, 2024
