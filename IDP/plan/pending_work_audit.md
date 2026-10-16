# Comprehensive Audit: Pending Work & Milestone Roadmap
**Project:** Intelligent Cloud Drive (AI-Powered File Management System with Secure Distributed Architecture)  
**Author:** Harsh Pratap Singh (25MCA1027)  
**Course:** PAMCA698 — MCA Dissertation / Project Work  
**Current Milestone:** Post-Review 3 → Draft Review (Mid-October) → Final Viva (Late October)  
**Current Implementation Status:** ~65% Complete | ~35% Remaining  

---

## 1. Executive Summary & Progress Overview

Over Reviews 1, 2, and 3, the foundational architecture of the Intelligent Cloud Drive has been successfully established:
- **Core Security:** BCrypt password hashing and stateless HMAC-SHA256 JWT authentication are operational in Spring Boot.
- **AI Microservice:** A machine learning pipeline comparing Multinomial Naive Bayes vs. Linear SVM on 570 balanced samples using Character N-Grams and TF-IDF was trained, validated (5-Fold CV), and deployed via FastAPI on port 8000.
- **Basic Desktop Client:** A functional JavaFX client handles login, registration, file listing, uploading, downloading, and deletion.
- **Cross-Platform Guides:** Documentation and launch scripts for CachyOS/Linux and Windows are in place.

However, to transition from a **Review 3 prototype (60–65%)** to a **production-grade academic dissertation deliverable (100%)** suitable for final grading and viva defense, several critical features, security fixes, DevOps pipelines, and academic documents remain pending.

---

## 2. Status Scorecard by Subsystem

| Subsystem / Layer | Current Status | Completion % | Critical Gaps |
| :--- | :---: | :---: | :--- |
| **JavaFX Desktop Client** | Working Prototype | **60%** | No drag-and-drop, no category filter/search, no storage pie chart, delimited text parsing. |
| **Spring Boot REST Backend** | Functional API | **70%** | Hard drive filename collisions (UUID missing on disk), delimited string response, no storage stats API, zero unit tests. |
| **FastAPI AI Microservice** | Operational Engine | **85%** | Lacks user feedback/active learning endpoint for misclassifications; confidence scores not forwarded to UI. |
| **DevOps & Containerization** | Manual Shell Scripts | **25%** | No `Dockerfile` for backend or AI; no `docker-compose.yml` (despite being on the architecture diagram). |
| **Academic Documentation** | Review PPTs & Setup Guides | **40%** | **60–70 page Final Dissertation Report** not yet written; Guide approval letter and final viva deck needed. |

---

## 3. Detailed Breakdown of All Pending Work

### A. Frontend Modernization (JavaFX Desktop Client)
*Files involved: `DashboardScreen.java`, `SceneManager.java`, `FileNetworkService.java`*

1. **Category Filtering & Search Bar (High Priority)**
   - *Current State:* Table shows all files in a single flat list without search or filters.
   - *Work Needed:* 
     - Add a real-time `TextField` search bar that filters files as the user types.
     - Add a `ComboBox` or segmented button bar for Category Filter (`All`, `Documents`, `Code`, `Media`, `Archives`, `Executables`, `System`).
     - Back the `TableView` with a JavaFX `FilteredList` for instantaneous UI updates without re-fetching from the network.

2. **Storage Analytics Visualization (High Priority)**
   - *Current State:* User has no visibility into their storage usage or category distribution.
   - *Work Needed:* 
     - Add a collapsible side pane or header card featuring a JavaFX `PieChart` showing storage breakdown by category (e.g., Documents: 45MB, Media: 120MB).
     - Display a total quota usage bar (e.g., "165 MB of 500 MB used").

3. **Drag-and-Drop File Ingestion (Medium Priority)**
   - *Current State:* Upload requires clicking `+ Upload File` and opening the native OS `FileChooser`.
   - *Work Needed:*
     - Attach `setOnDragOver` and `setOnDragDropped` event handlers to the `TableView` and dashboard pane.
     - Visual feedback (drop target highlight) when dragging files from the host file manager directly onto the app.

