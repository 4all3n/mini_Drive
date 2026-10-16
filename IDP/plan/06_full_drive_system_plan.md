# 06. Full Drive System — Feature Plan & Feasibility Analysis

> **Created:** 14 October 2026  
> **Status:** Planning Phase  
> **Goal:** Transform MiniDrive from a flat file store into a proper drive-like system with per-user isolated storage, virtual folder hierarchies, and full file management operations (rename, move, copy, delete, download, properties).

---

## 1. Current State Analysis

### What Exists Today

| Layer | Component | Current Capability | Key Limitation |
|:------|:----------|:-------------------|:---------------|
| **Database** | `FileMetadata.java` | Stores `fileName`, `fileType`, `fileSize`, `serverFilePath`, `uploadedBy`, `uploadDate`, `category` | **No folder concept.** All files exist in a single flat namespace per user. No `parentFolderId`. No `storedFileName` (UUID) separate from display name. |
| **Database** | `User.java` | Stores `id`, `username`, `password` | No storage quota field. No relationship to files via JPA `@OneToMany`. |
| **Backend** | `FileController.java` | Upload, download, delete, list | **All files land in a single `./cloud_storage/` directory.** No per-user subdirectories. No UUID on disk (collision risk). Owner taken from `@RequestParam` instead of JWT. No rename, move, copy, or folder APIs. |
| **Backend** | `FileRepository.java` | `findByUploadedBy(String username)` | Cannot query by folder. Cannot query by parent path. |
| **Backend** | `AiCategorizationService.java` | Calls FastAPI `/predict` endpoint | Hardcoded URL. Works correctly. |
| **Frontend** | `DashboardScreen.java` | Flat `TableView` showing all user files | No folder tree/breadcrumb. No right-click context menu. No rename/move/copy UI. |
| **Frontend** | `FileNetworkService.java` | Upload, download, delete, fetch list | Uses delimited string parsing (`;;;` and `|`). No folder-aware API calls. |
| **Physical Storage** | `./cloud_storage/` | Single flat directory | If User A and User B both upload `resume.pdf`, **User B overwrites User A's file**. |

### What Needs to Change (Summary)
1. **Database schema** must support folders as first-class entities and parent-child relationships.
2. **Physical storage** must isolate each user into their own directory and use UUID-based filenames on disk.
3. **Backend API** must support full CRUD on folders and files: create folder, rename, move, copy, properties, navigate.
4. **Frontend UI** must show a navigable folder tree or breadcrumb with context menus.

---

## 2. Feasibility Assessment

### ✅ Fully Feasible (Can Implement)

| Feature | Complexity | Approach |
|:--------|:----------:|:---------|
| **Per-user storage directories** | Low | Create `./cloud_storage/{username}/` on first upload or registration. All files for that user go inside their directory. |
| **Virtual folder hierarchy** | Medium | New `FolderMetadata` JPA entity with self-referencing `parentFolder`. Files get a `parentFolderId` field. The folder tree is stored in the database, not the physical file system. |
| **Create folder** | Low | Insert a new `FolderMetadata` row with name and parent reference. No physical directory needed (virtual). |
| **Upload to specific folder** | Low | Accept `folderId` parameter in upload. Set it on `FileMetadata`. Physical file still goes to `./cloud_storage/{username}/{uuid}_filename`. |
| **Rename file** | Low | Update `fileName` in the database. Physical file stays the same (UUID-based, so display name and disk name are decoupled). |
| **Rename folder** | Low | Update `name` field on the `FolderMetadata` row. Pure database operation. |
| **Delete file** | Low | Already exists. Delete physical file + database row. |
| **Delete folder** | Medium | Recursively delete all files and sub-folders within the folder (cascade). |
| **Download file** | Low | Already exists. No changes needed beyond IDOR fix. |
| **File properties** | Low | Return full metadata (name, size, type, category, uploadDate, path in folder tree) from existing database fields. |
| **Move file to different folder** | Low | Update `parentFolderId` on the `FileMetadata` row. Pure database operation. |
| **Move folder** | Medium | Update `parentFolder` on the `FolderMetadata` row. Must prevent circular references (folder cannot be moved inside itself). |
| **Copy file** | Medium | Create a new `FileMetadata` row pointing to a new physical copy of the file. Physically copy the bytes on disk. |
| **Copy folder** | High | Recursively copy all sub-folders and files. Each copied file needs a new UUID and new physical disk copy. |
| **List contents of a folder** | Low | Query `FileMetadata` and `FolderMetadata` by `parentFolderId` and `uploadedBy`. |
| **Navigate folder hierarchy (breadcrumb)** | Low | Walk up the `parentFolder` chain from current folder to root. Return as ordered list. |

