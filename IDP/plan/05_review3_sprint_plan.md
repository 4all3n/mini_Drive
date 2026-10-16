# 🚨 Review 3 Sprint Plan — 5-Day Countdown to 30th September

> **Deadline:** 30/09/2026 (Wednesday) — Extramural hours 11:40 AM – 1:30 PM  
> **Course Code:** PAMCA698 — MCA (Internship-I / Dissertation-I)  
> **Today:** 25/09/2026 (Thursday Night)  
> **Available Days:** 5 working days (Sept 26 – Sept 30 morning)

---

## 📋 Review 3 PPT Requirements Checklist

The panel mandates the following 14 items. Each is mapped to its **status** (already done from Review 2 vs. new work needed) and the **day it will be completed**.

| # | PPT Slide Requirement | Status | Work Needed | Scheduled |
|---|---|---|---|---|
| 1 | Project Title (Name, Reg No, Guide) | ✅ Done (Review 2) | Copy from Review 2 PPT | Day 5 (PPT) |
| 2 | Problem Statement | ✅ Done (Review 2) | Minor refinement | Day 5 (PPT) |
| 3 | Literature Review & Missing Gaps | ✅ Done (Review 2) | Copy from Review 2 PPT (10 papers) | Day 5 (PPT) |
| 4 | Research Objectives Identified | ✅ Done (Review 2) | Copy from Review 2 PPT (5 objectives) | Day 5 (PPT) |
| 5 | Proposed Solution to the Problem | ✅ Done (Review 2) | Update with implementation details | Day 5 (PPT) |
| 6 | Architecture Diagram | ✅ Done (Review 2) | Update diagram to show security layer | Day 5 (PPT) |
| 7 | Dataset Description | 🔴 **NEW** | Build 500+ filename dataset, document statistics | **Day 1** |
| 8 | Modules Identified | ⚠️ Partially Done | Formalize the 5 module breakdown for PPT | Day 5 (PPT) |
| 9 | Module Description with Algorithms | 🔴 **NEW** | Write algorithm per module (TF-IDF, NB, SVM, BCrypt, JWT) | Day 4 |
| 10 | **Implementation (50% of work)** | 🔴 **NEW — CRITICAL** | Code: AI pipeline + Security + Backend refactor | **Days 1–3** |
| 11 | Evaluation Metrics, Results & Graphs | 🔴 **NEW** | Run NB vs SVM benchmark, generate plots | **Day 2** |
| 12 | Guide's Approval Mail Screenshot | ⚠️ **YOU** | **Allen must get this from guide** | **Day 3** |
| 13 | Status of Work / Timeline | 🔴 **NEW** | Gantt chart showing progress & remaining work | Day 4 |
| 14 | Research Paper Progress | 🔴 **NEW** | Show outline / draft sections of dissertation | Day 4 |

---

## ⏰ Day-by-Day Execution Plan

---

### 🟢 DAY 1 — Friday, 26 September
#### **Theme: AI Dataset + ML Pipeline + Model Training**
*This is the highest-priority day. Everything downstream (graphs, results, PPT content) depends on the ML work being done first.*

| Time Block | Task | Deliverable |
|---|---|---|
| **Morning (9 AM – 12 PM)** | **Build the expanded dataset** | `model/dataset.py` with 500+ filenames across 6 categories |
| | Create a Python file containing at minimum: | |
| | • 80+ Documents (`.pdf`, `.docx`, `.txt`, `.xlsx`, `.pptx`, `.csv`) | |
| | • 80+ Code (`.java`, `.py`, `.js`, `.html`, `.css`, `.cpp`, `.ts`, `.go`) | |
| | • 80+ Media — split into Images (`.jpg`, `.png`, `.svg`, `.webp`) + Audio (`.mp3`, `.wav`) + Video (`.mp4`, `.mkv`) | |
| | • 80+ Archives (`.zip`, `.tar.gz`, `.7z`, `.rar`, `.jar`) | |
| | • 80+ System/Logs (`.log`, `.ini`, `.cfg`, `.conf`, `.out`) | |
| | • 80+ Executables (`.exe`, `.msi`, `.sh`, `.bat`, `.apk`, `.dmg`, `.deb`) | |
| **Afternoon (1 PM – 5 PM)** | **Build the comparative ML pipeline** | `model/train_and_evaluate.py` |
| | • Implement `TfidfVectorizer(analyzer='char_wb', ngram_range=(2, 5))` | |
| | • Train **Multinomial Naive Bayes** pipeline | |
| | • Train **Linear SVM (LinearSVC)** pipeline | |
| | • Implement 80/20 stratified train-test split | |
| | • Implement 5-fold cross-validation | |
| | • Compute: Accuracy, Precision, Recall, F1-Score (macro & per-class) | |
| | • Generate Confusion Matrix for both models | |
| | • Save trained models with `joblib` to `model/saved_models/` | |
| **Evening (6 PM – 9 PM)** | **Update FastAPI app to use saved models** | Updated `model/app.py` |
| | • Load the best model from `joblib` on startup (no retraining) | |
| | • Update `/predict` to return `{category, confidence, model_used}` | |
| | • Add `/evaluate` endpoint returning full metrics JSON | |
| | • Test both endpoints manually with `curl` or browser | |

