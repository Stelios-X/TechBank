# TechBank Minikube Setup - Visual Flow Guide

Visual representations of the setup process and architecture.

## Setup Process Flow

```
START
  │
  ├─→ [PREREQUISITES CHECK] ✓
  │     • Java 21
  │     • Maven 3.9+
  │     • Docker
  │     • Minikube
  │     • kubectl
  │
  ├─→ [BUILD PHASE - Run on HOST]
  │     bash scripts/build.sh
  │     ├─ Maven compiles Java code
  │     ├─ Creates JAR files for each service
  │     └─ Output: services/*/target/*.jar
  │
  ├─→ [START MINIKUBE VM]
  │     minikube start --cpus=4 --memory=8192
  │     ├─ Launches lightweight VM
  │     ├─ Initializes Kubernetes cluster inside
  │     ├─ Configures kubectl to reach cluster
  │     └─ Wait: 1-2 minutes on first run
  │
  ├─→ [CONFIGURE DOCKER] - CRITICAL STEP
  │     eval $(minikube docker-env)
  │     ├─ Points Docker CLI to Minikube's Docker daemon
  │     ├─ Future docker commands run INSIDE Minikube
  │     ├─ NOT on your host machine
  │     └─ Note: Per-terminal-session only!
  │
  ├─→ [BUILD DOCKER IMAGES] - Inside Minikube
  │     bash scripts/deploy-minikube.sh (step 1-2)
  │     ├─ docker build account-service:1.0.0
  │     ├─ docker build transaction-service:1.0.0
  │     ├─ docker build api-gateway:1.0.0
  │     └─ Output: Images stored inside Minikube's Docker
  │
  ├─→ [DEPLOY TO KUBERNETES] - Inside Minikube
  │     bash scripts/deploy-minikube.sh (step 3+)
  │     ├─ kubectl apply -f k8s/namespace.yaml
  │     ├─ kubectl apply -f k8s/configmaps/
  │     ├─ kubectl apply -f k8s/database/
  │     ├─ kubectl apply -f k8s/services/
  │     ├─ kubectl apply -f k8s/deployments/
  │     └─ Kubernetes spins up containers
  │
  ├─→ [WAIT FOR PODS] - Monitor startup
  │     kubectl get pods -n techbank -w
  │     ├─ postgres-0: Pending → ContainerCreating → Running
  │     ├─ account-service-*: Pending → Running
  │     ├─ transaction-service-*: Pending → Running
  │     └─ api-gateway-*: Pending → Running
  │
  ├─→ [PORT FORWARD] - Access from host
  │     kubectl port-forward -n techbank svc/api-gateway 8000:8000
  │     ├─ Maps host:8000 → Minikube's api-gateway:8000
  │     ├─ Now accessible at http://localhost:8000
  │     └─ Keep terminal running for this
  │
  ├─→ [TEST APIs]
  │     curl http://localhost:8000/api/v1/accounts/create?...
  │     ├─ Request reaches your host:8000
  │     ├─ Port-forward sends to Minikube:8000
  │     ├─ API Gateway routes to Account Service
  │     ├─ Account Service queries PostgreSQL
  │     └─ Response flows back
  │
  └─→ SUCCESS! 🎉
      TechBank is running on Minikube
```

## Docker Environment Configuration

This is the "magic" step that many find confusing:

```
[HOST MACHINE - Terminal 1]                 [HOST MACHINE - Terminal 2]
┌──────────────────────────────┐            ┌──────────────────────────────┐
│ Terminal 1: WITH Docker Env  │            │ Terminal 2: WITHOUT Docker   │
│ $ eval $(minikube docker-env)│            │ (different terminal)         │
│ $ docker images              │            │ $ docker images              │
│ account-service:1.0.0 ✓      │            │ (no custom images) ✗         │
│ transaction-service:1.0.0 ✓  │            │ (only base images)           │
│ api-gateway:1.0.0 ✓          │            │                              │
│                              │            │ This terminal talks to your  │
│ This terminal talks to:      │            │ HOST's Docker daemon!        │
│ MINIKUBE's Docker ✓          │            │                              │
└──────────────────────────────┘            └──────────────────────────────┘
         ↓                                             ↓
    [MINIKUBE VM]                              [HOST MACHINE]
    Inside Kubernetes                         Docker Desktop
    Docker Daemon                             Docker Daemon
    (contains our images)                     (different images)
```

## Architecture Layers

