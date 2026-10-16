# 07. Unified Master Roadmap — All Pending Work + Drive System Upgrade

> **Created:** 14 October 2026  
> **Deadline:** Draft Review: 16 Oct | Final Viva: 28 Oct  
> **Source Documents:** `pending_work_audit.md` (all pending fixes/features) + `06_full_drive_system_plan.md` (drive system upgrade)  
> **Purpose:** A single, prioritized execution schedule combining everything that needs to happen.

---

## Phase 0: Critical Bug Fixes (Day 1 — 15 Oct)
*These are blocking issues that must be resolved before any new feature work.*

| # | Task | File(s) | Est. Time | Status |
|:-:|:-----|:--------|:---------:|:------:|
| 0.1 | **Fix physical file collision** — Add UUID prefix to stored filename on disk | `FileController.java` | 1 hr | ☑ |
| 0.2 | **Secure upload owner extraction** — Get username from JWT `SecurityContextHolder` instead of `@RequestParam("username")` | `FileController.java` | 1 hr | ☑ |
| 0.3 | **Externalize AI service URL** — Move hardcoded `http://127.0.0.1:8000` to `application.properties` using `@Value` | `AiCategorizationService.java`, `application.properties` | 30 min | ☑ |
| 0.4 | **Add `storage.root.path` config** — Make storage directory configurable instead of hardcoded `System.getProperty("user.dir") + "/cloud_storage/"` | `FileController.java`, `application.properties` | 30 min | ☑ |

---

## Phase 1: Database Schema Evolution (Day 1 — 15 Oct)
*Build the foundation for the drive system.*

| # | Task | File(s) | Est. Time | Status |
|:-:|:-----|:--------|:---------:|:------:|
| 1.1 | **Create `FolderMetadata.java`** — JPA Entity with `id`, `name`, `owner` (ManyToOne → User), `parentFolder` (self-referencing ManyToOne), `createdAt`, `updatedAt` | New: `models/FolderMetadata.java` | 1 hr | ☑ |
| 1.2 | **Evolve `FileMetadata.java`** — Add `originalName`, `storedName` (UUID), `owner` (ManyToOne → User), `folder` (ManyToOne → FolderMetadata, nullable), `updatedAt`. Keep `uploadedBy` temporarily for backward compat. | `models/FileMetadata.java` | 1 hr | ☑ |
| 1.3 | **Create `FolderRepository.java`** — Methods: `findByOwnerAndParentFolder()`, `findByOwnerAndParentFolderIsNull()`, `findByOwnerAndId()` | New: `repository/FolderRepository.java` | 30 min | ☑ |
| 1.4 | **Update `FileRepository.java`** — Add `findByOwnerAndFolder()`, `findByOwnerAndFolderIsNull()` | `repository/FileRepository.java` | 30 min | ☑ |
| 1.5 | **Create all DTO classes** — `FileMetadataDTO`, `FolderMetadataDTO`, `DriveContentsDTO` (combined response with folders + files + breadcrumb) | New: `models/dto/*.java` | 1 hr | ☑ |

---

## Phase 2: Per-User Storage & Core Drive API (Days 1–2 — 15–16 Oct)

