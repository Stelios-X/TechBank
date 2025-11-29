# TechBank Troubleshooting Guide

Quick solutions for common issues when deploying TechBank on Minikube.

## Pre-Deployment Issues

### 1. Maven Build Fails: "Cannot find module"

**Error:**
```
[ERROR] COMPILATION ERROR
[ERROR] /path/to/services/account-service/src/main/java/...
[ERROR] package com.techbank.common does not exist
```

**Solution:**
```bash
# Build from project root (not individual services)
cd /home/brandon/projects/TechBank
bash scripts/build.sh

# Or manually:
mvn clean install -DskipTests
```

**Explanation:** Shared libraries must be built first. The parent pom.xml defines the build order.

---

### 2. Java Version Mismatch: "Unsupported class version"

**Error:**
```
java.lang.UnsupportedClassVersionError: com/techbank/... has a class file version 65.0
```

**Cause:** Java 21 compiled code running on Java 11 or lower

**Solution:**
```bash
# Check your Java version
java -version

# Should output:
# java version "21.x.x"

# If not, update JAVA_HOME
export JAVA_HOME=/path/to/java/21
java -version  # Verify

# Then rebuild
bash scripts/build.sh
```

---

### 3. Docker Build Fails: "Cannot write to image store"

**Error:**
```
failed to solve with frontend dockerfile.v0: failed to read dockerfile
```

**Cause:** Docker daemon not running or permission issue

**Solution:**
```bash
# Start Docker
systemctl start docker  # Linux
# or use Docker Desktop GUI (Mac/Windows)

# Verify Docker is running
docker ps

# If permission denied, add user to docker group
sudo usermod -aG docker $USER
newgrp docker  # Activate group
```

---

## Minikube Issues

### 4. Minikube Start Fails: "Cannot find VM driver"

**Error:**
```
E1129 10:30:45.123456 11111 machine.go:123: Error loading cluster config: 
Error loading driver config: Requested driver 'kvm2' does not exist
```

**Solution - Linux with KVM:**
```bash
# Install KVM driver
curl -LO https://github.com/kubernetes/minikube/releases/latest/download/docker-machine-driver-kvm2
chmod +x docker-machine-driver-kvm2
sudo mv docker-machine-driver-kvm2 /usr/local/bin/

# Start Minikube with KVM
minikube start --driver=kvm2 --cpus=4 --memory=8192
```

**Solution - Arch Linux (recommended):**
```bash
# Use Docker driver (simpler)
minikube start --driver=docker --cpus=4 --memory=8192

# If docker driver not available:
minikube config set driver docker
minikube start --cpus=4 --memory=8192
```

**Solution - Other systems:**
```bash
# Use auto-detection
minikube start --cpus=4 --memory=8192  # Minikube picks best driver

# Or specify explicitly
minikube start --driver=hyperv --cpus=4 --memory=8192     # Windows
minikube start --driver=hyperkit --cpus=4 --memory=8192   # macOS (older)
minikube start --driver=parallels --cpus=4 --memory=8192  # macOS (M-series)
```

---

### 5. Minikube Runs Out of Resources: Pods Stuck in "Pending"

**Error:**
```bash
$ kubectl get pods -n techbank
NAME                                    READY   STATUS    RESTARTS   AGE
account-service-7d4f9c8b5f-xxxxx       0/1     Pending   0          5m
api-gateway-6c9f3d2e1a-xxxxx           0/1     Pending   0          5m
```

**Solution:**
```bash
# Check node resources
kubectl top nodes

# Increase Minikube resources
minikube stop

# Option 1: Increase allocated resources
minikube config set cpus 8
minikube config set memory 12288
minikube start

# Option 2: Delete and restart with new config
minikube delete
minikube start --cpus=8 --memory=12288

# Verify pods now run
kubectl get pods -n techbank -w
```

---

### 6. "error: error loading config file" - kubectl context issue

**Error:**
```
E0129 10:30:00.000000 11111 memcache.go:123: couldn't get current server API group list: Get 
"http://localhost:8080/api?timeout=32s": dial tcp 127.0.0.1:8080: connect: connection refused
```

**Solution:**
```bash
# Check if minikube is running
minikube status

# If not running, start it
minikube start

# Reset kubectl context
minikube kubectl config use-context minikube

# Or set kubectl to use minikube
kubectl config use-context minikube

# Verify
kubectl cluster-info
```

---

## Docker Image Issues

### 7. ImagePullBackOff: Images not found

**Error:**
```bash
$ kubectl get pods -n techbank
NAME                                    READY   STATUS             RESTARTS   AGE
account-service-7d4f9c8b5f-xxxxx       0/1     ImagePullBackOff   0          2m
```

**Root Cause:** Docker images were built on host machine, not in Minikube

**Solution:**
```bash
# CRITICAL: Configure Docker to point to Minikube
eval $(minikube docker-env)

# Verify it's pointing to Minikube
docker ps  # Should show Minikube containers if already running

# Rebuild images inside Minikube
bash scripts/deploy-minikube.sh

# Verify images exist in Minikube
docker images | grep techbank

# If still not found, redeploy
kubectl rollout restart deployment/account-service -n techbank
kubectl get pods -n techbank -w
```