### ⚠️ Complex but Feasible (Needs Careful Design)

| Feature | Complexity | Notes |
|:--------|:----------:|:------|
| **Prevent circular folder moves** | Medium | When moving Folder A into Folder B, walk up from B to root. If A is encountered, reject the move. |
| **Recursive folder size calculation** | Medium | Sum all `fileSize` values for files in the folder and all sub-folders. Can be expensive; consider caching. |
| **Drag-and-drop in JavaFX** | Medium | JavaFX supports `setOnDragDetected`, `setOnDragOver`, `setOnDragDropped` for both internal reorder (move between folders) and external drop (upload from OS). |

### ❌ Not Feasible in Current Scope

| Feature | Reason |
|:--------|:-------|
| **Real-time collaborative editing** | Would require WebSocket/SSE infrastructure and conflict resolution (operational transforms). Out of scope for a single-semester project. |
| **File versioning** | Requires a complete versioning data model (version chains, diff storage). Adds significant complexity for minimal academic value. |
| **Sharing files between users** | Requires an access-control-list (ACL) or share-link system. Not impossible, but adds a major new domain. Mark as "future scope". |

---

## 3. Database Schema Changes

### Current Schema
```
┌─────────────┐       ┌─────────────────┐
│   users      │       │     files        │
├─────────────┤       ├─────────────────┤
│ id (PK)      │       │ id (PK)          │
│ username     │       │ fileName         │
│ password     │       │ fileType         │
│              │       │ fileSize         │
│              │       │ serverFilePath   │
│              │       │ uploadedBy (str) │
│              │       │ uploadDate       │
│              │       │ category         │
└─────────────┘       └─────────────────┘
  (no FK link)          (flat, no folders)
```

### Proposed Schema
```
┌──────────────┐       ┌──────────────────┐       ┌──────────────────┐
│   users       │       │    folders        │       │     files         │
├──────────────┤       ├──────────────────┤       ├──────────────────┤
│ id (PK)       │◄──┐  │ id (PK)           │◄──┐  │ id (PK)           │
│ username      │   │  │ name              │   │  │ originalName      │
│ password      │   │  │ owner_id (FK)─────┼───┘  │ storedName (UUID) │
│ storageQuota  │   │  │ parent_id (FK)────┼──┐   │ fileType          │
│               │   │  │ createdAt         │  │   │ fileSize          │
└──────────────┘   │  │ updatedAt         │  │   │ serverFilePath    │
                    │  └──────────────────┘  │   │ owner_id (FK)─────┤
                    │    ▲ self-referencing   │   │ folder_id (FK)────┤
                    │    └───────────────────┘   │ uploadDate        │
                    │                             │ category          │
                    └─────────────────────────────│ updatedAt         │
                                                  └──────────────────┘
```

**Key changes:**
1. **New `folders` table** — Self-referencing hierarchy (`parent_id` → `folders.id`). `NULL` parent means root-level folder.
2. **`files` table gains:**
   - `originalName` (display name) + `storedName` (UUID-based physical name) — decouples display from storage.
   - `owner_id` (FK to `users.id`) replaces the string `uploadedBy` — proper relational integrity.
   - `folder_id` (FK to `folders.id`, nullable) — `NULL` means the file is at the user's root level.
   - `updatedAt` — tracks when the file was last renamed/moved.
