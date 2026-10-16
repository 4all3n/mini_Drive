#!/usr/bin/env fish
# ==============================================================================
# MiniDrive - Master One-Command Orchestrator (Fish Shell)
# Starts AI Microservice (port 8000), Backend (port 8080), and JavaFX Frontend
# Cleanly terminates background services on exit.
# ==============================================================================

# Resolve canonical script directory even if invoked through a symlink
set SCRIPT_PATH (realpath (status -f))
set SCRIPT_DIR (dirname $SCRIPT_PATH)
if test (basename $SCRIPT_DIR) = "scripts"
    set PROJECT_ROOT (cd $SCRIPT_DIR/..; and pwd)
else
    set PROJECT_ROOT $SCRIPT_DIR
end

echo "=================================================================="
echo "🚀 MiniDrive - Launching Full Intelligent Cloud Drive System"
echo "📂 Project Root: $PROJECT_ROOT"
echo "=================================================================="

# 1. Verify MariaDB
echo "🔍 [1/4] Checking Database Service..."
if not systemctl is-active mariadb >/dev/null 2>&1
    echo "⚠️  MariaDB is inactive. Attempting to start..."
    sudo systemctl start mariadb
    if not systemctl is-active mariadb >/dev/null 2>&1
        echo "❌ Failed to start MariaDB. Please start it manually: sudo systemctl start mariadb"
        exit 1
    end
end
echo "   ✅ MariaDB is active."

# 2. Launch AI Microservice
echo "🤖 [2/4] Starting AI Microservice (FastAPI on :8000)..."
cd $PROJECT_ROOT/model
if not test -d .venv
    echo "❌ Python virtual environment missing in $PROJECT_ROOT/model/.venv"
    exit 1
end

source .venv/bin/activate.fish
python -m uvicorn app:app --host 127.0.0.1 --port 8000 > $PROJECT_ROOT/model/ai_service.log 2>&1 &
set AI_PID $last_pid
echo "   ✅ AI Service spawned with PID $AI_PID (logs: model/ai_service.log)"

# Wait for AI service health
echo -n "   ⏳ Waiting for AI health check..."
set -l ai_ready 0
for i in (seq 1 15)
    if curl -s http://127.0.0.1:8000/health >/dev/null 2>&1
        set ai_ready 1
        break
    end
    sleep 1
    echo -n "."
end
if test $ai_ready -eq 1
    echo " Online!"
else
    echo " Warning: AI health check timed out, proceeding anyway."
end

# 3. Launch Spring Boot Backend
echo "☕ [3/4] Starting Spring Boot Backend (Tomcat on :8080)..."
cd $PROJECT_ROOT
mkdir -p $PROJECT_ROOT/cloud_storage
java -jar $PROJECT_ROOT/Cloud_Backend_Workspace/backend/target/backend-0.0.1-SNAPSHOT.jar > $PROJECT_ROOT/Cloud_Backend_Workspace/backend.log 2>&1 &
set BACKEND_PID $last_pid
echo "   ✅ Backend spawned with PID $BACKEND_PID (logs: Cloud_Backend_Workspace/backend.log)"

# Wait for Backend health
echo -n "   ⏳ Waiting for Spring Boot port 8080..."
set -l backend_ready 0
for i in (seq 1 20)
    if curl -s http://localhost:8080/admin >/dev/null 2>&1
        set backend_ready 1
        break
    end
    sleep 1
    echo -n "."
end
if test $backend_ready -eq 1
    echo " Online!"
else
    echo " Warning: Backend check timed out, proceeding anyway."
end

# 4. Function to clean up on exit
function cleanup --on-signal SIGINT --on-signal SIGTERM
    echo ""
    echo "🛑 Shutting down MiniDrive services..."
    if test -n "$AI_PID"
        echo "   Killing AI service (PID $AI_PID)..."
        kill -9 $AI_PID 2>/dev/null
    end
    if test -n "$BACKEND_PID"
        echo "   Killing Backend service (PID $BACKEND_PID)..."
        kill -9 $BACKEND_PID 2>/dev/null
    end
    echo "👋 MiniDrive stopped cleanly."
    exit 0
end

# 5. Launch JavaFX Frontend in foreground
echo "🖥️  [4/4] Starting JavaFX Desktop Client..."
cd $PROJECT_ROOT/Cloud_Frontend_Workspace/frontend
./mvnw javafx:run

# When frontend closes, clean up background services
cleanup