```
┌─────────────────────────────────────────────────────────────────────┐
│ LAYER 7: Your Application (Web Requests)                            │
│ curl http://localhost:8000/api/v1/accounts                          │
└──────────────────────────────────┬──────────────────────────────────┘
                                   │
┌──────────────────────────────────▼──────────────────────────────────┐
│ LAYER 6: Port Forwarding (kubectl)                                  │
│ localhost:8000 → Minikube:8000                                      │
└──────────────────────────────────┬──────────────────────────────────┘
                                   │
┌──────────────────────────────────▼──────────────────────────────────┐
│ LAYER 5: Minikube Network                                           │
│ Internal routing within VM                                          │
└──────────────────────────────────┬──────────────────────────────────┘
                                   │
┌──────────────────────────────────▼──────────────────────────────────┐
│ LAYER 4: Kubernetes Service (ClusterIP/NodePort)                   │
│ api-gateway:8000 (routes traffic to pods)                          │
└──────────────────────────────────┬──────────────────────────────────┘
                                   │
┌──────────────────────────────────▼──────────────────────────────────┐
│ LAYER 3: Kubernetes Deployment (Pod Management)                     │
│ Maintains 2 replicas of each service pod                            │
└──────────────────────────────────┬──────────────────────────────────┘
                                   │
┌──────────────────────────────────▼──────────────────────────────────┐
│ LAYER 2: Docker Containers (Container Runtime)                      │
│ ┌────────────────┐  ┌────────────────┐  ┌────────────────┐          │
│ │ api-gateway    │  │ account-service│  │ transaction-srv│          │
│ └────────────────┘  └────────────────┘  └────────────────┘          │
│ (Java Process)      (Java Process)      (Java Process)              │
└──────────────────────────────────┬──────────────────────────────────┘
                                   │
┌──────────────────────────────────▼──────────────────────────────────┐
│ LAYER 1: Minikube VM (Host OS for Kubernetes)                       │
│ Runs Linux kernel, Docker daemon, Kubernetes components             │
└─────────────────────────────────────────────────────────────────────┘
```

## Request Flow Example

User makes a request to create an account:

```
USER's HOST MACHINE
│
│ curl -X POST "http://localhost:8000/api/v1/accounts/create?accountNumber=ACC001..."
│
├─→ Request hits localhost:8000
│
├─→ Port-forward intercepts: localhost:8000 → Minikube:8000
│
├─→ INSIDE MINIKUBE VM
│   │
│   ├─→ Request reaches Kubernetes Service (api-gateway)
│   │
│   ├─→ Service routes to one of the api-gateway pods
│   │
│   ├─→ API Gateway (Spring Cloud Gateway) receives request
│   │   ├─ Sees path: /api/v1/accounts/create
│   │   └─ Routes to: http://account-service:8001
│   │
│   ├─→ Request forwarded to Account Service pod (Kubernetes DNS resolves account-service)
│   │
│   ├─→ Account Service (Java application)
│   │   ├─ Receives request
│   │   ├─ Creates Account entity
│   │   ├─ Connects to PostgreSQL: jdbc:postgresql://postgres-service:5432/techbank_accounts
│   │   ├─ Inserts into database
│   │   └─ Returns HTTP response with account details
│   │
│   ├─→ Response flows back through API Gateway
│   │
│   └─→ Response exits Minikube VM
│
├─→ Port-forward receives response from Minikube:8000
│
└─→ Response delivered to localhost:8000 (your browser/curl)

FINAL: User sees 200 OK with account details ✓
```

## Pod Startup Sequence

```
Time 0s: kubectl apply -f k8s/deployments/
        │
        ├─ Deployment: account-service-deployment.yaml
        ├─ Deployment: transaction-service-deployment.yaml
        ├─ Deployment: api-gateway-deployment.yaml
        │
        ├─ Status: 0/2 Ready (Pending)
        │
        ├─ Kubernetes scheduler assigns pods to nodes
        │
Time ~5s: Status: 0/2 ContainerCreating
        │ Docker starts pulling container images
        │ JVM starts inside each container
        │ Spring Boot initializes
        │
Time ~15s: Status: 1/2 Ready
        │ One pod becomes ready
        │ One pod still initializing
        │
Time ~30s: Status: 2/2 Ready ✓
        │ All pods running and ready
        │ Services accessible

Timeline can vary based on:
- First run (Docker needs to pull postgres:15-alpine image)
- System resources available
- Network speed
- Application initialization time (Spring Boot startup)
```

## Kubernetes Object Relationships

