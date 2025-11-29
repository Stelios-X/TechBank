# TechBank Minikube Quick Reference

One-page cheat sheet for common Minikube operations with TechBank.

## Initial Setup (One-time)

```bash
# 1. Verify prerequisites
java -version && mvn -version && docker --version && minikube version && kubectl version --client

# 2. Build services
bash scripts/build.sh

# 3. Start Minikube
minikube start --cpus=4 --memory=8192

# 4. Deploy to Minikube
bash scripts/deploy-minikube.sh

# 5. Verify deployment
kubectl get pods -n techbank  # All should be Running (1/1)
```

## Starting Services (After Minikube Stop)

```bash
# Start Minikube VM
minikube start

# Set Docker environment (per terminal session)
eval $(minikube docker-env)

# Verify pods are running
kubectl get pods -n techbank
```

## Access Services

```bash
# API Gateway (main entry point) - Port 8000
kubectl port-forward -n techbank svc/api-gateway 8000:8000
# Visit: http://localhost:8000

# Account Service - Port 8001
kubectl port-forward -n techbank svc/account-service 8001:8001

# Transaction Service - Port 8002
kubectl port-forward -n techbank svc/transaction-service 8002:8002

# PostgreSQL Database - Port 5432
kubectl port-forward -n techbank svc/postgres-service 5432:5432
```

## Monitoring & Logs

```bash
# Watch pods in real-time
kubectl get pods -n techbank -w

# Watch deployments
kubectl get deployments -n techbank -w

# View logs from a specific service
kubectl logs -n techbank -l app=api-gateway
kubectl logs -n techbank -l app=account-service
kubectl logs -n techbank -l app=transaction-service

# Follow logs (tail -f)
kubectl logs -f -n techbank -l app=api-gateway

# View last 50 lines
kubectl logs -n techbank -l app=api-gateway --tail=50

# View events (what happened recently)
kubectl get events -n techbank --sort-by='.lastTimestamp'

# Check resource usage
kubectl top nodes
kubectl top pods -n techbank

# View pod details
kubectl describe pod -n techbank <pod-name>

# Open Kubernetes Dashboard
minikube dashboard
```

## Scaling & Updates

```bash
# Scale Account Service to 3 replicas
kubectl scale deployment account-service -n techbank --replicas=3

# Scale back to 1
kubectl scale deployment account-service -n techbank --replicas=1

# Update deployment image (after rebuilding Docker image)
eval $(minikube docker-env)
docker build -t account-service:1.0.1 services/account-service
kubectl set image deployment/account-service account-service=account-service:1.0.1 -n techbank

# Monitor rollout
kubectl rollout status deployment/account-service -n techbank

# Rollback if update fails
kubectl rollout undo deployment/account-service -n techbank
```

## Debugging

```bash
# Enter container (execute shell)
kubectl exec -it <pod-name> -n techbank -- /bin/sh
kubectl exec -it <pod-name> -n techbank -- bash

# Query PostgreSQL database
kubectl exec -it postgres-0 -n techbank -- psql -U postgres -c "SELECT * FROM accounts;"

# Port-forward then connect directly (from another terminal)
kubectl port-forward -n techbank svc/postgres-service 5432:5432
psql -h localhost -U postgres  # password: postgres

# Check service endpoints
kubectl get endpoints -n techbank
kubectl describe svc api-gateway -n techbank

# Check ConfigMaps
kubectl get configmaps -n techbank
kubectl describe configmap account-service-config -n techbank
```

## Testing API Endpoints

```bash
# Create account
curl -X POST "http://localhost:8000/api/v1/accounts/create?accountNumber=ACC001&accountHolder=John%20Doe"

# Get account
curl http://localhost:8000/api/v1/accounts/ACC001

# Deposit money
curl -X POST "http://localhost:8000/api/v1/accounts/ACC001/deposit?amount=1000"

# Withdraw money
curl -X POST "http://localhost:8000/api/v1/accounts/ACC001/withdraw?amount=500"

# Check balance
curl http://localhost:8000/api/v1/accounts/ACC001/balance

# Create transaction
curl -X POST "http://localhost:8000/api/v1/transactions?sourceAccount=ACC001&destinationAccount=ACC002&amount=100"

# Get all accounts
curl http://localhost:8000/api/v1/accounts

# Get all transactions for account
curl http://localhost:8000/api/v1/transactions/source/ACC001
```

## Troubleshooting

```bash
# Check Minikube status
minikube status

# Restart Minikube
minikube stop
minikube start --cpus=4 --memory=8192

# Full reset (deletes everything)
minikube delete
minikube start --cpus=4 --memory=8192
bash scripts/deploy-minikube.sh

# View Minikube logs
minikube logs -f

# Connect to Minikube VM
minikube ssh

# Check Docker inside Minikube
minikube ssh
docker images
docker ps
exit

# Increase verbosity for debugging
minikube start --cpus=4 --memory=8192 -v=5
```

## Cleanup

```bash
# Delete TechBank namespace (keeps Minikube running)
kubectl delete namespace techbank

# Rebuild TechBank
bash scripts/deploy-minikube.sh

# Stop Minikube (keeps state for quick restart)
minikube stop

# Full cleanup (deletes VM)
minikube delete

# Restart fresh
minikube start --cpus=4 --memory=8192
bash scripts/deploy-minikube.sh
```

## Environment Variables (set per terminal session)

```bash
# Configure Docker to use Minikube (MUST do this before building images)
eval $(minikube docker-env)

# Verify it's set
echo $DOCKER_HOST  # Should NOT be empty

# Unset (back to host Docker) - only works in current terminal
eval $(minikube docker-env -u)
```

## Helpful Aliases (Add to ~/.bashrc or ~/.zshrc)

```bash
# Add these to your shell profile for quick access

alias mk-start='minikube start --cpus=4 --memory=8192'
alias mk-stop='minikube stop'
alias mk-delete='minikube delete'
alias mk-dash='minikube dashboard'
alias mk-ssh='minikube ssh'

alias k='kubectl'
alias k-pods='kubectl get pods -n techbank'
alias k-deploy='kubectl get deployments -n techbank'
alias k-svc='kubectl get svc -n techbank'
alias k-events='kubectl get events -n techbank --sort-by=".lastTimestamp"'
alias k-logs='kubectl logs -n techbank -l app'
alias k-watch='kubectl get pods -n techbank -w'

# Usage: k-logs=api-gateway (shows logs for api-gateway)
```

## Resource Allocation Guide

```
Development: minikube start --cpus=4 --memory=8192
Testing:    minikube start --cpus=6 --memory=12288
Production-like: minikube start --cpus=8 --memory=16384

Note: Allocate half your system resources to Minikube
```

## Common Port Numbers

- **API Gateway**: 8000 (public entry point)
- **Account Service**: 8001 (direct access)
- **Transaction Service**: 8002 (direct access)
- **PostgreSQL**: 5432 (database)
- **Kubernetes API**: 6443 (internal)

---

For detailed information, see [MINIKUBE_SETUP.md](MINIKUBE_SETUP.md)
