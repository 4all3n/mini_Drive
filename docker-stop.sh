#!/usr/bin/env bash
# ==============================================================================
# Mini Cloud Drive - Docker Teardown Script
# ==============================================================================

set -e

RED="\033[0;31m"
GREEN="\033[0;32m"
YELLOW="\033[1;33m"
BOLD="\033[1m"
NC="\033[0m"

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

DOCKER_COMPOSE_CMD=""
if docker compose version >/dev/null 2>&1; then
    DOCKER_COMPOSE_CMD="docker compose"
elif command -v docker-compose >/dev/null 2>&1; then
    DOCKER_COMPOSE_CMD="docker-compose"
else
    echo -e "${RED}❌ Error: Neither 'docker compose' nor 'docker-compose' found.${NC}"
    exit 1
fi

if [[ "$1" == "--volumes" || "$1" == "-v" ]]; then
    echo -e "${YELLOW}🛑 Stopping MiniDrive and wiping persistent volumes (fresh reset)...${NC}"
    $DOCKER_COMPOSE_CMD down -v
    echo -e "${GREEN}✅ All containers and data volumes stopped and removed.${NC}"
else
    echo -e "${YELLOW}🛑 Stopping MiniDrive services (data preserved in volumes)...${NC}"
    $DOCKER_COMPOSE_CMD down
    echo -e "${GREEN}✅ All containers stopped.${NC}"
    echo -e "💡 Tip: Run '${BOLD}./docker-stop.sh -v${NC}' if you want to wipe database & file storage."
fi