4. **Category & File Type Visual Indicators (Medium Priority)**
   - *Current State:* Plain text strings for file name, type, and category.
   - *Work Needed:*
     - Add visual icon badges / color-coded tags for each category (e.g., Blue for Code, Emerald for Documents, Purple for Media, Amber for Archives, Red for Executables).

5. **File Preview Capabilities (Low / Polish Priority)**
   - *Current State:* User must download a file to local disk before seeing its content.
   - *Work Needed:*
     - Implement a quick-preview dialog for text/code files and common images (`.png`, `.jpg`) using an overlay modal.

6. **Migration from Delimited Strings to JSON DTOs (High Priority)**
   - *Current State:* `FileNetworkService.java` parses `parts[0]`, `parts[1]` using `;;;` and `|` splits.
   - *Work Needed:*
     - Adopt Jackson or Java 11 JSON parser to deserialize `List<FileRecord>` from structured JSON returned by Spring Boot.

---

### B. Backend API Hardening & Standards (Spring Boot)
*Files involved: `FileController.java`, `AiCategorizationService.java`, `application.properties`, `pom.xml`*

1. **UUID Physical File Collision Prevention (CRITICAL BUG FIX)**
   - *Current State:* In `FileController.java` line 59:
     ```java
     Path targetPath = Paths.get(UPLOAD_DIR).resolve(cleanFileName).normalize().toAbsolutePath();
     ```
     If User A and User B both upload `resume.pdf`, User B overwrites User A's physical file on disk!
   - *Work Needed:*
     - Generate a UUID for physical disk storage: `String storedFileName = UUID.randomUUID() + "_" + cleanFileName;`
     - Save `storedFileName` to the file system, but keep `originalFileName` in the database to display cleanly to the user.

2. **Secure User Context Extraction (Security Fix)**
   - *Current State:* `uploadFile()` accepts `@RequestParam("username") String username`. A rogue user with a valid JWT could pass `username=admin` and upload files attributed to another user.
   - *Work Needed:*
     - Extract the owner strictly from `SecurityContextHolder.getContext().getAuthentication().getName()`. Remove `username` request parameter.

3. **RESTful JSON DTO Migration (Architectural Standard)**
   - *Current State:* `GET /api/files/list` builds a manual string with pipe (`|`) and semicolon delimiters (`;;;`).
   - *Work Needed:*
     - Create `FileMetadataDTO.java` containing `id`, `fileName`, `fileType`, `fileSize`, `category`, `uploadDate`.
     - Return `ResponseEntity<List<FileMetadataDTO>>` with standard JSON serialization.

4. **Storage Statistics API Endpoint (High Priority)**
   - *Current State:* No endpoint exists to calculate a user's total usage or category breakdown.
   - *Work Needed:*
     - Implement `GET /api/files/stats`: Returns JSON containing `{ "totalBytes": 12450000, "byCategory": { "Documents": 4500000, "Code": 7950000 } }`.

5. **Dynamic AI Service Configuration (Config Fix)**
   - *Current State:* `AiCategorizationService.java` has hardcoded `http://127.0.0.1:8000/predict`.
   - *Work Needed:*
     - Move AI service URL to `application.properties`: `ai.service.url=http://127.0.0.1:8000/predict` (inject via `@Value("${ai.service.url}")`). Allows containerized deployment where the AI service host is `ai-service` instead of `localhost`.

6. **Automated Unit & Integration Test Suite (High Academic Priority)**
   - *Current State:* `BackendApplicationTests.java` has an empty `contextLoads()` test.
   - *Work Needed:*
     - Unit tests for `JwtTokenProvider` (token generation, expiration, signature tampering).
     - Unit tests for Path Traversal blocking (verifying `../../` inputs return 400/403).
     - MockMvc tests for `AuthController` (login failure on bad password, success on valid credentials).
     - MockMvc tests for IDOR validation in `FileController`.

---

