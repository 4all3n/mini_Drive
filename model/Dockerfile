# Dockerfile for Python FastAPI AI Categorization Microservice
FROM python:3.11-slim

WORKDIR /app

# Install system dependencies
RUN apt-get update && apt-get install -y --no-install-recommends \
    gcc \
    && rm -rf /var/lib/apt/lists/*

# Install Python requirements
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

# Copy source and trained models
COPY . .

# Ensure models are trained if not already present
RUN python -c "import os; from train_and_evaluate import train_models; train_models() if not os.path.exists('saved_models/svm_model.joblib') else None"

EXPOSE 8000

CMD ["uvicorn", "app:app", "--host", "0.0.0.0", "--port", "8000"]