3. **`users` table gains:** `storageQuota` (optional, for future use).

---

## 4. Physical Storage Layout

### Current (Broken)
```
./cloud_storage/
    resume.pdf          ← User A's file
    resume.pdf          ← User B's upload OVERWRITES User A!
    project.zip
```

### Proposed (Per-User Isolated + UUID)
```
./cloud_storage/
    john_doe/
        a1b2c3d4_resume.pdf
        e5f6g7h8_report.docx
    jane_smith/
        i9j0k1l2_resume.pdf       ← No collision!
        m3n4o5p6_presentation.pptx
```

**Note:** The folder hierarchy is **virtual** (database only). We do NOT create physical sub-directories for virtual folders. All of a user's files live flat inside `./cloud_storage/{username}/`. This is a deliberate design decision:
- Simpler disk I/O (no deeply nested paths).
- Faster file system operations.
- Moving a file between virtual folders is a single database UPDATE, not a physical file move.
- The database `folder_id` tracks which virtual folder a file belongs to.

---

## 5. New REST API Endpoints

### Folder Operations
| Method | Endpoint | Description |
|:-------|:---------|:------------|
| `POST` | `/api/folders/create` | Create a new folder. Body: `{ "name": "Documents", "parentFolderId": null }` |
| `GET` | `/api/folders/list?parentId={id}` | List sub-folders inside a folder. `parentId=null` or omitted → root level. |
| `PUT` | `/api/folders/rename/{id}` | Rename a folder. Body: `{ "newName": "My Docs" }` |
| `PUT` | `/api/folders/move/{id}` | Move a folder. Body: `{ "newParentId": 5 }` |
| `DELETE` | `/api/folders/delete/{id}` | Delete a folder and ALL its contents recursively. |
| `GET` | `/api/folders/breadcrumb/{id}` | Get the full path from root to this folder (for breadcrumb navigation). |
| `GET` | `/api/folders/properties/{id}` | Get folder metadata (name, item count, total size, created date). |

### Enhanced File Operations
| Method | Endpoint | Description |
|:-------|:---------|:------------|
| `POST` | `/api/files/upload` | Upload file. Now accepts optional `folderId` param. |
| `GET` | `/api/files/list?folderId={id}` | List files inside a specific folder. `folderId=null` → root. |
| `GET` | `/api/files/download/{id}` | Download file (existing, with IDOR fix). |
| `DELETE` | `/api/files/delete/{id}` | Delete file (existing, with IDOR fix). |
| `PUT` | `/api/files/rename/{id}` | Rename a file. Body: `{ "newName": "updated_report.pdf" }` |
| `PUT` | `/api/files/move/{id}` | Move file to another folder. Body: `{ "newFolderId": 3 }` |
| `POST` | `/api/files/copy/{id}` | Copy a file. Body: `{ "targetFolderId": 5 }` |
| `GET` | `/api/files/properties/{id}` | Get full file metadata (name, size, type, category, path, dates). |
| `GET` | `/api/files/stats` | Get storage usage stats for the current user. |

### Combined Listing (Files + Folders in One Call)
| Method | Endpoint | Description |
|:-------|:---------|:------------|
| `GET` | `/api/drive/contents?folderId={id}` | Returns `{ "folders": [...], "files": [...], "breadcrumb": [...] }` — everything the UI needs in a single request. |

---

## 6. Backend Implementation Plan

### 6.1 New Files to Create

| File | Purpose |
|:-----|:--------|
| `models/FolderMetadata.java` | JPA Entity for the `folders` table with self-referencing `parentFolder`. |
| `models/dto/FileMetadataDTO.java` | Response DTO to replace delimited string responses. |
| `models/dto/FolderMetadataDTO.java` | Response DTO for folder data. |
| `models/dto/DriveContentsDTO.java` | Combined response DTO (folders + files + breadcrumb). |
| `repository/FolderRepository.java` | JPA repository with queries: `findByOwnerAndParentFolder()`, `findByOwnerAndParentFolderIsNull()`. |
| `controllers/FolderController.java` | REST controller for all folder CRUD operations. |
| `controllers/DriveController.java` | Combined endpoint returning folder contents + breadcrumb in a single call. |
| `services/StorageService.java` | Physical file I/O service (save with UUID, copy, delete). Encapsulates all disk operations. |

