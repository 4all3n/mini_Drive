import os
import json
from fastapi import FastAPI, Query
from pydantic import BaseModel
import uvicorn
import joblib

app = FastAPI(
    title="Intelligent Cloud Drive — AI Classification Microservice",
    description="High-performance NLP service for ultra-short text (filename) categorization.",
    version="2.0.0"
)

# Directories
BASE_DIR = os.path.dirname(os.path.abspath(__file__))
MODELS_DIR = os.path.join(BASE_DIR, "saved_models")
RESULTS_DIR = os.path.join(BASE_DIR, "results")

# Model paths
SVM_PATH = os.path.join(MODELS_DIR, "svm_model.joblib")
NB_PATH = os.path.join(MODELS_DIR, "naive_bayes_model.joblib")
RESULTS_PATH = os.path.join(RESULTS_DIR, "evaluation_results.json")

# In-memory models
svm_model = None
nb_model = None
active_model = None
active_model_name = "None"


def load_models():
    """Load serialized models from disk on startup."""
    global svm_model, nb_model, active_model, active_model_name

    if os.path.exists(SVM_PATH):
        try:
            svm_model = joblib.load(SVM_PATH)
            print(f"[AI Service] Successfully loaded Linear SVM from {SVM_PATH}")
        except Exception as e:
            print(f"[AI Service] Warning: Failed to load Linear SVM: {e}")

    if os.path.exists(NB_PATH):
        try:
            nb_model = joblib.load(NB_PATH)
            print(f"[AI Service] Successfully loaded Naive Bayes from {NB_PATH}")
        except Exception as e:
            print(f"[AI Service] Warning: Failed to load Naive Bayes: {e}")

    # Set default active model (Linear SVM preferred, fallback to NB)
    if svm_model is not None:
        active_model = svm_model
        active_model_name = "Linear SVM"
    elif nb_model is not None:
        active_model = nb_model
        active_model_name = "Multinomial Naive Bayes"
    else:
        print("[AI Service] WARNING: No pre-trained model found in saved_models/. Running emergency training...")
        from dataset import generate_dataset
        from train_and_evaluate import build_svm_pipeline
        data = generate_dataset()
        X = [item[0] for item in data]
        y = [item[1] for item in data]
        active_model = build_svm_pipeline()
        active_model.fit(X, y)
        active_model_name = "Emergency Trained Linear SVM"
        print("[AI Service] Emergency model trained and loaded.")


def notify_backend_online():
    """Notify the Spring Boot backend that the AI service is online to trigger recategorization."""
    import time
    import urllib.request

    backend_url = os.environ.get("BACKEND_AI_NOTIFY_URL", "http://127.0.0.1:8080/api/ai/online-notify")
    # Retry up to 15 times with 2-second delay to wait for Spring Boot backend bootup
    for attempt in range(1, 16):
        time.sleep(2.0)
        try:
            req = urllib.request.Request(backend_url, method="POST", data=b"{}")
            req.add_header("Content-Type", "application/json")
            with urllib.request.urlopen(req, timeout=3) as resp:
                resp_body = resp.read().decode("utf-8")
                print(f"[AI Service] Successfully notified backend at {backend_url} on attempt {attempt} -> {resp_body}")
                return
        except Exception:
            # Backend not ready yet; continue waiting
            pass
    print("[AI Service] Backend notification deferred. Backend auto-scheduler will synchronize pending files.")


@app.on_event("startup")
def startup_event():
    load_models()
    import threading
    threading.Thread(target=notify_backend_online, daemon=True).start()


class FileData(BaseModel):
    filename: str


class BatchFileData(BaseModel):
    filenames: list[str]


class DetailedPrediction(BaseModel):
    category: str
    confidence: float
    model_used: str
    filename: str


@app.post("/predict")
def predict_category(data: FileData, detailed: bool = False):
    """
    Standard prediction endpoint.
    Default returns {"category": "..."} for 100% compatibility with Spring Boot backend string splitting.
    If detailed=True, returns category, confidence, and model metadata.
    """
    global active_model, active_model_name
    if active_model is None:
        load_models()

    filename = data.filename.strip()
    prediction = str(active_model.predict([filename])[0])

    confidence = 1.0
    if hasattr(active_model, "predict_proba"):
        try:
            probs = active_model.predict_proba([filename])[0]
            confidence = float(max(probs))
        except Exception:
            confidence = 1.0

    print(f"[AI Prediction] '{filename}' --> [{prediction}] (conf: {confidence:.2f}, model: {active_model_name})")

    if detailed:
        return {
            "category": prediction,
            "confidence": round(confidence, 4),
            "model_used": active_model_name,
            "filename": filename
        }

    # Default output for backward compatibility with Spring Boot
    return {"category": prediction}


@app.post("/predict/detailed", response_model=DetailedPrediction)
def predict_category_detailed(data: FileData):
    """Explicit endpoint returning category, probability confidence, and model provenance."""
    res = predict_category(data, detailed=True)
    return res


@app.post("/predict/batch")
def predict_batch(data: BatchFileData):
    """
    Batch prediction endpoint for classifying multiple filenames simultaneously.
    Useful for bulk recategorization and fast ingestion.
    """
    global active_model, active_model_name
    if active_model is None:
        load_models()

    if not data.filenames:
        return {"predictions": []}

    clean_names = [fn.strip() for fn in data.filenames]
    predictions = active_model.predict(clean_names)

    results = []
    for fn, pred in zip(clean_names, predictions):
        results.append({
            "filename": fn,
            "category": str(pred)
        })

    print(f"[AI Batch Prediction] Classified {len(results)} files via {active_model_name}")
    return {"predictions": results, "model_used": active_model_name}


@app.get("/evaluate")
def get_evaluation_metrics():
    """Returns the full academic comparative benchmark metrics between Naive Bayes and Linear SVM."""
    if os.path.exists(RESULTS_PATH):
        try:
            with open(RESULTS_PATH, "r") as f:
                return json.load(f)
        except Exception as e:
            return {"error": f"Failed to read evaluation results: {str(e)}"}
    return {
        "status": "pending",
        "message": "Evaluation results not yet generated. Run train_and_evaluate.py to generate benchmarks."
    }


@app.get("/health")
def health_check():
    """Health check endpoint providing runtime model status."""
    return {
        "status": "AI Microservice is running and ready.",
        "active_model": active_model_name,
        "models_loaded": {
            "linear_svm": svm_model is not None,
            "naive_bayes": nb_model is not None
        }
    }


if __name__ == "__main__":
    print("Starting Intelligent Cloud Drive AI Service on port 8000...")
    uvicorn.run(app, host="127.0.0.1", port=8000)