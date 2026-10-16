#!/usr/bin/env bash
# ==============================================================================
# MiniDrive - Master One-Command Orchestrator (Bash/POSIX)
# Starts AI Microservice (:8000), Backend (:8080), and JavaFX Frontend
# Cleanly terminates background services on exit.
# ==============================================================================

set -e
SCRIPT_PATH="$(realpath "${BASH_SOURCE[0]}")"
SCRIPT_DIR="$(dirname "$SCRIPT_PATH")"
if [ "$(basename "$SCRIPT_DIR")" = "scripts" ]; then
    PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
else
    PROJECT_ROOT="$SCRIPT_DIR"
fi

echo "=================================================================="
echo "🚀 MiniDrive - Launching Full Intelligent Cloud Drive System"
echo "📂 Project Root: $PROJECT_ROOT"
echo "=================================================================="

# 1. Verify MariaDB
echo "🔍 [1/4] Checking Database Service..."
if ! systemctl is-active mariadb >/dev/null 2>&1; then
    echo "⚠️  MariaDB is inactive. Attempting to start..."
    sudo systemctl start mariadb || { echo "❌ Failed to start MariaDB."; exit 1; }
fi
echo "   ✅ MariaDB is active."

# 2. Launch AI Microservice
echo "🤖 [2/4] Starting AI Microservice (FastAPI on :8000)..."
cd "$PROJECT_ROOT/model"
source .venv/bin/activate
python -m uvicorn app:app --host 127.0.0.1 --port 8000 > "$PROJECT_ROOT/model/ai_service.log" 2>&1 &
AI_PID=$!
echo "   ✅ AI Service spawned with PID $AI_PID (logs: model/ai_service.log)"

# Wait for AI service health
echo -n "   ⏳ Waiting for AI health check..."
for i in {1..15}; do
    if curl -s http://127.0.0.1:8000/health >/dev/null 2>&1; then
        echo " Online!"
        break
    fi
    sleep 1
    echo -n "."
done

# 3. Launch Spring Boot Backend
echo "☕ [3/4] Starting Spring Boot Backend (Tomcat on :8080)..."
cd "$PROJECT_ROOT"
mkdir -p "$PROJECT_ROOT/cloud_storage"
java -jar "$PROJECT_ROOT/Cloud_Backend_Workspace/backend/target/backend-0.0.1-SNAPSHOT.jar" > "$PROJECT_ROOT/Cloud_Backend_Workspace/backend.log" 2>&1 &
BACKEND_PID=$!
echo "   ✅ Backend spawned with PID $BACKEND_PID (logs: Cloud_Backend_Workspace/backend.log)"

# Wait for Backend health
echo -n "   ⏳ Waiting for Spring Boot port 8080..."
for i in {1..20}; do
    if curl -s http://localhost:8080/admin >/dev/null 2>&1; then
        echo " Online!"
        break
    fi
    sleep 1
    echo -n "."
done

# Cleanup trap
cleanup() {
    echo ""
    echo "🛑 Shutting down MiniDrive services..."
    [ -n "$AI_PID" ] && kill -9 "$AI_PID" 2>/dev/null || true
    [ -n "$BACKEND_PID" ] && kill -9 "$BACKEND_PID" 2>/dev/null || true
    echo "👋 MiniDrive stopped cleanly."
    exit 0
}
trap cleanup SIGINT SIGTERM EXIT

# 4. Launch JavaFX Frontend in foreground
echo "🖥️  [4/4] Starting JavaFX Desktop Client..."
cd "$PROJECT_ROOT/Cloud_Frontend_Workspace/frontend"
./mvnw javafx:run
