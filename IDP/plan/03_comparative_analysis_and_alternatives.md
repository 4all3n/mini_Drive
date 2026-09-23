# 03. Comparative Analysis & Justification of Alternatives

A core requirement of this engineering plan is an exhaustive, grounded evaluation of technical alternatives. Below is the systematic comparison of every major architectural decision, contrasting the chosen approach with viable alternatives and detailing the exact rationale for our selection.

---

## 1. Project Direction: Upgrading `mini_Drive` vs. Autonomous IDP

| Attribute | Upgrading `mini_Drive` (Intelligent Cloud Drive) | Building Autonomous IDP (from Scratch) |
| :--- | :--- | :--- |
| **Foundation** | Builds on existing, verified working prototype (`mini_Drive`). | Zero existing codebase; starting from scratch. |
| **Academic Alignment** | Approved Review 2 topic with 10 literature survey papers and slide deck. | High risk of panel pushback on feasibility within timeline. |
| **Complexity** | Distributed microservices, applied NLP, and security engineering. | Involves programmatic `/var/run/docker.sock` interaction and local LLMs. |
| **Review 3 Timeline** | Fully achievable by the scheduled milestone. | Extreme risk of falling behind deadline. |
| **Decision** | **CHOSEN (Primary Engineering Track)** | **Documented as high-level future research** |

### Rationale:
The repository already contains a functional foundation in Java, Spring Boot, JavaFX, and FastAPI. Upgrading this into a secure, distributed, AI-driven file management system directly addresses identified security vulnerabilities, fulfills the empirical ML research gap, and guarantees on-time delivery for university milestones without the high risk of platform engineering blockers.

---

## 2. Text Representation: Character N-Grams vs. Word N-Grams vs. Regex

| Technique | How It Works | Suitability for Filenames | Limitations |
| :--- | :--- | :--- | :--- |
| **Regex / File Extension Rules** | Hardcoded mapping (`.pdf` $\rightarrow$ Document) | Poor (Trivial) | Zero academic novelty. Fails on files without extensions or compound names. |
| **Word-Level N-Grams (Bag-of-Words)** | Tokenizes text based on whitespace separation | Very Poor | Filenames rarely contain spaces (e.g., `tax_return_2025.csv`, `source_v1.tar.gz`), yielding empty or single tokens. |
| **Word Embeddings (Word2Vec / GloVe)** | Dense vectors representing semantic word relations | Poor | Pretrained embeddings do not recognize technical tokens, extensions, or concatenated words. |
| **Character-Level N-Grams ($n=2 \dots 5$)** *(CHOSEN)* | Breaks strings into overlapping character chunks (`rep`, `epo`, `ort`, `rt.`, `t.p`, `.pd`, `pdf`) | **Optimal** | Captures morphological patterns, sub-words, abbreviations, and extensions without requiring whitespace. |

### Why We Chose Character N-Grams:
Filenames are ultra-short text strings (typically 5 to 30 characters). Das et al. (2023) demonstrated that character n-grams combined with TF-IDF significantly outperform word-level features for short, unstructured technical strings because they capture both prefix/suffix morphology and extension patterns without explicit rule programming.

---

## 3. Classifier Selection: Linear SVM & Naive Bayes vs. Deep Learning

| Model Family | Inference Latency | Training Data Required | Resource Footprint | Academic Justification |
| :--- | :--- | :--- | :--- | :--- |
| **Deep Learning (BERT / RoBERTa / LSTMs)** | High (50–200ms) | Large (10,000+ samples) | Heavy (GPU recommended, high VRAM) | **Overkill:** Severe overfitting on 500–1000 filenames; massive latency penalty for file upload. |
| **Local LLM (Llama 3.2 via Ollama)** | Very High (500–3000ms) | Prompt-based | Extremely Heavy (4–8 GB RAM for weights) | **Impractical:** Generating an extension category via an autoregressive LLM wastes vast CPU cycles. |
| **Multinomial Naive Bayes (MNB)** *(CHOSEN - Baseline)* | Ultra-Low (< 1ms) | Small (100–500 samples) | Negligible (< 10 MB RAM) | Classic baseline; probabilistic independence assumption allows lightning-fast computation. |
| **Linear SVM (LinearSVC)** *(CHOSEN - Primary)* | Ultra-Low (< 2ms) | Moderate (300–1000 samples) | Negligible (< 15 MB RAM) | Proven by Joachims (1998) to excel in high-dimensional sparse vector spaces (TF-IDF) by maximizing margins. |

### Why We Chose Linear SVM & Naive Bayes:
1. **Empirical Research Value:** Rather than picking one arbitrary model, implementing both creates a legitimate academic study comparing a generative probabilistic model (Naive Bayes) with a discriminative max-margin model (Linear SVM) on ultra-short text.
2. **Computational Efficiency:** File uploads require instantaneous categorization (< 10ms). SVM and Naive Bayes evaluate in sub-milliseconds on CPU without external GPU dependencies.
3. **Preventing Overfitting:** Complex transformer models memorize small datasets. Linear models with L2 regularization generalize cleanly on short text.

