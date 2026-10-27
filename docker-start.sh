#!/usr/bin/env bash
# ==============================================================================
# Mini Cloud Drive - 1-Command Docker Deployment Script
# ==============================================================================

set -e

# Terminal colors
GREEN="\033[0;32m"
CYAN="\033[0;36m"
YELLOW="\033[1;33m"
RED="\033[0;31m"
BOLD="\033[1m"
NC="\033[0m"

echo -e "${CYAN}${BOLD}==================================================================${NC}"
echo -e "${CYAN}${BOLD}🐳 Mini Cloud Drive - Automated Docker Deployment${NC}"
echo -e "${CYAN}${BOLD}==================================================================${NC}"

# 1. Prerequisite verification
echo -e "\n${YELLOW}🔍 Checking Docker environment...${NC}"
if ! command -v docker >/dev/null 2>&1; then
    echo -e "${RED}❌ Error: 'docker' command is not installed or not in PATH.${NC}"
    echo "Please install Docker from https://docs.docker.com/get-docker/ and try again."
    exit 1
fi

# Detect docker compose syntax (modern plugin vs legacy standalone)
DOCKER_COMPOSE_CMD=""
if docker compose version >/dev/null 2>&1; then
    DOCKER_COMPOSE_CMD="docker compose"
elif command -v docker-compose >/dev/null 2>&1; then
    DOCKER_COMPOSE_CMD="docker-compose"
else
    echo -e "${RED}❌ Error: Neither 'docker compose' nor 'docker-compose' was found.${NC}"
    exit 1
fi
echo -e "   ${GREEN}✅ Docker engine and Compose detected (${DOCKER_COMPOSE_CMD})${NC}"

# 2. Prepare .env configuration
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

if [ ! -f .env ]; then
    echo -e "\n${YELLOW}⚙️  Creating default .env from .env.example...${NC}"
    cp .env.example .env
    echo -e "   ${GREEN}✅ Created .env with default settings.${NC}"
fi

# 3. Build & start containers
echo -e "\n${YELLOW}🚀 Building and starting containers in detached mode...${NC}"
$DOCKER_COMPOSE_CMD up --build -d

# 4. Wait for healthy status
echo -e "\n${YELLOW}⏳ Waiting for services to become healthy...${NC}"
MAX_WAIT=90
ELAPSED=0

while [ $ELAPSED -lt $MAX_WAIT ]; do
    DB_STATUS=$($DOCKER_COMPOSE_CMD ps --format json 2>/dev/null | grep -i "minidrive-database" || true)
    AI_STATUS=$($DOCKER_COMPOSE_CMD ps --format json 2>/dev/null | grep -i "minidrive-ai-service" || true)
    BACKEND_STATUS=$($DOCKER_COMPOSE_CMD ps --format json 2>/dev/null | grep -i "minidrive-backend" || true)

    # Check healthiness via standard docker inspect
    DB_HEALTH=$(docker inspect --format='{{json .State.Health.Status}}' minidrive-database 2>/dev/null || echo "\"starting\"")
    AI_HEALTH=$(docker inspect --format='{{json .State.Health.Status}}' minidrive-ai-service 2>/dev/null || echo "\"starting\"")
    BE_HEALTH=$(docker inspect --format='{{json .State.Health.Status}}' minidrive-backend 2>/dev/null || echo "\"starting\"")

    if [ "$DB_HEALTH" = "\"healthy\"" ] && [ "$AI_HEALTH" = "\"healthy\"" ] && [ "$BE_HEALTH" = "\"healthy\"" ]; then
        echo -e "\n${GREEN}${BOLD}🎉 All MiniDrive services are healthy and running!${NC}"
        break
    fi

    echo -ne "   [${ELAPSED}s] Database: ${DB_HEALTH//\"/}, AI Service: ${AI_HEALTH//\"/}, Backend: ${BE_HEALTH//\"/}\r"
    sleep 3
    ELAPSED=$((ELAPSED + 3))
done

echo ""
echo -e "${CYAN}${BOLD}==================================================================${NC}"
echo -e "${GREEN}${BOLD}✨ System Deployment Summary:${NC}"
echo -e "   🗄️  ${BOLD}Database (MariaDB):${NC}       localhost:3306 (database: cloud_db)"
echo -e "   🤖 ${BOLD}AI Microservice:${NC}         http://localhost:8000"
echo -e "      ${CYAN}• Swagger / Interactive Docs: http://localhost:8000/docs${NC}"
echo -e "      ${CYAN}• Health Check:               http://localhost:8000/health${NC}"
echo -e "   ☕ ${BOLD}Spring Boot REST API:${NC}    http://localhost:8080"
echo -e "      ${CYAN}• AI Connection Status:       http://localhost:8080/api/ai/status${NC}"
echo -e "${CYAN}${BOLD}==================================================================${NC}"
echo -e "${YELLOW}${BOLD}🖥️  To Launch the Desktop Client:${NC}"
echo -e "   cd Cloud_Frontend_Workspace/frontend"
echo -e "   ./mvnw javafx:run"
echo -e "${CYAN}${BOLD}==================================================================${NC}"
echo -e "📜 View live container logs:  ${BOLD}$DOCKER_COMPOSE_CMD logs -f${NC}"
echo -e "🛑 Stop all containers:       ${BOLD}./docker-stop.sh${NC}"
echo ""