### C. AI Microservice Enhancements (FastAPI & ML)
*Files involved: `model/app.py`, `model/train_and_evaluate.py`*

1. **Active Learning / Feedback Loop Endpoint (High Value for Final Viva)**
   - *Current State:* Model is static. If it miscategorizes a file, there is no mechanism to record user correction.
   - *Work Needed:*
     - Add `POST /feedback`: Accepts `{ "filename": "script.sh", "predicted": "Documents", "corrected": "Code" }`.
     - Append corrections to a `corrections.jsonl` audit file for periodic retraining.
     - Panel members frequently ask: *"What happens if the model predicts wrong?"* Having this endpoint and UI button provides an unbeatable answer.

2. **Confidence Score Forwarding to Backend**
   - *Current State:* FastAPI calculates probability confidence (`confidence: 0.98`), but Spring Boot only extracts the category string.
   - *Work Needed:*
     - Store `confidence` in `FileMetadata` database table and display low-confidence tags (e.g. `< 70%`) in the UI.

---

### D. DevOps, Deployment & Containerization
*Files to create: `Dockerfile` (Backend), `Dockerfile` (AI), `docker-compose.yml`*

1. **Docker Compose Multi-Container Orchestration (High Priority)**
   - *Current State:* The architecture diagram in Review 3 shows Docker Compose, but the project currently relies on host processes and shell scripts (`run.fish`, `run.bat`).
   - *Work Needed:*
     - `Cloud_Backend_Workspace/backend/Dockerfile`: Multi-stage build with Maven + Eclipse Temurin 17 JRE.
     - `model/Dockerfile`: Python 3.10-slim + scikit-learn + FastAPI + Uvicorn.
     - `docker-compose.yml` at project root:
       - Service 1: `mariadb` (with persistent volume and environment credentials).
       - Service 2: `ai-service` (port 8000).
       - Service 3: `backend` (port 8080, depends on `mariadb` and `ai-service`, mounts `cloud_storage` volume).
     - Allows running the entire backend infrastructure on any machine with `docker compose up -d`.

---

### E. Academic Dissertation & Final Viva Deliverables (Highest Weight)
*Deliverables required by university guidelines:*

1. **60–70 Page Final Dissertation Report (Draft Review Requirement)**
   - *Target Similarity / Plagiarism:* Under 15%.
   - *Structure Required:*
     - **Front Matter:** Title page, Bonafide Certificate, Student Declaration, Acknowledgements, Abstract, Table of Contents, List of Tables, List of Figures.
     - **Chapter 1 — Introduction:** Cloud Storage Evolution, Automated File Organization Problem, Motivation, Research Objectives, Scope and Deliverables.
     - **Chapter 2 — Literature Review:** Exhaustive analysis of 10–12 papers (Joachims, Kowsari, Li et al., Ghemawat/GFS, Prasad & Sharma, Biryukov), comparative gap matrix.
     - **Chapter 3 — System Requirements & Design:** Functional and Non-Functional Requirements, Architectural Design (4-tier distributed), UML Class Diagrams, Sequence Diagrams (JWT Auth, File Ingestion), ER Diagrams.
     - **Chapter 4 — Machine Learning Methodology:** Ultra-short text challenge, Tokenization limitations, Character N-Gram theory (n=2..5), TF-IDF mathematical formulation, Mathematical foundation of Multinomial Naive Bayes vs. Linear Support Vector Machines.
     - **Chapter 5 — Implementation Details:** Spring Boot REST & Security configuration, Stateless JWT Filter implementation, BCrypt password hashing, JavaFX Native Desktop Client architecture, FastAPI Microservice setup.
     - **Chapter 6 — Experimental Results & Empirical Discussion:** Dataset synthesis and distribution, Comparative Evaluation (Accuracy, Precision, Recall, F1), 5-Fold Cross-Validation variance and stability analysis, Confusion matrix breakdown, Discussion of per-class performance.
     - **Chapter 7 — Security & Threat Analysis:** IDOR vulnerability mitigation, Path traversal defense mechanisms, Filename collision prevention, Token replay defense.
     - **Chapter 8 — Conclusion & Future Enhancements:** Research summary, Limitations, Future scope (S3 integration, chunked uploads, active learning).
     - **References & Appendices:** APA references, sample code snippets, API specification.