### 6.2 Existing Files to Modify

| File | Changes |
|:-----|:--------|
| `models/FileMetadata.java` | Add `originalName`, `storedName`, `folder` (ManyToOne), `owner` (ManyToOne to User), `updatedAt`. Rename `uploadedBy` to use FK. |
| `models/User.java` | Add `storageQuota` field (optional). |
| `repository/FileRepository.java` | Add `findByOwnerAndFolder()`, `findByOwnerAndFolderIsNull()`. |
| `controllers/FileController.java` | Refactor upload to use per-user directories + UUID. Add rename, move, copy, properties endpoints. Extract owner from JWT instead of request param. Return JSON DTOs. |
| `controllers/AuthController.java` | On registration, create the user's physical storage directory `./cloud_storage/{username}/`. |
| `services/AiCategorizationService.java` | Move URL to `application.properties` config (use `@Value`). |
| `application.properties` | Add `ai.service.url`, `storage.root.path` properties. |

---

## 7. Frontend Implementation Plan

### 7.1 New UI Components

| Component | Description |
|:----------|:------------|
| `ui/FolderTreePane.java` | A `TreeView<FolderNode>` sidebar showing the virtual folder hierarchy. Clicking a folder loads its contents in the main table. |
| `ui/BreadcrumbBar.java` | A horizontal `HBox` of clickable `Hyperlink` elements showing the path from root (e.g., `My Drive > Projects > Java`). |
| `ui/ContextMenuFactory.java` | Right-click context menu with: Open, Download, Rename, Move To, Copy To, Delete, Properties. |
| `ui/PropertiesDialog.java` | A modal `Dialog` showing file/folder properties (name, size, type, category, dates, path). |
| `ui/RenameDialog.java` | A small `TextInputDialog` for renaming files/folders. |
| `ui/MoveDialog.java` | A `Dialog` with a `TreeView` of folders for selecting a move/copy destination. |
| `ui/NewFolderDialog.java` | A `TextInputDialog` for creating a new folder. |

### 7.2 Existing Files to Modify

| File | Changes |
|:-----|:--------|
| `DashboardScreen.java` | Redesign layout: Left pane = `FolderTreePane`, Top = `BreadcrumbBar`, Center = `TableView` (showing contents of current folder). Add "New Folder" button. Add right-click context menu on table rows. |
| `FileNetworkService.java` | Add methods: `createFolder()`, `renameFile()`, `renameFolder()`, `moveFile()`, `moveFolder()`, `copyFile()`, `getProperties()`, `getStats()`, `getDriveContents()`. Migrate from delimited string parsing to JSON. |
| `SceneManager.java` | Pass `currentFolderId` state when refreshing the dashboard. Wire up context menu actions. |

---

## 8. Migration Strategy (Data Preservation)

Since the database uses `spring.jpa.hibernate.ddl-auto=update`, Hibernate will auto-create new columns and tables. However, existing data needs migration:

1. **Add new columns with defaults:** `storedName` defaults to `fileName` (existing files don't have UUIDs). `folder_id` defaults to `NULL` (root level). `owner_id` can be populated by matching `uploadedBy` string to `users.id`.
2. **Migrate physical files:** Write a one-time migration script that:
   - Creates `./cloud_storage/{username}/` for each existing user.
   - Moves each user's files from `./cloud_storage/` into their user directory.
   - Renames physical files to `{uuid}_{originalName}`.
   - Updates `serverFilePath` and `storedName` in the database.
3. **This migration is optional if the database is reset** (which is acceptable for a development/academic project).

---

## 9. Implementation Sprint Schedule

### Sprint 1 (Days 1–2): Database & Storage Foundation
- [ ] Create `FolderMetadata.java` entity
- [ ] Modify `FileMetadata.java` (add `originalName`, `storedName`, `folder`, `owner`, `updatedAt`)
- [ ] Modify `User.java` (optional: add `storageQuota`)
- [ ] Create `FolderRepository.java`
- [ ] Update `FileRepository.java` with folder-aware queries
- [ ] Create `StorageService.java` for per-user directory creation and UUID file storage
- [ ] Create all DTO classes (`FileMetadataDTO`, `FolderMetadataDTO`, `DriveContentsDTO`)
- [ ] Update `AuthController.java` to create user storage directory on registration
- [ ] Fix `FileController.java` upload to use UUID + per-user directory

### Sprint 2 (Days 3–4): Folder API & File Operations
- [ ] Create `FolderController.java` (create, list, rename, move, delete, breadcrumb, properties)
- [ ] Add rename endpoint to `FileController.java`
- [ ] Add move endpoint to `FileController.java`
- [ ] Add copy endpoint to `FileController.java`
- [ ] Add properties endpoint to `FileController.java`
- [ ] Add stats endpoint to `FileController.java`
- [ ] Create `DriveController.java` (combined contents endpoint)
- [ ] Implement circular reference prevention for folder moves
- [ ] Implement recursive folder deletion
- [ ] Extract upload owner from JWT SecurityContext (remove `@RequestParam("username")`)

### Sprint 3 (Days 5–6): Frontend Drive UI
- [ ] Build `FolderTreePane.java` with `TreeView`
- [ ] Build `BreadcrumbBar.java`
- [ ] Redesign `DashboardScreen.java` with split-pane layout (sidebar + content)
- [ ] Build context menu (right-click) with Rename, Move, Copy, Delete, Properties
- [ ] Build `RenameDialog.java`, `MoveDialog.java`, `NewFolderDialog.java`, `PropertiesDialog.java`
- [ ] Update `FileNetworkService.java` to use JSON parsing and new API endpoints
- [ ] Wire all UI actions through `SceneManager.java`

### Sprint 4 (Day 7): Integration Testing & Polish
- [ ] Full end-to-end test: Register → Login → Create Folders → Upload to Folder → Navigate → Rename → Move → Copy → Delete
- [ ] Verify IDOR protection on all new endpoints
- [ ] Verify path traversal protection on renamed files
- [ ] Test edge cases: empty folder delete, move to self, copy large file
- [ ] UI polish: loading spinners, error messages, empty state illustrations

---

## 10. Risk Assessment

| Risk | Likelihood | Impact | Mitigation |
|:-----|:----------:|:------:|:-----------|
| Breaking existing upload/download flow during migration | High | Critical | Work on a separate git branch. Keep old endpoints functional until new ones are tested. |
| Circular reference in folder moves | Medium | Data corruption | Implement ancestor-walk check before allowing any folder move. |
| Large recursive folder copy blocking the thread | Medium | UX freeze | Limit initial scope to file-level copy. Folder copy can be marked as "future scope". |
| Frontend complexity explosion | Medium | Time overrun | Build the folder tree and breadcrumb first. Add context menus incrementally. |
| Hibernate auto-migration breaking existing data | Low | Data loss | Back up the database before schema changes. Test on a fresh database first. |

---

## 11. What to Demo & How to Defend

When the panel sees this upgrade, emphasize:

1. **"I designed a virtual folder system"** — The folder hierarchy is stored in the database, not on the physical file system. This is the same approach used by Google Drive and Dropbox internally. It makes move/rename operations O(1) database updates instead of expensive file system operations.

2. **"I solved the filename collision problem"** — Each physical file is stored with a UUID prefix, decoupled from the display name. This is enterprise-standard practice (AWS S3 uses object keys, not file names).

3. **"I implemented per-user storage isolation"** — Each user's files are physically separated in `./cloud_storage/{username}/`. Even if the database is compromised, one user cannot access another user's raw files.

4. **"I built a recursive folder system with circular reference prevention"** — Moving folder A into folder B checks the entire ancestor chain to prevent impossible states.
