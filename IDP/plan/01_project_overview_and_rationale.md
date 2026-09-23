# 01. Project Overview & Rationale

## 1. Context and Problem Statement

### 1.1 Academic and Practical Context
The project **`mini_Drive`** originated as an academic prototype for a simplified cloud storage system. While the baseline demonstrated basic multi-tier communication (a JavaFX desktop frontend, a Spring Boot backend, a local MySQL database, and a basic FastAPI service), an in-depth security and architectural evaluation reveals significant gaps preventing it from functioning as a secure, production-grade, or academically rigorous distributed system.

Furthermore, university guidelines for the 3rd-semester project (IDP) mandate that:
> *"No website designing or web application projects or mobile application development projects are entertained."*

The project must demonstrate **system-level programming, distributed systems architecture, security engineering, and applied machine learning**.

### 1.2 The Core Problems in Personal Cloud Storage

1. **Manual File Organization ("Zero Intelligence"):**
   Modern personal storage solutions (Google Drive, Dropbox, OneDrive) still require users to manually create nested directory trees, rename files, and drag-and-drop items into folders. Users accumulate hundreds of unorganized downloads and documents.
2. **Data Privacy Leakage in Cloud AI:**
   Commercial cloud storage platforms offering AI features (e.g., Google Workspace smart organization, Microsoft Copilot) require transmitting file metadata and contents to external third-party servers. For organizations, researchers, and individuals handling proprietary, medical, legal, or confidential data, offloading data to third-party APIs violates compliance and privacy regulations.
3. **Severe Vulnerabilities in Academic & DIY Prototypes:**
   Analysis of the existing `mini_Drive` prototype reveals standard pitfalls common in student implementations:
   - **Plain-Text Credentials:** Passwords stored and evaluated in cleartext within `cloud_db.users`.
   - **Absence of Token / Session Security:** Endpoints lack authentication filters; any HTTP client can trigger operations if the endpoint is known.
   - **Path Traversal Vulnerability:** Filenames supplied in multipart uploads are directly concatenated into the filesystem path (`UPLOAD_DIR + file.getOriginalFilename()`), allowing directory breakout (`../../`).
   - **Insecure Direct Object References (IDOR):** File download and delete endpoints take database IDs (`/api/files/download/{id}`, `/api/files/delete/{id}`) without validating that the authenticated user owns the referenced file.
   - **Fragile Serialization Protocol:** File listing relies on a custom pipe-delimited string (`id|name|type|size|category;;;`) split manually with string indexing, instead of standard JSON.

---

## 2. What We Are Going to Do

In a dedicated git branch (`feature/intelligent-cloud-drive`), we will systematically engineer **"Intelligent Cloud Drive: AI-Powered File Management System with Secure Distributed Architecture"**.

### Key Deliverables:
1. **Machine Learning Pipeline & Comparative Study:**
   - Implement an automated classification pipeline using **TF-IDF with character-level n-grams ($n \in [2, 5]$)**.
   - Train and conduct an empirical comparative benchmark of **Multinomial Naive Bayes** versus **Linear Support Vector Machine (Linear SVM)** on a curated dataset of 500+ filenames across 6 distinct categories:
     1. `Documents` (`.pdf`, `.docx`, `.txt`, `.xlsx`, `.pptx`)
     2. `Images` (`.jpg`, `.png`, `.gif`, `.svg`, `.webp`)
     3. `Videos` (`.mp4`, `.mkv`, `.avi`, `.mov`)
     4. `Audio` (`.mp3`, `.wav`, `.flac`, `.aac`)
     5. `Code` (`.java`, `.py`, `.js`, `.html`, `.cpp`, `.css`)
     6. `Archives` (`.zip`, `.tar.gz`, `.7z`, `.rar`, `.jar`)
   - Compute and compare performance metrics: **Accuracy, Precision, Recall, and F1-Score**, alongside a confusion matrix.
2. **Production-Grade Security Implementation:**
   - **BCrypt Password Hashing:** Salted, slow-by-design hashing with adaptive cost factor (10+).
   - **Stateless JWT Authentication:** HMAC-SHA256 signed bearer tokens passed in the `Authorization` header, validated by a Spring Security filter chain.
   - **Path Traversal Elimination:** Rigorous path normalization (`Path.normalize()`) and filename sanitization to guarantee all file operations remain confined within `/cloud_storage/`.
   - **IDOR Remediation:** Enforce per-user file authorization checks so users can only view, download, or delete their own resources.
3. **Backend Refactoring & REST Standardization:**
   - Replace brittle delimited string protocols with standardized JSON request/response DTOs.
   - Maintain separation of concerns: Spring Boot acts as the **Control Plane** (managing metadata and access control), while local storage acts as the **Data Plane** (persisting file bytes).
4. **Enhanced JavaFX Desktop Client:**
   - Modernized UI featuring drag-and-drop file upload.
   - Category-based file filtering and interactive storage analytics charts.
   - Secure client-side JWT token storage and automated header injection on outgoing HTTP/2 requests.
5. **Full Containerization & Orchestration:**
   - Complete `docker-compose.yml` multi-service orchestration packaging MySQL, Spring Boot, and FastAPI into reproducible containers.

---

## 3. Why We Are Doing It (Research Gaps & Objectives)

### 3.1 Research Gaps Identified
A survey of 10 academic papers (Joachims 1998, Ghemawat et al. 2003, Biryukov et al. 2016, Kowsari et al. 2019, Söylemez et al. 2022, Das et al. 2023) established three clear research gaps:

* **Gap 1: The Intelligence Gap in Local Cloud Storage:** Commercial systems with AI depend on remote cloud servers. Open-source local storage solutions lack embedded, privacy-preserving machine learning inference for automatic file categorization.
* **Gap 2: The Security Gap in Academic Prototypes:** Most academic software engineering prototypes omit authentication tokens, encryption, input sanitization, and access control. Demonstrating a secure-by-design distributed system provides high educational and engineering value.
* **Gap 3: The Short-Text ML Comparative Study Gap:** Traditional text classification benchmarks (e.g., Joachims 1998 on Reuters-21578) evaluate models on long-form documents (hundreds to thousands of words). Ultra-short text (filenames of 5–30 characters without space separators) represents a fundamentally different domain where character n-grams are critical. Comparing Naive Bayes against Linear SVM under these conditions provides actionable empirical findings.

### 3.2 Research Objectives (SLO / Project Goals)

| Objective | Description | Success Metric |
| :--- | :--- | :--- |
| **Objective 1** | Architect a multi-component distributed system | Independent execution of JavaFX, Spring Boot, FastAPI, and MySQL with zero inter-service blocking. |
| **Objective 2** | Implement an NLP character n-gram pipeline | Robust feature extraction from raw filenames without word delimiters. |
| **Objective 3** | Conduct a comparative analysis of NB vs. Linear SVM | Empirical evaluation on 500+ filenames reporting Precision, Recall, F1, and Accuracy. |
| **Objective 4** | Implement enterprise security standards | 100% elimination of plain-text passwords (BCrypt), JWT validation, and prevention of Path Traversal & IDOR. |
| **Objective 5** | Containerize deployment via Docker Compose | Single-command deployment (`docker-compose up`) of the distributed backend. |