**Important:** The `eval $(minikube docker-env)` command is session-specific. If you:
- Open a new terminal → must run it again
- Close the terminal → loses the context
- Shut down Minikube → Docker env is invalid

---

### 8. "failed to solve with frontend dockerfile.v0"

**Error:**
```
error: failure calling webhook to validate crd creation: 
Post "https://webhook.invalid:443/...": dial tcp: lookup webhook.invalid on...: no such host
```

**Solution - Docker environment not configured:**
```bash
# Make sure Docker env is set to Minikube
eval $(minikube docker-env)

# Then rebuild
bash scripts/deploy-minikube.sh

# If still failing, try manual rebuild
cd services/account-service
docker build -t account-service:1.0.0 .
cd ../../  # Back to root
```

---

## Kubernetes Deployment Issues

### 9. CrashLoopBackOff: Pods crashing repeatedly

**Error:**
```bash
$ kubectl get pods -n techbank
NAME                                    READY   STATUS             RESTARTS   AGE
api-gateway-6c9f3d2e1a-xxxxx           0/1     CrashLoopBackOff   5          3m
```

**Solution - Check logs first:**
```bash
# See what's wrong
kubectl logs -n techbank api-gateway-6c9f3d2e1a-xxxxx

# Or use label selector
kubectl logs -n techbank -l app=api-gateway --tail=50

# Get events for more context
kubectl describe pod <pod-name> -n techbank

# Common issues and fixes:

# 1. Database connection failed
#    → Check if postgres pod is running: kubectl get pods -n techbank | grep postgres
#    → Check db credentials in ConfigMaps: kubectl get configmap -n techbank

# 2. Port already in use
#    → Check container: kubectl logs <pod> -n techbank | grep "Address already in use"
#    → Solution: Change port in k8s/configmaps/

# 3. Spring Boot startup failed
#    → Logs will show the error
#    → Common: Missing environment variables
#    → Solution: Check k8s/configmaps/* for all required configs
```

---

### 10. Pods Running but Unresponsive: Liveness/Readiness probe failing

**Error:**
```bash
$ kubectl describe pod <pod-name> -n techbank
...
Ready               False
Container ready    False
...
Warning  Unhealthy   12s (x5 over 52s)  kubelet   
Readiness probe failed: HTTP probe failed with statuscode: 503
```

**Solution:**
```bash
# Wait for application to fully start (Spring Boot can take 30+ seconds)
kubectl get pods -n techbank -w  # Watch until READY=1/1

# Check logs for startup errors
kubectl logs -n techbank -l app=api-gateway -f --all-containers=true

# If timeout, increase probe delay
kubectl patch deployment api-gateway -n techbank -p \
  '{"spec":{"template":{"spec":{"containers":[{"name":"api-gateway","readinessProbe":{"initialDelaySeconds":60}}]}}}}'

# Verify again
kubectl get pods -n techbank
```

---

### 11. Services Not Discoverable: "Cannot resolve hostname"

**Error:**
```
Connection refused: account-service:8001
or
java.net.UnknownHostException: account-service
```

**Solution:**
```bash
# Check if services are created
kubectl get svc -n techbank

# Should show:
# account-service       ClusterIP   10.x.x.x  <none>     8001/TCP
# transaction-service   ClusterIP   10.x.x.x  <none>     8002/TCP
# api-gateway          NodePort    10.x.x.x  <none>    8000:3xxxx/TCP

# If services missing, apply manifests
kubectl apply -f k8s/services/ -n techbank

# Verify DNS resolution from inside a pod
kubectl exec -it <pod-name> -n techbank -- nslookup account-service

# If DNS not working, check if CoreDNS pod is running
kubectl get pods -n kube-system | grep coredns
```

---

## Network/Access Issues

### 12. Cannot Access API Gateway: Port Forward Not Working

**Error:**
```
$ curl http://localhost:8000/
curl: (7) Failed to connect to localhost port 8000: Connection refused
```

**Solution:**
```bash
# Check if port-forward is running
ps aux | grep "port-forward"

# If not, start it
kubectl port-forward -n techbank svc/api-gateway 8000:8000

# If port 8000 already in use, use different port
kubectl port-forward -n techbank svc/api-gateway 9000:8000
curl http://localhost:9000/

# Kill conflicting process
sudo lsof -i :8000
sudo kill -9 <PID>

# Then try again
kubectl port-forward -n techbank svc/api-gateway 8000:8000
```

---

### 13. Timeout Accessing Services Through Ingress

**Error:**
```
$ curl http://techbank.local/api/v1/accounts/create?...
curl: (7) Failed to connect to techbank.local port 80
```

**Note:** Our setup uses port-forward, not Ingress

**Solution - Use port-forward instead:**
```bash
# Standard approach (recommended)
kubectl port-forward -n techbank svc/api-gateway 8000:8000

# Then use
curl http://localhost:8000/api/v1/accounts/create?...

# NOT localhost without port-forward
```

