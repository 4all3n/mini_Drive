#!/usr/bin/env fish
# ==============================================================================
# MiniDrive - Spring Boot Backend Runner (Fish Shell)
# Starts Spring Boot Backend on port 8080
# ==============================================================================

set SCRIPT_PATH (realpath (status -f))
set SCRIPT_DIR (dirname $SCRIPT_PATH)
if test (basename $SCRIPT_DIR) = "scripts"
    set PROJECT_ROOT (cd $SCRIPT_DIR/..; and pwd)
else
    set PROJECT_ROOT $SCRIPT_DIR
end

echo "☕ [MiniDrive] Checking MariaDB status..."
if not systemctl is-active mariadb >/dev/null 2>&1
    echo "⚠️  MariaDB is not active! Starting MariaDB service..."
    sudo systemctl start mariadb
end

echo "☕ [MiniDrive] Starting Spring Boot Backend on port 8080..."
cd $PROJECT_ROOT

# Ensure cloud_storage folder exists in working directory
mkdir -p $PROJECT_ROOT/cloud_storage

# Prefer precompiled executable jar for fast startup; fallback to mvnw spring-boot:run
if test -f $PROJECT_ROOT/Cloud_Backend_Workspace/backend/target/backend-0.0.1-SNAPSHOT.jar
    echo "⚡ Launching from packaged JAR..."
    java -jar $PROJECT_ROOT/Cloud_Backend_Workspace/backend/target/backend-0.0.1-SNAPSHOT.jar
else
    echo "📦 Packaging and launching via Maven wrapper..."
    cd $PROJECT_ROOT/Cloud_Backend_Workspace/backend
    ./mvnw spring-boot:run
end