| # | Task | File(s) | Est. Time | Status |
|:-:|:-----|:--------|:---------:|:------:|
| 2.1 | **Create `StorageService.java`** — Handles per-user directory creation (`./cloud_storage/{username}/`), UUID file writing, file copying, file deletion from disk | New: `services/StorageService.java` | 2 hr | ☑ |
| 2.2 | **Update `AuthController.java`** — On successful registration, create the user's physical storage directory | `controllers/AuthController.java` | 30 min | ☑ |
| 2.3 | **Refactor `FileController.java` upload** — Use `StorageService` for UUID-based file storage in per-user directory. Accept optional `folderId` parameter. Return JSON DTO. | `controllers/FileController.java` | 2 hr | ☑ |
| 2.4 | **Refactor `FileController.java` list** — Return `ResponseEntity<List<FileMetadataDTO>>` instead of delimited strings. Accept `folderId` query parameter. | `controllers/FileController.java` | 1 hr | ☑ |
| 2.5 | **Add file rename endpoint** — `PUT /api/files/rename/{id}` — Update `originalName` in DB only (disk file unchanged due to UUID) | `controllers/FileController.java` | 30 min | ☑ |
| 2.6 | **Add file move endpoint** — `PUT /api/files/move/{id}` — Update `folder` FK in DB only | `controllers/FileController.java` | 30 min | ☑ |
| 2.7 | **Add file copy endpoint** — `POST /api/files/copy/{id}` — Create new DB row + physical disk copy via `StorageService` | `controllers/FileController.java` | 1 hr | ☑ |
| 2.8 | **Add file properties endpoint** — `GET /api/files/properties/{id}` — Return full `FileMetadataDTO` with folder path | `controllers/FileController.java` | 30 min | ☑ |
| 2.9 | **Add storage stats endpoint** — `GET /api/files/stats` — Return total bytes + per-category breakdown for current user | `controllers/FileController.java` | 1 hr | ☑ |

---

## Phase 3: Folder API (Day 2 — 16 Oct)

| # | Task | File(s) | Est. Time | Status |
|:-:|:-----|:--------|:---------:|:------:|
| 3.1 | **Create `FolderController.java`** — Full REST controller for folder CRUD | New: `controllers/FolderController.java` | 3 hr | ☑ |
| 3.2 | **Implement create folder** — `POST /api/folders/create` with `name` and optional `parentFolderId` | `FolderController.java` | 30 min | ☑ |
| 3.3 | **Implement list sub-folders** — `GET /api/folders/list?parentId={id}` | `FolderController.java` | 30 min | ☑ |
| 3.4 | **Implement rename folder** — `PUT /api/folders/rename/{id}` | `FolderController.java` | 15 min | ☑ |
| 3.5 | **Implement move folder** — `PUT /api/folders/move/{id}` with circular reference prevention | `FolderController.java` | 1 hr | ☑ |
| 3.6 | **Implement recursive folder delete** — `DELETE /api/folders/delete/{id}` — Cascade delete all files + sub-folders | `FolderController.java` | 1 hr | ☑ |
| 3.7 | **Implement breadcrumb** — `GET /api/folders/breadcrumb/{id}` — Walk up parent chain to root | `FolderController.java` | 30 min | ☑ |
| 3.8 | **Create combined drive endpoint** — `GET /api/drive/contents?folderId={id}` — Returns folders + files + breadcrumb in one response | New: `controllers/DriveController.java` | 1 hr | ☑ |

---

## Phase 4: Frontend Drive UI Overhaul (Days 3–4 — 17–18 Oct)

| # | Task | File(s) | Est. Time | Status |
|:-:|:-----|:--------|:---------:|:------:|
| 4.1 | **Build `FolderTreePane.java`** — `TreeView<FolderNode>` sidebar showing folder hierarchy. Lazy-loading children on expand. | New: `ui/FolderTreePane.java` | 3 hr | ☑ |
| 4.2 | **Build `BreadcrumbBar.java`** — Clickable path navigation (`My Drive > Projects > Java`) | New: `ui/BreadcrumbBar.java` | 1 hr | ☑ |
| 4.3 | **Redesign `DashboardScreen.java`** — SplitPane layout: Left = folder tree, Top = breadcrumb + action buttons, Center = file/folder table. Table shows both folders (with folder icon) and files. | `ui/DashboardScreen.java` | 3 hr | ☑ |
| 4.4 | **Build right-click context menu** — Actions: Open, Download, Rename, Move To, Copy To, Delete, Properties | New: `ui/ContextMenuFactory.java` | 2 hr | ☑ |
| 4.5 | **Build dialogs** — `RenameDialog`, `NewFolderDialog`, `MoveDialog` (with folder tree picker), `PropertiesDialog` | New: `ui/dialogs/*.java` | 2 hr | ☑ |
| 4.6 | **Update `FileNetworkService.java`** — Add all new API methods. Migrate from delimited string parsing to JSON (use `java.json` or simple string-based JSON parsing). | `network/FileNetworkService.java` | 3 hr | ☑ |
| 4.7 | **Wire all actions in `SceneManager.java`** — Connect context menu actions, folder navigation, breadcrumb clicks, New Folder button | `controllers/SceneManager.java` | 2 hr | ☑ |
| 4.8 | **Add category filter & search bar** — `TextField` for instant search + `ComboBox` for category filtering using `FilteredList` | `ui/DashboardScreen.java` | 2 hr | ☑ |

