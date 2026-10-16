#!/usr/bin/env fish
# ==============================================================================
# MiniDrive - JavaFX Desktop Client Runner (Fish Shell)
# Starts JavaFX Desktop GUI on CachyOS
# Usage:
#   ./scripts/run_frontend.fish                (connects to localhost:8080)
#   ./scripts/run_frontend.fish 192.168.1.150  (connects to remote Windows server)
# ==============================================================================

set SCRIPT_PATH (realpath (status -f))
set SCRIPT_DIR (dirname $SCRIPT_PATH)
if test (basename $SCRIPT_DIR) = "scripts"
    set PROJECT_ROOT (cd $SCRIPT_DIR/..; and pwd)
else
    set PROJECT_ROOT $SCRIPT_DIR
end

set SERVER_HOST "localhost"
if test (count $argv) -ge 1
    set SERVER_HOST $argv[1]
end

echo "🖥️  [MiniDrive] Launching JavaFX Desktop Client..."
echo "🌐 Connecting to Backend Host: $SERVER_HOST:8080"
cd $PROJECT_ROOT/Cloud_Frontend_Workspace/frontend

./mvnw javafx:run -Doptions="-Dminicloud.server.host=$SERVER_HOST"
