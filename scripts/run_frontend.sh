#!/usr/bin/env bash
# ==============================================================================
# MiniDrive - JavaFX Desktop Client Runner (Bash)
# Starts JavaFX Desktop GUI
# Usage:
#   ./scripts/run_frontend.sh                (connects to localhost:8080)
#   ./scripts/run_frontend.sh 192.168.1.150  (connects to remote Windows server)
# ==============================================================================

set -e
SCRIPT_PATH="$(realpath "${BASH_SOURCE[0]}")"
SCRIPT_DIR="$(dirname "$SCRIPT_PATH")"
if [ "$(basename "$SCRIPT_DIR")" = "scripts" ]; then
    PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
else
    PROJECT_ROOT="$SCRIPT_DIR"
fi

SERVER_HOST="${1:-localhost}"

echo "🖥️  [MiniDrive] Launching JavaFX Desktop Client..."
echo "🌐 Connecting to Backend Host: $SERVER_HOST:8080"
cd "$PROJECT_ROOT/Cloud_Frontend_Workspace/frontend"

./mvnw javafx:run -Doptions="-Dminicloud.server.host=$SERVER_HOST"