```
┌─────────────────────────────────────────────────────────────┐
│ Namespace: techbank (logical grouping)                      │
│                                                             │
│ ┌─────────────────────────────────────────────────────────┐│
│ │ Service: api-gateway (NodePort: 30000)                  ││
│ │ ├─ Maps external port 8000 to internal 8000             ││
│ │ ├─ Selector: app=api-gateway                            ││
│ │ └─ Routes to pods matching selector                     ││
│ │     ├─ Pod: api-gateway-5c6d4f9b8b-xxxxx                ││
│ │     └─ Pod: api-gateway-5c6d4f9b8b-yyyyy                ││
│ └─────────────────────────────────────────────────────────┘│
│                                                             │
│ ┌─────────────────────────────────────────────────────────┐│
│ │ Service: account-service (ClusterIP)                    ││
│ │ ├─ Internal DNS: account-service.techbank.svc.cluster   ││
│ │ ├─ Selector: app=account-service                        ││
│ │ └─ Routes to pods:                                       ││
│ │     ├─ Pod: account-service-7c3d9f8k2l-xxxxx            ││
│ │     └─ Pod: account-service-7c3d9f8k2l-yyyyy            ││
│ └─────────────────────────────────────────────────────────┘│
│                                                             │
│ ┌─────────────────────────────────────────────────────────┐│
│ │ Service: transaction-service (ClusterIP)                ││
│ │ ├─ Internal DNS: transaction-service.techbank.svc...    ││
│ │ ├─ Selector: app=transaction-service                    ││
│ │ └─ Routes to pods:                                       ││
│ │     ├─ Pod: transaction-service-8x2m9k1p3q-xxxxx        ││
│ │     └─ Pod: transaction-service-8x2m9k1p3q-yyyyy        ││
│ └─────────────────────────────────────────────────────────┘│
│                                                             │
│ ┌─────────────────────────────────────────────────────────┐│
│ │ StatefulSet: postgres-0 (single instance)               ││
│ │ ├─ Service: postgres-service (ClusterIP: None)          ││
│ │ ├─ Internal DNS: postgres-0.postgres-service.techbank   ││
│ │ ├─ Persistent Volume (5Gi storage)                      ││
│ │ └─ Pod: postgres-0                                      ││
│ │     └─ Container: PostgreSQL 15-alpine                  ││
│ └─────────────────────────────────────────────────────────┘│
│                                                             │
│ ConfigMaps:                                                 │
│ ├─ postgres-config (credentials)                           │
│ ├─ account-service-config (application.properties)         │
│ ├─ transaction-service-config (application.properties)     │
│ └─ api-gateway-config (application.properties)             │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

## Troubleshooting Decision Tree

```
ISSUE: Pods not running
│
├─→ kubectl get pods -n techbank
│   ├─ Status: "Pending"
│   │   └─ Likely cause: Node resources (CPU/Memory)
│   │       └─ Solution: kubectl top nodes, increase Minikube allocation
│   │
│   ├─ Status: "ImagePullBackOff"
│   │   └─ Likely cause: Docker images not in Minikube
│   │       ├─ Did you run: eval $(minikube docker-env) ?
│   │       ├─ Did you build images: docker build ... ?
│   │       └─ Solution: Rebuild: bash scripts/deploy-minikube.sh
│   │
│   ├─ Status: "CrashLoopBackOff"
│   │   └─ Likely cause: Application error
│   │       ├─ kubectl logs -n techbank <pod-name>
│   │       ├─ Check database connection
│   │       ├─ Check port configuration
│   │       └─ Solution: Fix application, rebuild image
│   │
│   └─ Status: "Running" but service unavailable
│       └─ Likely cause: Application not responding
│           ├─ kubectl exec <pod> -- curl http://localhost:8000
│           ├─ Check application logs
│           └─ Wait longer (Spring Boot startup time)
│
└─→ API Gateway timeout
    ├─ Is port-forward running?
    │   └─ kubectl port-forward -n techbank svc/api-gateway 8000:8000
    │
    ├─ Is api-gateway pod running?
    │   └─ kubectl get pods -n techbank | grep api-gateway
    │
    ├─ Can you reach pod directly?
    │   └─ kubectl exec <api-gateway-pod> -- curl http://localhost:8000/
    │
    └─ Check service exists
        └─ kubectl get svc -n techbank
```

## Commands Decision Tree

```
GOAL: Monitor what's happening
├─→ Watch pods
│   └─ kubectl get pods -n techbank -w
│
├─→ See detailed pod info
│   └─ kubectl describe pod <name> -n techbank
│
├─→ Check pod logs
│   └─ kubectl logs -n techbank -l app=api-gateway
│
├─→ See all events
│   └─ kubectl get events -n techbank --sort-by=.lastTimestamp
│
└─→ Check resource usage
    └─ kubectl top pods -n techbank


GOAL: Access services
├─→ From local machine
│   ├─ kubectl port-forward -n techbank svc/api-gateway 8000:8000
│   └─ curl http://localhost:8000
│
├─→ From inside a pod
│   ├─ kubectl exec -it <pod> -n techbank -- bash
│   └─ curl http://api-gateway:8000
│
└─→ From Minikube VM
    ├─ minikube ssh
    └─ docker ps


GOAL: Update and restart
├─→ Change code
│   ├─ Edit services/*/src/...
│   └─ bash scripts/build.sh
│
├─→ Rebuild Docker image
│   ├─ eval $(minikube docker-env)
│   └─ docker build -t account-service:1.0.1 services/account-service
│
├─→ Update Kubernetes
│   ├─ kubectl set image deployment/account-service \
│   │   account-service=account-service:1.0.1 -n techbank
│   └─ kubectl rollout status deployment/account-service -n techbank
│
└─→ Verify update
    └─ kubectl logs -f -n techbank -l app=account-service
```

---

**For detailed commands and examples, see:**
- 📖 [MINIKUBE_SETUP.md](MINIKUBE_SETUP.md) - Comprehensive guide
- 📄 [MINIKUBE_CHEATSHEET.md](MINIKUBE_CHEATSHEET.md) - Quick reference