**Day 1 Exit Criteria:**
- [ ] `model/dataset.py` exists with 500+ labeled filenames
- [ ] `model/train_and_evaluate.py` runs and prints NB vs SVM comparison table
- [ ] Confusion matrices generated for both models
- [ ] `model/saved_models/` contains serialized `.joblib` files
- [ ] `model/app.py` loads saved models and serves `/predict` and `/evaluate`

---

### 🟡 DAY 2 — Saturday, 27 September
#### **Theme: Evaluation Graphs + Backend Security Implementation**

| Time Block | Task | Deliverable |
|---|---|---|
| **Morning (9 AM – 12 PM)** | **Generate all evaluation graphs & charts** | `model/results/` folder with PNG charts |
| | Using matplotlib/seaborn, generate: | |
| | • Bar chart: Accuracy comparison (NB vs SVM) | |
| | • Grouped bar chart: Per-class Precision, Recall, F1 | |
| | • Two confusion matrix heatmaps (one per model) | |
| | • Table image: Summary metrics comparison | |
| | These will go directly into the PPT slides for Item 11 | |
| **Afternoon (1 PM – 5 PM)** | **Backend: BCrypt Password Hashing** | Modified backend code |
| | • Add `spring-boot-starter-security` to `pom.xml` | |
| | • Create `SecurityConfig.java` with `BCryptPasswordEncoder` bean | |
| | • Update `AuthController.java`: hash password on register, verify on login | |
| | • Test: register a user, verify password is hashed in MySQL | |
| **Evening (6 PM – 9 PM)** | **Backend: JWT Authentication** | JWT infrastructure |
| | • Add `jjwt` dependencies to `pom.xml` | |
| | • Create `JwtTokenProvider.java` (generate + validate tokens) | |
| | • Create `JwtAuthenticationFilter.java` (extract bearer token from headers) | |
| | • Wire into `SecurityConfig.java` (permit `/api/auth/**`, secure `/api/files/**`) | |
| | • Test: login returns a JWT, file endpoints reject requests without token | |

**Day 2 Exit Criteria:**
- [ ] `model/results/` contains 4+ PNG charts ready for PPT
- [ ] Backend compiles and runs with BCrypt hashing (no more plaintext passwords)
- [ ] JWT generation and validation works end-to-end
- [ ] Can demonstrate: Register → Login (get token) → Use token to upload file

---

### 🔵 DAY 3 — Sunday, 28 September
#### **Theme: Backend Hardening + Frontend Token Integration + Working Demo**

| Time Block | Task | Deliverable |
|---|---|---|
| **Morning (9 AM – 12 PM)** | **Backend: Path Traversal & IDOR Fixes** | Hardened `FileController.java` |
| | • Add filename sanitization (strip `../`, normalize path) | |
| | • Add ownership check on download and delete endpoints | |
| | • Refactor `/api/files/list` to return **JSON** instead of delimited strings | |
| | • Update `AiCategorizationService.java` to use proper JSON parsing | |
| **Afternoon (1 PM – 4 PM)** | **Frontend: JWT Token Integration** | Updated JavaFX client |
| | • Update `AuthNetworkService.java` to parse JWT from login response | |
| | • Update `FileNetworkService.java` to attach `Authorization: Bearer` header | |
| | • Update `FileNetworkService.fetchUserFiles()` to parse JSON responses | |
| | • Test full flow: Login → Dashboard loads files → Upload → Download → Delete | |
| **Afternoon (4 PM – 6 PM)** | **Full Integration Test** | Working end-to-end demo |
| | • Start MySQL, FastAPI AI service, Spring Boot backend | |
| | • Launch JavaFX client | |
| | • Record or screenshot the working flow for PPT evidence | |
| **Evening (6 PM – 8 PM)** | **Guide Approval & Screenshots** | Evidence for PPT items 10, 12 |
| | • Take screenshots of: running backend logs, AI prediction console, JavaFX dashboard | |
| | • **⚠️ Allen: Send guide the progress update and get approval mail reply** | |

