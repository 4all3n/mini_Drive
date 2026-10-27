# ==============================================================================
# Python FastAPI AI Categorization Microservice Dockerfile
# ==============================================================================
FROM python:3.11-slim

WORKDIR /app

# Install system build dependencies and cleanup in single layer
RUN apt-get update && apt-get install -y --no-install-recommends \
    gcc \
    curl \
    && rm -rf /var/lib/apt/lists/*

# Install Python dependencies first for optimal Docker layer caching
COPY requirements.txt .
RUN pip install --no-cache-dir --upgrade pip && \
    pip install --no-cache-dir -r requirements.txt

# Copy application source code and pre-trained models
COPY . .

# Ensure models are pre-trained if not already present in the build context
RUN python -c "import os; from train_and_evaluate import train_models; train_models() if not os.path.exists('saved_models/svm_model.joblib') else None"

EXPOSE 8000

# Run FastAPI via Uvicorn on 0.0.0.0:8000
CMD ["uvicorn", "app:app", "--host", "0.0.0.0", "--port", "8000"]
