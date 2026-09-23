# Autonomous Internal Developer Platform (IDP)

## 1. Project Overview & Assessment

### 1.1 Abstract
Modern cloud-native architectures rely heavily on containerization and complex deployment pipelines, which frequently introduce significant operational overhead for software engineers. This project proposes a "Zero-Ops" Internal Developer Platform (IDP) designed to completely abstract away infrastructure management, enabling developers to deploy applications autonomously directly from raw source code.

The core architecture replaces traditional manual DevOps configurations with an Agentic AI workflow. By integrating a local, quantized Large Language Model (Llama 3.2 via Ollama), the platform programmatically analyzes repository codebases to deduce framework dependencies and autonomously generates optimal, secure Dockerfile configurations without exposing proprietary code to external cloud APIs.

The infrastructure orchestration layer is driven by a high-throughput backend (Node.js) that bypasses standard CLI utilities to interface directly with the host’s Docker Daemon via the Unix socket (`/var/run/docker.sock`). This direct integration enables the programmatic execution of image compilation, dynamic container provisioning, and automated reverse-proxy port routing within an isolated environment.

To ensure strict operational transparency, the system establishes a bidirectional WebSocket connection to stream real-time compilation logs and container telemetry directly to a centralized React dashboard. 

### 1.2 Difficulty Level Assessment (For a Beginner)
**Difficulty: HIGH**
As a beginner (3rd-semester MCA student), this project is quite ambitious but extremely rewarding. Here is why:
- **Learning Curve:** You will need to learn Agentic AI prompting, Docker APIs (not just basic Docker commands, but daemon interfacing programmatically), WebSockets, and Full-Stack development all at once.
- **System Level Operations:** Interacting with `/var/run/docker.sock` requires an understanding of OS-level permissions, streams, and networking.
- **Guideline Compliance:** Your college guidelines explicitly state: *"No website designing or web application projects or mobile application development projects are entertained."* 
  - **CRITICAL ADVICE:** You must strongly frame this as a **Platform Engineering & Agentic AI** project. The React dashboard is just a "Telemetry UI" to monitor the system. Do not pitch this as a "Web App". Focus heavily on the AI generation and Docker daemon orchestration in your presentations to satisfy your panel.

---

## 2. Technical Specifications

### 2.1 Tech Stack
- **AI / LLM Engine:** Ollama running Llama 3.2 (Local & Quantized for Data Privacy).
- **Backend / Orchestrator:** Node.js with Express.js. *(Note: Node.js is highly recommended here because you can use the `dockerode` library, which makes interacting with the Docker Unix socket much easier for beginners).*
- **Frontend / Telemetry UI:** React.js.
- **Communication Protocol:** WebSockets (Socket.io) for real-time log streaming.
- **Infrastructure:** Docker Engine, Linux Host.

### 2.2 Database Used
Since the platform is designed to ingest repositories and spin up containers dynamically, a massive relational database isn't strictly necessary, but you need state persistence:
- **Primary Database:** SQLite or PostgreSQL. Used to store repository metadata, container status history (running, stopped, failed), and deployment logs.
- **Vector Database (Optional/Advanced):** ChromaDB or FAISS. If you want the AI to understand complex, multi-file repository structures better, you can implement Retrieval-Augmented Generation (RAG) using a local vector store.

---

## 3. Project Modules

1. **Repository Ingestion Module:** Scans and parses the raw source code directory, identifying package managers (e.g., `package.json`, `requirements.txt`) and project structures.
2. **Agentic AI Blueprint Module:** Passes repository metadata to the local LLM (Ollama) to autonomously generate a secure, optimized `Dockerfile` and network configurations.
3. **Infrastructure Orchestration Module:** The Node.js backend connects to `/var/run/docker.sock` to build the Docker image and deploy the container programmatically without human intervention.
4. **Networking & Proxy Module:** Automatically assigns random available ports on the host and sets up reverse proxy routing to the newly spun-up container.
5. **Telemetry & Dashboard Module:** Streams build logs and container health stats (CPU/Memory usage) to the React dashboard via WebSockets.

---

## 4. Proposed Timeline (Aligned with College Guidelines)

| Phase | Milestone | Expected Dates | Deliverables |
|-------|-----------|----------------|--------------|
| **Phase 1** | Domain Selection & Abstract | **9 - 11 July 2026** *(Review 1)* | Finalized Title, Problem Statement, Abstract. Guide Approval screenshot. |
| **Phase 2** | Research & Architecture | **12 August 2026** *(Review 2)* | Literature Review (10 papers), Architecture Diagram, Modules identified, Tech Stack finalized. |
| **Phase 3** | Core Implementation (AI & Docker) | **30 Sept 2026** *(Review 3)* | 60% Implementation complete: LLM successfully generating Dockerfiles, Node.js building containers. Initial Results. |
| **Phase 4** | Dashboard & Draft Report | **12 - 16 Oct 2026** *(Draft Review)* | Telemetry Dashboard complete. 60-70 page Draft Report submitted with <15% Plagiarism/AI content. |
| **Phase 5** | Final Polish & Verification | **28 Oct 2026** *(Final Review)* | 100% Implementation, UI Polish, Final Results, Demo ready, Research paper proof (if any). |