---

## Database Issues

### 14. PostgreSQL Pod Won't Start

**Error:**
```bash
$ kubectl logs -n techbank postgres-0
...
chmod: Permission denied
or
cannot open data directory "/var/lib/postgresql/data": Permission denied
```

**Solution:**
```bash
# Delete StatefulSet and try again
kubectl delete statefulset postgres -n techbank
kubectl delete pvc postgres-data -n techbank

# Reapply database manifests
kubectl apply -f k8s/database/ -n techbank

# Wait for postgres to start
kubectl get pods -n techbank -w | grep postgres

# Verify
kubectl logs -n techbank postgres-0
```

---

### 15. Database Connection Timeouts

**Error:**
```
org.postgresql.util.PSQLException: Connection to postgres-service:5432 refused
```

**Solution:**
```bash
# Check if postgres pod is running
kubectl get pods -n techbank | grep postgres

# Check postgres logs
kubectl logs -n techbank postgres-0

# If connection string wrong, check ConfigMaps
kubectl get configmap -n techbank
kubectl describe configmap account-service-config -n techbank

# Verify network connectivity from app pod
kubectl exec -it <app-pod> -n techbank -- \
  sh -c "nc -zv postgres-service 5432"

# If fails, DNS issue
kubectl exec -it <app-pod> -n techbank -- nslookup postgres-service
```

---

## Debugging Techniques

### General Debugging Workflow

```bash
# 1. Check pod status
kubectl get pods -n techbank

# 2. Get detailed pod info
kubectl describe pod <pod-name> -n techbank

# 3. Check logs
kubectl logs -n techbank <pod-name>
kubectl logs -n techbank -l app=<service-name> --tail=100

# 4. Check events
kubectl get events -n techbank --sort-by='.lastTimestamp'

# 5. Exec into pod
kubectl exec -it <pod-name> -n techbank -- bash

# 6. Test connectivity from pod
kubectl exec -it <pod-name> -n techbank -- curl http://localhost:8001

# 7. Check resource limits
kubectl top pods -n techbank
kubectl describe nodes
```

---

### Useful Commands for Troubleshooting

```bash
# Watch logs in real-time
kubectl logs -f -n techbank <pod-name>

# Follow logs for all pods of a service
kubectl logs -f -n techbank -l app=api-gateway --all-containers

# Get shell in pod
kubectl exec -it <pod-name> -n techbank -- /bin/bash

# Copy file from pod
kubectl cp techbank/<pod-name>:/path/to/file ./local-file

# Check Minikube resources
minikube ssh  # SSH into Minikube VM
docker ps     # List running containers inside Minikube
exit          # Exit Minikube SSH

# Check Minikube logs
minikube logs

# Dashboard
minikube dashboard  # Opens Kubernetes dashboard in browser
```

---

## Common Issues Quick Reference

| Symptom | Cause | Fix |
|---------|-------|-----|
| Pods stuck in Pending | Insufficient resources | `minikube stop`, increase memory/CPU, `minikube start` |
| ImagePullBackOff | Images not in Minikube | `eval $(minikube docker-env)` then rebuild |
| CrashLoopBackOff | App crash | `kubectl logs <pod> -n techbank` to see error |
| Cannot access services | Port-forward not running | `kubectl port-forward -n techbank svc/api-gateway 8000:8000` |
| Database connection fails | Postgres not running | `kubectl get pods -n techbank \| grep postgres` |
| Connection refused | Wrong port or service down | Check `kubectl get svc -n techbank` |
| DNS resolution fails | CoreDNS issue | `kubectl get pods -n kube-system \| grep coredns` |
| Out of disk space | Minikube storage full | `minikube ssh` then `df -h` to check, delete old images |

---

## Still Stuck?

### Step-by-step recovery:

```bash
# 1. Get full system status
kubectl get all -n techbank
kubectl describe all -n techbank

# 2. Check events for errors
kubectl get events -n techbank --sort-by='.lastTimestamp' | tail -20

# 3. Full pod diagnostics
kubectl get pods -n techbank -o wide
kubectl logs -n techbank --all-containers --prefix <pod-name>

# 4. Full cleanup and restart
bash scripts/teardown-minikube.sh
minikube delete
minikube start --cpus=4 --memory=8192

# 5. Full redeploy
bash scripts/build.sh
bash scripts/deploy-minikube.sh

# 6. Verify
kubectl get pods -n techbank -w
```

---

### Get Help

1. **Check logs first** - 90% of issues are revealed in logs
2. **Describe pods** - Detailed information about pod state
3. **Check events** - Timeline of what happened
4. **Verify prerequisites** - Ensure Java, Maven, Docker, Minikube all installed
5. **Network connectivity** - Test from inside pods using `kubectl exec`
6. **Resource availability** - Check `kubectl top nodes` and `minikube status`

---

**Last Updated:** November 29, 2024
**Version:** 1.0.0