---

## 4. Authentication: Stateless JWT vs. Stateful HTTP Sessions

| Criteria | Stateless JWT (Bearer Tokens) *(CHOSEN)* | Stateful HTTP Sessions (`JSESSIONID`) |
| :--- | :--- | :--- |
| **Server State** | **Zero state** stored in server memory. | Session table or in-memory map stored on server. |
| **Horizontal Scalability** | **Seamless:** Any backend instance can verify token signature. | Requires sticky sessions or a shared Redis cluster. |
| **Desktop Client Fit** | **Optimal:** Token stored in client memory, sent in standard HTTP header. | Requires cookie handling and lifecycle synchronization. |
| **Microservice Propagation** | Token can be forwarded across downstream microservices. | Sessions are tightly coupled to the single origin server. |

### Why We Chose JWT:
JWTs eliminate server-side session storage, allowing the Spring Boot backend to remain completely stateless (`SessionCreationPolicy.STATELESS`). The client retains the token and injects it into the `Authorization: Bearer` header, creating an industry-standard API contract.

---

## 5. Password Security: BCrypt vs. Argon2 vs. PBKDF2 vs. Plaintext

| Algorithm | Cryptographic Type | Spring Security Integration | GPU / ASIC Resistance | Selection Status |
| :--- | :--- | :--- | :--- | :--- |
| **Plaintext (Current)** | None | Broken / Unacceptable | None | **REMOVED** (Critical Vulnerability) |
| **SHA-256 (Simple Hash)** | Fast cryptographic hash | Not recommended | Vulnerable to rainbow tables & GPU brute-force | **REJECTED** |
| **Argon2** | Memory-hard password hash | Requires external native bindings / JNI | Maximum (Winner of Password Hashing Competition) | **DOCUMENTED** (Future Roadmap) |
| **BCrypt** *(CHOSEN)* | Adaptive, salted Blowfish cipher | **Native** out-of-the-box (`BCryptPasswordEncoder`) | High (Salted per password, configurable work factor) | **CHOSEN** |

### Why We Chose BCrypt:
BCrypt provides enterprise-grade protection against rainbow table and brute-force attacks via automatic salting and configurable iteration rounds (work factor 10+). It is natively integrated into Spring Security 6.x without requiring third-party native C libraries (which Argon2 demands on certain host OS environments), ensuring frictionless cross-platform builds.

---

## 6. Client Interface: JavaFX Desktop Client vs. Web Application

| Factor | JavaFX Desktop Client *(CHOSEN)* | Web Application (React / Angular / Vue) |
| :--- | :--- | :--- |
| **University Guideline Compliance** | **100% Compliant:** Fulfills systems/distributed criteria. | **VIOLATION:** Guidelines explicitly prohibit web/mobile apps. |
| **Filesystem Interaction** | Direct OS filesystem access; native streaming download/upload. | Sandboxed by browser; cannot directly control file I/O paths. |
| **Drag-and-Drop Latency** | Instant OS-level drag event handling. | Subject to browser file API limitations and overhead. |
| **Language Consistency** | Shared Java ecosystem with backend data structures. | Requires polyglot frontend stack (JavaScript/TypeScript). |

### Why We Chose JavaFX:
The primary driver is strict adherence to the university evaluation criteria forbidding generic web applications. Beyond compliance, JavaFX provides direct OS-level file streaming, native drag-and-drop file ingest, and zero browser sandboxing restrictions for file downloads.

---

## 7. Storage Engine: Separation of Planes vs. Database Blobs

| Architecture | Metadata Storage | File Binary Storage | Performance & Trade-offs |
| :--- | :--- | :--- | :--- |
| **Monolithic DB (Blobs in MySQL)** | MySQL (`files`) | MySQL `LONGBLOB` | **Poor:** Causes massive database bloat, destroys query cache, makes backups slow. |
| **Remote Cloud (AWS S3 / GCS)** | MySQL | Remote Object Storage | **Rejected:** Destroys privacy; requires paid credentials and constant internet. |
| **Decoupled Planes (GFS Model)** *(CHOSEN)* | **MySQL (Control Plane)** | **Local Disk (Data Plane)** | **Optimal:** Database remains sub-millisecond fast; file streaming leverages OS file caching directly. |

### Why We Chose Decoupled Planes:
Following the architectural principles of the **Google File System (Ghemawat et al., 2003)**, separating the control plane (metadata, indexing, user permissions in MySQL) from the data plane (binary storage on disk in `/cloud_storage/`) guarantees that database queries never suffer from I/O blocking caused by reading or writing large binary payloads.
