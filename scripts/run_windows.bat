@echo off
REM ==============================================================================
REM MiniDrive - Windows Backend Node Launcher
REM Starts AI Microservice (port 8000) and Spring Boot Backend (port 8080)
REM ==============================================================================

title MiniDrive Backend Node (Windows)
echo ==================================================================
echo 🚀 MiniDrive - Launching Backend Services on Windows
echo ==================================================================

set "PROJECT_ROOT=%~dp0.."
cd /d "%PROJECT_ROOT%"

REM 1. Display Current Machine IP Address
echo.
echo 📡 Detecting Windows Laptop IP Address (use this on the Linux laptop):
ipconfig | findstr /i "IPv4 Address"
echo.

REM 2. Create Storage Folder if missing
if not exist "%PROJECT_ROOT%\cloud_storage" (
    mkdir "%PROJECT_ROOT%\cloud_storage"
)

REM 3. Launch AI Microservice in separate window
echo 🤖 [1/2] Starting Python FastAPI AI Microservice on Port 8000...
if exist "%PROJECT_ROOT%\model\.venv\Scripts\activate.bat" (
    start "MiniDrive - AI Microservice (:8000)" cmd /k "cd /d %PROJECT_ROOT%\model && call .venv\Scripts\activate.bat && python -m uvicorn app:app --host 0.0.0.0 --port 8000"
) else (
    echo ⚠️  Virtual environment not found at model\.venv. Starting directly with python...
    start "MiniDrive - AI Microservice (:8000)" cmd /k "cd /d %PROJECT_ROOT%\model && python -m uvicorn app:app --host 0.0.0.0 --port 8000"
)

REM 4. Launch Spring Boot Backend
echo ☕ [2/2] Starting Spring Boot Backend on Port 8080...
if exist "%PROJECT_ROOT%\Cloud_Backend_Workspace\backend\target\backend-0.0.1-SNAPSHOT.jar" (
    start "MiniDrive - Spring Boot Backend (:8080)" cmd /k "cd /d %PROJECT_ROOT% && java -jar Cloud_Backend_Workspace\backend\target\backend-0.0.1-SNAPSHOT.jar"
) else (
    echo 📦 JAR not found. Launching via Maven wrapper...
    start "MiniDrive - Spring Boot Backend (:8080)" cmd /k "cd /d %PROJECT_ROOT%\Cloud_Backend_Workspace\backend && gradlew.bat spring-boot:run || mvnw.cmd spring-boot:run"
)

echo.
echo ==================================================================
echo ✅ Backend services are running in separate windows!
echo 🌐 On your Linux laptop, run:
echo    ./scripts/run_frontend.fish ^<YOUR_WINDOWS_IP^>
echo ==================================================================
echo.
pause
