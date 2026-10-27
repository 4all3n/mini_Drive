# 🐳 Mini Cloud Drive — Docker Deployment Guide

This guide details how to deploy the entire Mini Cloud Drive server stack (MariaDB Database, Python FastAPI AI Microservice, and Spring Boot REST API) using Docker and Docker Compose.

---

## 🏛️ Architecture Overview

The multi-container Docker architecture coordinates three core services on a dedicated bridge network (`minidrive-network`):

```
                       ┌────────────────────────────────────────┐
                       │          JavaFX Client (Host)          │
                       │    (Runs natively on desktop/laptop)   │
                       └───────────────────┬────────────────────┘
                                           │ HTTP (:8080)
                                           ▼
┌────────────────────────────────────────────────────────────────────────────────────────┐
│ Docker Network (minidrive-network)                                                     │
│                                                                                        │
│  ┌───────────────────────┐   Internal HTTP (:8000)   ┌───────────────────────────────┐ │
│  │   Spring Boot Backend │ ────────────────────────> │      AI Microservice (FastAPI)│ │
│  │     (Port :8080)      │ <──────────────────────── │       (Linear SVM & NB Models)│ │
│  └───────────┬───────────┘    Webhook Notifications  └───────────────────────────────┘ │
│              │                                                                         │
│              │ JDBC Connection (:3306)                                                 │
│              ▼                                                                         │
│  ┌───────────────────────┐                                                             │
│  │   MariaDB Database    │                                                             │
│  │     (Port :3306)      │                                                             │
│  └───────────────────────┘                                                             │
└────────────────────────────────────────────────────────────────────────────────────────┘
```

| Service | Technology | Internal Port | Host Port | Description |
| :--- | :--- | :--- | :--- | :--- |
| **`database`** | MariaDB 10.11 | `3306` | `3306` | Relational storage for users, folder hierarchy, and file metadata. |
| **`ai-service`** | Python 3.11 / FastAPI | `8000` | `8000` | ML file classification microservice with pre-trained Linear SVM and Naive Bayes models. |
| **`backend`** | Eclipse Temurin 21 / Spring Boot | `8080` | `8080` | Core REST API, JWT auth, Drive operations, and physical file storage. |

---

## 🚀 Quick Start (1-Command Deployment)

### 1. Prerequisites
- [Docker Engine](https://docs.docker.com/engine/install/) (v20.10+)
- [Docker Compose](https://docs.docker.com/compose/install/) (v2.0+)

### 2. Launch the Server Stack
Run the automated deployment script from the project root:

```bash
./docker-start.sh
```

*Or manually via Docker Compose:*
```bash
docker compose up --build -d
```

### 3. Verify Health & Services
Once all healthchecks pass, the following endpoints are available:

- **AI Microservice Swagger Docs**: [http://localhost:8000/docs](http://localhost:8000/docs)
- **AI Health Status**: [http://localhost:8000/health](http://localhost:8000/health)
- **Backend AI Status Endpoint**: [http://localhost:8080/api/ai/status](http://localhost:8080/api/ai/status)
- **MariaDB Database**: `localhost:3306` (User: `app_user`, DB: `cloud_db`)

---

## 🖥️ Launching the Desktop Client

The JavaFX desktop client connects to the Dockerized backend at `http://localhost:8080`:

```bash
cd Cloud_Frontend_Workspace/frontend
./mvnw javafx:run
```

> **Connecting from another machine on the LAN:**  
> Pass the server's IP address:  
> `./mvnw javafx:run -Dminicloud.server.host=192.168.1.100`

---

## ⚙️ Configuration (`.env`)

You can customize credentials, ports, and JWT settings by creating/editing `.env` (defaults are pre-configured in `.env.example`):

```env
# Database Configuration
DB_ROOT_PASSWORD=rootpassword
DB_NAME=cloud_db
DB_USER=app_user
DB_PASSWORD=7317355
DB_PORT=3306

# AI Microservice
AI_SERVICE_PORT=8000

# Spring Boot REST API
BACKEND_PORT=8080
JWT_SECRET=MiniCloudDriveSecretKey2026ForHMACSHA256SigningAlgorithm
JWT_EXPIRATION=86400000
```

---

## 📜 Logs & Management

### View Live Logs
```bash
# All services
docker compose logs -f

# Specific service
docker compose logs -f backend
docker compose logs -f ai-service
docker compose logs -f database
```

### Stop Services
```bash
# Normal stop (preserves database and uploaded files in volumes)
./docker-stop.sh

# Or:
docker compose down
```

### Complete Reset (Wipe Database & Files)
To start with a completely fresh, empty installation:
```bash
./docker-stop.sh --volumes

# Or:
docker compose down -v
```

---

## 🛠️ Docker Optimization Highlights

1. **Multi-Stage Spring Boot Build**:
   - Compiles inside `eclipse-temurin:21-jdk-alpine`.
   - Packages into a minimal `eclipse-temurin:21-jre-alpine` runtime image, reducing image footprint by over 60%.
2. **Context Optimization (`.dockerignore`)**:
   - Excludes local `.venv` (saving ~500 MB upload), `target/`, and `cloud_storage/` during builds.
3. **Automated Healthchecks & Dependency Ordering**:
   - `backend` waits for both `database` and `ai-service` to be completely healthy before starting up.
4. **Self-Healing AI Notification**:
   - The AI service retries webhook notifications to `http://backend:8080/api/ai/online-notify` on startup, automatically triggering instant recategorization of any files uploaded while offline.