**Day 3 Exit Criteria:**
- [ ] Full stack runs: JavaFX → Spring Boot (JWT secured) → FastAPI AI → MySQL
- [ ] Path traversal attacks are blocked
- [ ] IDOR attacks are blocked (user A cannot access user B's files)
- [ ] JSON-based file listing works
- [ ] Screenshots captured for PPT slides
- [ ] Guide approval mail obtained (or in progress)

---

### 🟣 DAY 4 — Monday, 29 September
#### **Theme: PPT Content Preparation + Documentation**

| Time Block | Task | Deliverable |
|---|---|---|
| **Morning (9 AM – 12 PM)** | **Write Module Descriptions with Algorithms** (PPT Item 9) | Content ready for slides |
| | Document each module with its specific algorithm: | |
| | **Module 1 — Repository Ingestion & File Storage:** Multipart streaming, disk I/O, GFS-inspired control/data plane separation | |
| | **Module 2 — Agentic AI Classification:** TF-IDF char n-grams (n=2..5), Multinomial NB (Bayes' theorem), Linear SVM (max-margin hyperplane) | |
| | **Module 3 — Authentication & Security:** BCrypt (Blowfish cipher, salted, cost-factor 10), JWT (HMAC-SHA256 signing) | |
| | **Module 4 — RESTful API Gateway:** Spring Boot MVC, JSON DTO serialization, Spring Security filter chain | |
| | **Module 5 — Desktop Presentation Layer:** JavaFX scene graph, HTTP/2 client, token state management | |
| **Afternoon (1 PM – 4 PM)** | **Create Timeline / Gantt Chart** (PPT Item 13) | Timeline image |
| | • Create a visual Gantt chart showing: | |
| | — Months 1–2: Literature Review & Synopsis (DONE) | |
| | — Months 2–4: Application Development (IN PROGRESS — 50%) | |
| | — Months 3–5: AI Model Development (IN PROGRESS — 60%) | |
| | — Months 5–6: Integration & Testing (UPCOMING) | |
| | — Months 7–8: Report Writing & Viva (UPCOMING) | |
| **Afternoon (4 PM – 6 PM)** | **Research Paper Progress Section** (PPT Item 14) | Outline / draft |
| | • Create a dissertation chapter outline (TOC with ~8 chapters) | |
| | • Show word count progress (e.g., "15 of 60 pages drafted") | |
| | • List completed sections: Abstract, Literature Review, System Design | |
| | • List remaining sections: Implementation, Results, Conclusion | |
| **Evening (6 PM – 9 PM)** | **Dataset Description Slide Content** (PPT Item 7) | Stats and tables |
| | • Total sample count, per-category distribution table | |
| | • Category distribution bar chart | |
| | • Sample filenames per category | |
| | • Train/test split ratio (80/20) | |

**Day 4 Exit Criteria:**
- [ ] Module-algorithm mapping document complete
- [ ] Gantt chart / timeline visual created
- [ ] Research paper outline with progress percentages
- [ ] Dataset description with distribution charts

---

### 🔴 DAY 5 — Tuesday, 30 September (Presentation Day)
#### **Theme: PPT Assembly + Rehearsal**

| Time Block | Task | Deliverable |
|---|---|---|
| **Early Morning (7 AM – 9 AM)** | **Assemble the full PPT** | `Review3_Presentation.pptx` |
| | Build slides in this exact order: | |
| | **Slide 1:** Title (Name, Reg No, Guide Name) | |
| | **Slide 2:** Problem Statement (3 problems) | |
| | **Slide 3–4:** Literature Review Table (10 papers) + Research Gaps | |
| | **Slide 5:** Research Objectives (5 objectives) | |
| | **Slide 6:** Proposed Solution | |
| | **Slide 7:** Architecture Diagram (updated with security layer) | |
| | **Slide 8:** Dataset Description (stats, distribution chart) | |
| | **Slide 9:** Modules Identified (5 modules) | |
| | **Slide 10:** Module Descriptions with Algorithms | |
| | **Slide 11–12:** Implementation Screenshots (50% demo) | |
| | **Slide 13–14:** Evaluation Metrics — NB vs SVM Results & Graphs | |
| | **Slide 15:** Guide Approval Mail Screenshot | |
| | **Slide 16:** Timeline / Gantt Chart | |
| | **Slide 17:** Research Paper Progress | |
| | **Slide 18:** References | |
| | **Slide 19:** Thank You | |
| **Morning (9 AM – 10:30 AM)** | **Rehearsal** | Timed practice |
| | • Practice the full presentation (target 12–15 min) | |
| | • Anticipate panel questions (refer to Review2_Speaking_Notes.txt Part 3) | |
| | • Make sure live demo environment is ready as backup | |
| **10:30 AM – 11:30 AM** | **Travel & Setup** | Arrive at venue |
| **11:40 AM – 1:30 PM** | **🎤 PRESENTATION** | Review 3 Complete |

---

## 🎯 Critical Path Summary

```
Day 1 (Fri 26): Dataset → ML Pipeline → Model Training → Saved Models
                            ↓
Day 2 (Sat 27): Evaluation Graphs ──→ BCrypt Implementation ──→ JWT Implementation
                            ↓                                        ↓
Day 3 (Sun 28): Path Traversal Fix + IDOR Fix + JSON API ──→ Frontend JWT ──→ Full Demo + Screenshots
                            ↓
Day 4 (Mon 29): Module Docs + Gantt Chart + Paper Outline + Dataset Stats
                            ↓
Day 5 (Tue 30): PPT Assembly ──→ Rehearsal ──→ 🎤 PRESENT
```

---

## ⚠️ Risk Mitigation

| Risk | Likelihood | Impact | Mitigation |
|---|---|---|---|
| Spring Security config blocks all endpoints | High | Blocks demo | Keep a `SecurityConfig` toggle — can disable JWT filter if broken, demo BCrypt separately |
| ML model accuracy below 85% | Low | Weak results slide | Dataset already uses distinctive file extensions; char n-grams handle this well. Increase training samples if needed. |
| JavaFX fails to parse new JSON responses | Medium | Broken frontend | Keep a branch with old delimiter protocol as fallback; demo backend via Postman/curl if needed |
| Guide doesn't respond with approval mail | Medium | Missing PPT item 12 | Send mail by Day 2 evening at latest; follow up on Day 3 morning. Can show "sent mail" screenshot as fallback. |
| Time overrun on security code | Medium | Days 2–3 slip | BCrypt alone is enough for "50% implementation". JWT can be shown as "in progress" if needed. |

---

## 📦 Deliverables Checklist (Final)

### Code Deliverables (for "50% Implementation"):
- [ ] `model/dataset.py` — 500+ labeled filenames
- [ ] `model/train_and_evaluate.py` — NB vs SVM comparative pipeline
- [ ] `model/saved_models/*.joblib` — Serialized trained models
- [ ] `model/app.py` — Updated FastAPI with `/predict` and `/evaluate`
- [ ] `model/results/*.png` — Evaluation charts and confusion matrices
- [ ] `AuthController.java` — BCrypt password hashing
- [ ] `SecurityConfig.java` — Spring Security configuration
- [ ] `JwtTokenProvider.java` — Token generation and validation
- [ ] `JwtAuthenticationFilter.java` — Bearer token extraction filter
- [ ] `FileController.java` — Path traversal fix + IDOR fix + JSON responses

### Presentation Deliverables:
- [ ] `Review3_Presentation.pptx` — 18–19 slides covering all 14 required items
- [ ] Screenshots of running system (backend logs, AI predictions, JavaFX dashboard)
- [ ] Guide approval mail screenshot
- [ ] Evaluation result graphs (bar charts, confusion matrices)
- [ ] Gantt chart / timeline visual
- [ ] Dissertation outline showing progress

---

> **Bottom Line:** The critical work is front-loaded on Days 1–2 (AI pipeline + Security). Day 3 is integration and demo capture. Day 4 is pure documentation and PPT content. Day 5 is assembly and showtime. Stick to this schedule and you will walk into Review 3 with a strong 50% implementation, real ML results with graphs, and a polished presentation.