---

## Phase 5: Testing, Polish & DevOps (Day 5 — 19 Oct)

| # | Task | File(s) | Est. Time | Status |
|:-:|:-----|:--------|:---------:|:------:|
| 5.1 | **End-to-end integration test** — Register → Login → Create Folders → Upload into Folder → Navigate → Rename → Move → Copy → Delete | Manual testing & Guide | 2 hr | ☑ |
| 5.2 | **Write JUnit tests** — JWT validation, path traversal blocking, IDOR prevention, folder circular reference prevention | `test/` | 3 hr | ☐ |
| 5.3 | **Create Dockerfiles** — Backend multi-stage (Maven build + JRE runtime), AI service (Python slim + scikit-learn + uvicorn) | New: `Dockerfile` files | 2 hr | ☑ |
| 5.4 | **Create `docker-compose.yml`** — MariaDB + Backend + AI Service orchestration | New: `docker-compose.yml` | 1 hr | ☑ |
| 5.5 | **UI polish** — Loading spinners, empty state messages, error handling, file type icons | Frontend UI files | 2 hr | ☑ |

---

## Phase 6: Academic Deliverables (Days 6–12 — 20–26 Oct)

| # | Task | Est. Time | Status |
|:-:|:-----|:---------:|:------:|
| 6.1 | **Draft Dissertation Chapters 1–4** — Introduction, Literature Review (10–12 papers), System Design & UML, ML Methodology | 3 days | ☐ |
| 6.2 | **Draft Dissertation Chapters 5–8** — Implementation, Experimental Results, Security Analysis, Conclusion & Future Scope | 3 days | ☐ |
| 6.3 | **Dissertation formatting** — IEEE/VIT template, table of contents, list of figures, references (APA) | 1 day | ☐ |
| 6.4 | **Plagiarism check** — Run through Turnitin/university tool, verify < 15% | 2 hr | ☐ |
| 6.5 | **Final Viva presentation deck** — 25 slides covering 100% completed system | 1 day | ☐ |
| 6.6 | **Live demo recording** — Screen recording of the full working system as backup for viva | 1 hr | ☐ |

---

## Effort Summary

| Phase | Description | Total Est. Time |
|:-----:|:------------|:---------------:|
| 0 | Critical Bug Fixes | 3 hours |
| 1 | Database Schema | 4 hours |
| 2 | Storage & Core File API | 9 hours |
| 3 | Folder API | 7 hours |
| 4 | Frontend UI Overhaul | 18 hours |
| 5 | Testing & DevOps | 10 hours |
| 6 | Dissertation & Viva | 8 days |
| **Total Code Work** | Phases 0–5 | **~51 hours (~6–7 working days)** |
| **Total Academic** | Phase 6 | **~8 working days** |

---

## Git Branch Strategy

```
main
  └── feature/intelligent-cloud-drive  (current)
        └── feature/drive-system-v2    (new branch for this work)
```

Create `feature/drive-system-v2` from the current branch. Merge back once all phases 0–5 are complete and tested.
