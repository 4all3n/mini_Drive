#!/usr/bin/env fish
# ==============================================================================
# MiniDrive - AI Microservice Runner (Fish Shell)
# Starts FastAPI AI Categorization Service on port 8000
# ==============================================================================

set SCRIPT_PATH (realpath (status -f))
set SCRIPT_DIR (dirname $SCRIPT_PATH)
if test (basename $SCRIPT_DIR) = "scripts"
    set PROJECT_ROOT (cd $SCRIPT_DIR/..; and pwd)
else
    set PROJECT_ROOT $SCRIPT_DIR
end

echo "🤖 [MiniDrive] Starting AI Microservice..."
cd $PROJECT_ROOT/model

if not test -d .venv
    echo "❌ Python virtual environment not found in $PROJECT_ROOT/model/.venv"
    exit 1
end

source .venv/bin/activate.fish
echo "🚀 AI Service running on http://127.0.0.1:8000"
echo "   Endpoints: /health, /predict, /predict/detailed, /evaluate"
python -m uvicorn app:app --host 127.0.0.1 --port 8000