2. **Guide Approval Documentation**
   - Official progress update email and sign-off confirmation from Project Guide (Dr. Saranya G).

3. **Final Viva Presentation Deck (Review 4 / Final Defense)**
   - 25-slide final presentation deck reflecting the 100% completed system, live demo recordings, complete benchmarks, and project conclusion.

---

## 4. Prioritized Execution Roadmap (To 100% Completion)

```
┌─────────────────────────────────────────────────────────────────────────────┐
│ PHASE 1: Critical Code Fixes & REST Standardization (Days 1–2)              │
│ • Fix UUID collision bug in FileController.java                             │
│ • Secure upload username parameter with SecurityContext                     │
│ • Migrate /api/files/list to typed JSON DTOs                                │
│ • Add GET /api/files/stats endpoint                                         │
└──────────────────────────────────────┬──────────────────────────────────────┘
                                       ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│ PHASE 2: Desktop Client Polish & Visualizations (Days 3–4)                  │
│ • Add Category Filter & Search Bar in DashboardScreen                       │
│ • Add Storage Analytics PieChart                                            │
│ • Add Drag-and-Drop file upload handler                                     │
│ • Update FileNetworkService to parse JSON DTOs                              │
└──────────────────────────────────────┬──────────────────────────────────────┘
                                       ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│ PHASE 3: Containerization & Test Suite (Days 5–6)                           │
│ • Create Dockerfile for Backend & FastAPI AI Service                        │
│ • Create root docker-compose.yml                                            │
│ • Write JUnit unit tests for JWT, Path Traversal, and BCrypt                │
│ • Add POST /feedback endpoint to AI service                                 │
└──────────────────────────────────────┬──────────────────────────────────────┘
                                       ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│ PHASE 4: Dissertation Writing & Final Viva Prep (Days 7–12)                 │
│ • Author Chapters 1 through 8 of the 60–70 page Dissertation                │
│ • Format diagrams, charts, and tables into IEEE/VIT standard template       │
│ • Run plagiarism check (verify < 15%)                                       │
│ • Prepare Final Viva presentation and live demonstration script             │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## 5. Effort & Priority Matrix

| Item # | Task Description | Component | Priority | Est. Time |
|:---:|:---|:---:|:---:|:---:|
| **1** | Fix physical disk UUID filename collision | Backend | **Critical** | 1 hour |
| **2** | Secure upload username extraction from JWT | Backend | **High** | 1 hour |
| **3** | Migrate file listing to JSON DTOs | Backend & Client | **High** | 3 hours |
| **4** | Add Category Filter & instant Search Bar | Client | **High** | 3 hours |
| **5** | Add Storage Analytics PieChart | Client & Backend | **High** | 4 hours |
| **6** | Implement Drag-and-Drop file upload | Client | **Medium** | 2 hours |
| **7** | Implement `docker-compose.yml` & Dockerfiles | DevOps | **High** | 4 hours |
| **8** | Implement Active Learning `/feedback` endpoint | AI & Backend | **Medium** | 2 hours |
| **9** | Write automated unit and integration tests | Backend | **Medium** | 4 hours |
| **10** | Add Category visual icon badges & style polish | Client | **Medium** | 2 hours |
| **11** | Draft Chapters 1–4 of Dissertation (Intro, Lit Review, Design, ML) | Documentation | **Critical** | 3 days |
| **12** | Draft Chapters 5–8 of Dissertation (Impl, Results, Security, Conclusion)| Documentation | **Critical** | 3 days |
| **13** | Dissertation formatting, plagiarism check & proofreading | Documentation | **High** | 1 day |
| **14** | Final Viva presentation deck & live demo rehearsal | Viva Prep | **High** | 1 day |

---

*This document serves as the master tracking checklist for completing all remaining requirements before the final dissertation submission and viva voce.*
