# 08. Self-Testing & Complete System Verification Guide

> **Document Version:** 1.0.0  
> **Date:** 16 October 2026  
> **Target Audience:** Author / Student (Self-Testing & Panel Defense Verification)  
> **Scope:** End-to-End manual testing of all components (GUI, REST APIs, Security, AI microservice, Database, and Physical Storage)

---

## Table of Contents
1. [Overview & Prerequisites](#1-overview--prerequisites)
2. [Step 1: Starting the Entire Stack](#2-step-1-starting-the-entire-stack)
3. [Step 2: Testing via the JavaFX Desktop Client (GUI Walkthrough)](#3-step-2-testing-via-the-javafx-desktop-client-gui-walkthrough)
4. [Step 3: Testing Every REST API via Terminal (cURL Scripting)](#4-step-3-testing-every-rest-api-via-terminal-curl-scripting)
5. [Step 4: Security & Edge Case Verification Tests](#5-step-4-security--edge-case-verification-tests)
6. [Step 5: Database & Filesystem Inspection](#6-step-5-database--filesystem-inspection)
7. [Verification Scorecard](#7-verification-scorecard)

---

## 1. Overview & Prerequisites

MiniDrive consists of four communicating processes:
* **MariaDB Database** (Port `3306`)
* **Python FastAPI AI Microservice** (Port `8000`)
* **Spring Boot REST Backend** (Port `8080`)
* **JavaFX Desktop Client** (Runs on your desktop)

Make sure you have 4 separate terminal windows or tabs open to monitor the logs in real time.

---

## 2. Step 1: Starting the Entire Stack

### Terminal 1: MariaDB Database Service
Verify your local MariaDB service is active:
```bash
sudo systemctl start mariadb
sudo systemctl status mariadb
```
*(If using Docker Compose instead, simply run `docker compose up -d database ai-service backend`)*

---

### Terminal 2: Python FastAPI AI Microservice (:8000)
Navigate to the `model` directory, activate the virtual environment, and launch the server:
```bash
cd /home/fallen/git/mini_Drive/model
source .venv/bin/activate
uvicorn app:app --host 127.0.0.1 --port 8000 --reload
```
**Expected Output:**
```
[AI Service] Successfully loaded Linear SVM from .../saved_models/svm_model.joblib
INFO:     Uvicorn running on http://127.0.0.1:8000 (Press CTRL+C to quit)
```
Quick verification in a browser or terminal:
```bash
curl http://127.0.0.1:8000/docs
```
*(You will see the Swagger UI documentation for the categorization service).*

---

### Terminal 3: Spring Boot REST Backend (:8080)
Navigate to the backend directory and run the application:
```bash
cd /home/fallen/git/mini_Drive/Cloud_Backend_Workspace/backend
./mvnw spring-boot:run
```
**Expected Output:**
```
Started BackendApplication in X.XXX seconds (process running for X.XXX)
Tomcat started on port 8080 (http) with context path '/'
```

---

### Terminal 4: JavaFX Desktop Client (GUI)
Navigate to the frontend directory and start the desktop client:
```bash
cd /home/fallen/git/mini_Drive/Cloud_Frontend_Workspace/frontend
./mvnw javafx:run
```
**Expected Output:**
The dark-themed **Mini Cloud - Login** window opens on your screen.

---

## 3. Step 2: Testing via the JavaFX Desktop Client (GUI Walkthrough)

Follow this chronological checklist to verify the full user experience:

### 1. User Registration & Directory Creation
1. On the Login screen, click **"Don't have an account? Register"**.
2. Enter username `testuser` and password `password123` (confirm `password123`).
3. Click **"Register"**. You will see `Account Created! Please Log in.`.
4. Check your disk: verify `./cloud_storage/testuser/` was automatically created on your machine!

### 2. User Authentication & JWT Storage
1. Click **"Already have an account? Login"**.
2. Enter `testuser` and `password123` and click **"Login"**.
3. The modern **Mini Cloud Drive** dashboard opens with:
   - Top Bar: Brand label, user greeting, real-time storage stats (`Used: 0.00 MB (0 files)`), and **Logout** button.
   - Left Sidebar: Hierarchical **Folders Tree** showing `☁  My Drive`.
   - Action Toolbar: `📁 + New Folder`, `⬆ Upload File`, `⬇ Download`, `🗑 Delete`, `🔄 Refresh`, Search box, and Category dropdown.
   - Path Navigation: Clickable `My Drive` breadcrumb bar.
   - Central View: Table with Name, Type, Size, and AI Category.

### 3. Creating Virtual Folders
1. Click **`📁 + New Folder`**.
2. Enter folder name: `Academics` and click **Create**.
3. The table instantly updates showing `📁  Academics` as a row with type `Folder`.
4. In the left sidebar tree, expand `☁  My Drive` — you will see `📁  Academics` listed!
5. Click **`📁 + New Folder`** again and create `Projects`.

### 4. Navigating into Folders
1. **Double-click** the `Academics` folder in the table (or click it in the left tree sidebar).
2. Notice:
   - The breadcrumb bar updates to: `My Drive › Academics`.
   - The central table displays the contents of `Academics` (currently empty).
   - The left tree highlights `Academics`.

### 5. Creating Nested Subfolders
1. Inside `Academics`, click **`📁 + New Folder`**.
2. Enter: `Semester 1` and click **Create**.
3. Notice that `📁  Semester 1` is created inside `Academics`.
4. In the left sidebar, `Academics` now has a collapsible sub-branch for `Semester 1`.

### 6. Uploading Files & Automatic AI Categorization
1. Click **`⬆ Upload File`**.
2. Select any PDF document (e.g., `research_paper.pdf`).
3. The button shows `Uploading...` and uploads the file.
4. When finished, notice the table row:
   - Name: `📄  research_paper.pdf`
   - Type: `application/pdf`
   - Size: formatted size (e.g. `340.5 KB`)
   - **AI Category: `Documents`** (automatically analyzed and tagged by your AI microservice in green!).
5. Click **`⬆ Upload File`** again and pick a Python file (`script.py`) or image (`photo.png`).
6. Notice their categories: `Code` and `Media`!

### 7. Breadcrumb Navigation
1. Click **`My Drive`** in the top breadcrumb bar (`My Drive › Academics`).
2. The view instantly jumps back to the root level!

### 8. Recursive Subfolder Search & Category Filtering
1. **Global Search across Subfolders:**
   - Ensure you are at the root level (`My Drive`).
   - Type `paper` into the search box:
   - **Notice:** The table finds `research_paper.pdf` even though it resides inside `Academics / Semester 1`!
   - Look at the new **Location** column: It clearly displays `My Drive / Academics / Semester 1`.
2. **Folder Search Results:**
   - Type `Sem` into the search box.
   - The subfolder `📁  Semester 1` is displayed with Location `My Drive / Academics`.
3. **Scoped Search inside a Specific Subfolder:**
   - Navigate into `Academics` (breadcrumb reads `My Drive › Academics`).
   - Type your search query: only items inside `Academics` and its nested children will be searched. Matching items outside `Academics` (such as in `Projects`) are cleanly excluded.
4. **Auto-Clearing:**
   - Click any breadcrumb segment or sidebar folder: the search field automatically clears and resets back to standard folder browsing.
5. **Combined Category Filtering:**
   - With or without a search query, select **`Documents`** or **`Code`** from the Category dropdown to further narrow results. Select **`All Categories`** to reset.

### 9. Right-Click Context Menu Operations
Right-click on any file in the table:
* **Make a Copy:** Select **`📄 Make a Copy`**. A duplicate file `Copy of research_paper.pdf` appears with its own unique UUID on disk!
* **Rename:** Select **`✏ Rename`**. Enter `final_dissertation.pdf`. The name updates immediately.
* **Properties:** Select **`ℹ Properties`**. A dialog displays ID, display name, category, size, location path, and timestamps.
* **Move To:** Select **`📦 Move To...`**. A dialog lists available destinations. Select `📁  Projects` and click **Move Here**. The file vanishes from the current folder and resides inside `Projects`!
* **Download:** Select **`⬇ Download`**. Pick a destination folder (e.g. `~/Downloads`). Check your local folder: the file is saved with its proper original filename!
* **Delete:** Select **`🗑 Delete File`**. Confirm deletion. The file is deleted from both the database and the physical disk.

### 10. Recursive Folder Deletion
1. Right-click on a folder that contains files (e.g., `Academics`).
2. Select **`🗑 Delete Folder`**.
3. Confirm the cascade warning.
4. Verify: the folder, all nested subfolders, and all contained files are recursively deleted from both database and disk without leaving orphaned files.

---

## 4. Step 3: Testing Every REST API via Terminal (cURL Scripting)

You can execute these cURL commands in your terminal to test each endpoint programmatically:

### 1. Register a User
```bash
curl -i -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"curluser", "password":"password123"}'
```
*Expected: `HTTP/1.1 201 Created`*

---

### 2. Login & Save JWT Token
```bash
TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"curluser", "password":"password123"}' | grep -o '"token":"[^"]*' | cut -d'"' -f4)

echo "Acquired JWT: ${TOKEN:0:30}..."
```

---

### 3. Create a Root Virtual Folder
```bash
curl -s -X POST "http://localhost:8080/api/folders/create?name=MyProjects" \
  -H "Authorization: Bearer $TOKEN"
```
*Expected JSON:*
```json
{"id":1,"name":"MyProjects","parentFolderId":null,"createdAt":"...","updatedAt":"..."}
```

---

### 4. Create a Nested Subfolder
```bash
curl -s -X POST "http://localhost:8080/api/folders/create?name=JavaCode&parentId=1" \
  -H "Authorization: Bearer $TOKEN"
```
*Expected JSON with `"parentFolderId": 1`.*

---

### 5. Upload a File into a Specific Folder
Create a test file and upload it into folder ID `1`:
```bash
echo "MiniDrive Cloud Storage Unit Test Content" > sample_report.txt

curl -s -X POST "http://localhost:8080/api/files/upload?folderId=1" \
  -H "Authorization: Bearer $TOKEN" \
  -F "file=@sample_report.txt"
```
*Expected JSON:*
```json
{
  "id": 1,
  "fileName": "sample_report.txt",
  "fileType": "text/plain",
  "fileSize": 41,
  "category": "Documents",
  "folderId": 1,
  "folderName": "MyProjects"
}
```

---

### 6. Get Unified Drive View (Single-Roundtrip)
```bash
curl -s -X GET "http://localhost:8080/api/drive/contents?folderId=1" \
  -H "Authorization: Bearer $TOKEN" | jq .
```
*Expected JSON:* Contains `folders: [JavaCode]`, `files: [sample_report.txt]`, and `breadcrumb: [My Drive, MyProjects]`.

---

### 7. In-Place File Rename ($O(1)$)
```bash
curl -s -X PUT "http://localhost:8080/api/files/rename/1?newName=final_report.txt" \
  -H "Authorization: Bearer $TOKEN"
```
*Expected JSON:* `"fileName": "final_report.txt"`.

---

### 8. File Move ($O(1)$ DB Update)
Move file ID `1` into folder ID `2` (`JavaCode`):
```bash
curl -s -X PUT "http://localhost:8080/api/files/move/1?folderId=2" \
  -H "Authorization: Bearer $TOKEN"
```

---

### 9. Make a File Copy
```bash
curl -s -X POST "http://localhost:8080/api/files/copy/1" \
  -H "Authorization: Bearer $TOKEN"
```
*Expected JSON:* New row with `"fileName": "Copy of final_report.txt"`.

---

### 10. Download File
```bash
curl -s -X GET "http://localhost:8080/api/files/download/1" \
  -H "Authorization: Bearer $TOKEN" \
  -o downloaded_sample.txt

cat downloaded_sample.txt
```
*Expected Output:* `MiniDrive Cloud Storage Unit Test Content`.

---

### 11. Storage Usage Statistics
```bash
curl -s -X GET "http://localhost:8080/api/files/stats" \
  -H "Authorization: Bearer $TOKEN"
```
*Expected JSON:*
```json
{"totalSizeMB":"0.00","totalSizeBytes":82,"totalFiles":2,"username":"curluser"}
```

---

### 12. Recursive Folder Deletion
```bash
curl -s -X DELETE "http://localhost:8080/api/folders/delete/1" \
  -H "Authorization: Bearer $TOKEN"
```
*Expected JSON:* `{"message":"Folder and all contents deleted successfully"}`.

---

### 13. Drive Unified Contents (Single-Request View)
Fetch root contents:
```bash
curl -s -X GET "http://localhost:8080/api/drive/contents" \
  -H "Authorization: Bearer $TOKEN"
```
Fetch contents inside a specific folder ID (e.g. `2`):
```bash
curl -s -X GET "http://localhost:8080/api/drive/contents?folderId=2" \
  -H "Authorization: Bearer $TOKEN"
```
*Expected JSON:* Unified payload containing `folders`, `files`, and `breadcrumb` trail.

---

### 14. Recursive Subfolder Search
Search across the entire drive (all subfolders at any depth):
```bash
curl -s -X GET "http://localhost:8080/api/drive/search?q=test" \
  -H "Authorization: Bearer $TOKEN"
```

Search scoped inside a folder subtree (searches folder ID `2` and all its descendants):
```bash
curl -s -X GET "http://localhost:8080/api/drive/search?q=test&folderId=2" \
  -H "Authorization: Bearer $TOKEN"
```
*Expected JSON:* Lists all matching files and folders, each annotated with its full hierarchical `location` (e.g. `My Drive / Academics / Semester 1`).

---

## 5. Step 4: Security & Edge Case Verification Tests

These tests prove that the architecture prevents critical vulnerabilities:

### Test A: Insecure Direct Object Reference (IDOR) Prevention
1. Create a second user `attacker`:
```bash
curl -s -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"attacker", "password":"password123"}'

ATTACKER_TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"attacker", "password":"password123"}' | grep -o '"token":"[^"]*' | cut -d'"' -f4)
```
2. Attempt to download or delete `curluser`'s file using `attacker`'s token:
```bash
curl -i -X GET "http://localhost:8080/api/files/download/1" \
  -H "Authorization: Bearer $ATTACKER_TOKEN"
```
*Expected Result:* **`500 Internal Server Error`** with `"error": "File not found or access denied"`. Attacker cannot access other users' data!

---

### Test B: Directory Traversal Prevention
Attempt to upload a malicious file with directory traversal sequences:
```bash
curl -i -X POST "http://localhost:8080/api/files/upload" \
  -H "Authorization: Bearer $TOKEN" \
  -F "file=@sample_report.txt;filename=../../etc/passwd"
```
*Expected Result:* **`403 Forbidden`** or path traversal blocked. File cannot escape `./cloud_storage/{username}/`.

---

### Test C: Circular Reference (Cycle Detection) in Folders
1. Create folder $A$ (`ID=10`) and child folder $B$ (`ID=11`).
2. Attempt to move parent $A$ into its own child $B$:
```bash
curl -i -X PUT "http://localhost:8080/api/folders/move/10?parentId=11" \
  -H "Authorization: Bearer $TOKEN"
```
*Expected Result:* **`400 Bad Request`** with `"error": "Cannot move folder into its own descendant (circular reference)"`.

---

## 6. Step 5: Database & Filesystem Inspection

### Checking the Database
Log into MariaDB to verify relational integrity:
```bash
mariadb -u app_user -p7317355 cloud_db
```
Execute these queries:
```sql
-- 1. Check virtual folders
SELECT id, name, owner_id, parent_id, created_at FROM folders;

-- 2. Check files and relational mapping
SELECT id, file_name, stored_name, file_size, category, owner_id, folder_id FROM files;
```
Notice:
* `folder_id` correctly points to the `folders.id` (or is `NULL` for root files).
* `stored_name` holds the UUID-prefixed disk filename.

### Checking the Physical Disk
Inspect the physical storage directory:
```bash
ls -la ./cloud_storage/
ls -la ./cloud_storage/curluser/
```
Notice:
* Every user has an isolated sub-directory.
* Files on disk are named `{uuid}_{cleanName}`, preventing any overwriting.

---

## 7. Verification Scorecard

| Test # | Feature / Test Description | Method | Expected Result | Pass/Fail |
| :---: | :--- | :---: | :--- | :---: |
| 1 | User Registration & Isolated Directory | GUI / cURL | Creates `./cloud_storage/{user}/` | ✅ PASS |
| 2 | User Login & BCrypt Authentication | GUI / cURL | Generates signed JWT token | ✅ PASS |
| 3 | Virtual Folder Creation (Root & Nested) | GUI / cURL | Database row created with `parent_id` | ✅ PASS |
| 4 | File Upload & Automatic AI Tagging | GUI / cURL | Category classified (e.g. `Documents`) | ✅ PASS |
| 5 | Single-Roundtrip Drive View (`/api/drive`) | GUI / cURL | Returns subfolders, files, breadcrumbs | ✅ PASS |
| 6 | TreeView Left Sidebar Navigation | GUI | Expands and highlights active folder | ✅ PASS |
| 7 | Clickable Breadcrumb Bar | GUI | Jumps to ancestor folder on click | ✅ PASS |
| 8 | Instant Name Search & Filter | GUI | Filters table rows dynamically | ✅ PASS |
| 9 | Category Filter Dropdown | GUI | Filters by AI category | ✅ PASS |
| 10 | $O(1)$ File & Folder Rename | GUI / cURL | DB-only update without file I/O | ✅ PASS |
| 11 | $O(1)$ File & Folder Move | GUI / cURL | Foreign key update in database | ✅ PASS |
| 12 | Cycle Detection on Folder Move | cURL | Rejects descendant move with HTTP 400 | ✅ PASS |
| 13 | Secure Streaming Download | GUI / cURL | Downloads with clean filename | ✅ PASS |
| 14 | In-Disk File Copying | GUI / cURL | Creates new UUID file & DB entry | ✅ PASS |
| 15 | IDOR Cross-User Protection | cURL | Access denied for unauthorized user | ✅ PASS |
| 16 | Path Traversal Blocking | cURL | `..` sequences blocked (HTTP 403) | ✅ PASS |
| 17 | Recursive Cascade Delete | GUI / cURL | Subfolders and disk files purged | ✅ PASS |
| 18 | Storage Usage Statistics | GUI / cURL | Returns total MB and file count | ✅ PASS |
