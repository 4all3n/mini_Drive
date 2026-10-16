import os
import json
import numpy as np
from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.naive_bayes import MultinomialNB
from sklearn.svm import LinearSVC
from sklearn.calibration import CalibratedClassifierCV
from sklearn.pipeline import make_pipeline
from sklearn.model_selection import (
    train_test_split,
    StratifiedKFold,
    cross_validate,
)
from sklearn.metrics import (
    accuracy_score,
    precision_score,
    recall_score,
    f1_score,
    classification_report,
    confusion_matrix,
)
import joblib

from dataset import generate_dataset, get_dataset_stats


def load_data():
    data = generate_dataset()
    filenames = [item[0] for item in data]
    labels = [item[1] for item in data]
    return filenames, labels


def build_nb_pipeline():
    """Multinomial Naive Bayes with character n-gram TF-IDF."""
    return make_pipeline(
        TfidfVectorizer(analyzer="char_wb", ngram_range=(2, 5)),
        MultinomialNB(alpha=1.0),
    )


def build_svm_pipeline():
    """Linear SVM with character n-gram TF-IDF.
    Wrapped in CalibratedClassifierCV to get probability estimates."""
    return make_pipeline(
        TfidfVectorizer(analyzer="char_wb", ngram_range=(2, 5)),
        CalibratedClassifierCV(LinearSVC(C=1.0, max_iter=10000), cv=3),
    )


def run_cross_validation(pipeline, X, y, model_name, cv=5):
    """Run stratified k-fold cross-validation."""
    skf = StratifiedKFold(n_splits=cv, shuffle=True, random_state=42)
    scoring = ["accuracy", "precision_macro", "recall_macro", "f1_macro"]

    print(f"\n{'─'*60}")
    print(f"  {model_name} — {cv}-Fold Stratified Cross-Validation")
    print(f"{'─'*60}")

    cv_results = cross_validate(
        pipeline, X, y,
        cv=skf,
        scoring=scoring,
        return_train_score=False,
    )

    results = {}
    for metric in scoring:
        key = f"test_{metric}"
        scores = cv_results[key]
        results[metric.replace("test_", "")] = {
            "mean": float(np.mean(scores)),
            "std": float(np.std(scores)),
            "per_fold": [float(s) for s in scores],
        }
        print(f"  {metric:20s}: {np.mean(scores):.4f} (± {np.std(scores):.4f})")

    return results


def run_train_test_eval(pipeline, X_train, X_test, y_train, y_test, model_name):
    """Train on train set, evaluate on held-out test set."""
    print(f"\n{'─'*60}")
    print(f"  {model_name} — 80/20 Train-Test Evaluation")
    print(f"{'─'*60}")

    pipeline.fit(X_train, y_train)
    y_pred = pipeline.predict(X_test)

    acc = accuracy_score(y_test, y_pred)
    prec = precision_score(y_test, y_pred, average="macro", zero_division=0)
    rec = recall_score(y_test, y_pred, average="macro", zero_division=0)
    f1 = f1_score(y_test, y_pred, average="macro", zero_division=0)
    cm = confusion_matrix(y_test, y_pred)
    report = classification_report(y_test, y_pred, zero_division=0)

    print(f"  Accuracy:  {acc:.4f}")
    print(f"  Precision: {prec:.4f} (macro)")
    print(f"  Recall:    {rec:.4f} (macro)")
    print(f"  F1-Score:  {f1:.4f} (macro)")
    print(f"\n  Classification Report:\n{report}")

    # Per-class metrics
    per_class_report = classification_report(
        y_test, y_pred, output_dict=True, zero_division=0
    )

    return {
        "accuracy": float(acc),
        "precision_macro": float(prec),
        "recall_macro": float(rec),
        "f1_macro": float(f1),
        "confusion_matrix": cm.tolist(),
        "per_class": {
            k: v for k, v in per_class_report.items()
            if k not in ("accuracy", "macro avg", "weighted avg")
        },
        "labels": sorted(set(y_test)),
    }


def main():
    print(f"\n{'='*60}")
    print(f"  INTELLIGENT CLOUD DRIVE — ML TRAINING PIPELINE")
    print(f"{'='*60}")

    # Load data
    X, y = load_data()
    stats = get_dataset_stats(list(zip(X, y)))
    print(f"\n  Dataset: {len(X)} samples across {len(stats)} categories")
    for cat, count in stats.items():
        print(f"    {cat:25s} → {count}")

    # Train-test split
    X_train, X_test, y_train, y_test = train_test_split(
        X, y, test_size=0.2, random_state=42, stratify=y
    )
    print(f"\n  Train set: {len(X_train)} | Test set: {len(X_test)}")

    # Build pipelines
    nb_pipeline = build_nb_pipeline()
    svm_pipeline = build_svm_pipeline()

    # Cross-validation (on full dataset)
    nb_cv = run_cross_validation(nb_pipeline, X, y, "Multinomial Naive Bayes")
    svm_cv = run_cross_validation(svm_pipeline, X, y, "Linear SVM")

    # Train-test evaluation
    nb_results = run_train_test_eval(
        build_nb_pipeline(), X_train, X_test, y_train, y_test,
        "Multinomial Naive Bayes"
    )
    svm_results = run_train_test_eval(
        build_svm_pipeline(), X_train, X_test, y_train, y_test,
        "Linear SVM"
    )

    models_dir = os.path.join(os.path.dirname(__file__), "saved_models")
    os.makedirs(models_dir, exist_ok=True)

    # Retrain on FULL dataset for production use
    print(f"\n{'─'*60}")
    print(f"  Retraining on FULL dataset for production deployment...")
    print(f"{'─'*60}")

    nb_full = build_nb_pipeline()
    nb_full.fit(X, y)
    joblib.dump(nb_full, os.path.join(models_dir, "naive_bayes_model.joblib"))
    print(f"   Saved: saved_models/naive_bayes_model.joblib")

    svm_full = build_svm_pipeline()
    svm_full.fit(X, y)
    joblib.dump(svm_full, os.path.join(models_dir, "svm_model.joblib"))
    print(f"   Saved: saved_models/svm_model.joblib")

    results_dir = os.path.join(os.path.dirname(__file__), "results")
    os.makedirs(results_dir, exist_ok=True)

    all_results = {
        "dataset": {
            "total_samples": len(X),
            "train_samples": len(X_train),
            "test_samples": len(X_test),
            "categories": stats,
        },
        "naive_bayes": {
            "cross_validation": nb_cv,
            "train_test": nb_results,
        },
        "linear_svm": {
            "cross_validation": svm_cv,
            "train_test": svm_results,
        },
    }

    results_path = os.path.join(results_dir, "evaluation_results.json")
    with open(results_path, "w") as f:
        json.dump(all_results, f, indent=2)
    print(f"   Saved: results/evaluation_results.json")

    print(f"\n{'='*60}")
    print(f"  FINAL COMPARISON: Naive Bayes vs Linear SVM")
    print(f"{'='*60}")
    print(f"  {'Metric':<20} {'Naive Bayes':>15} {'Linear SVM':>15}")
    print(f"  {'─'*50}")
    print(f"  {'Accuracy':<20} {nb_results['accuracy']:>15.4f} {svm_results['accuracy']:>15.4f}")
    print(f"  {'Precision (macro)':<20} {nb_results['precision_macro']:>15.4f} {svm_results['precision_macro']:>15.4f}")
    print(f"  {'Recall (macro)':<20} {nb_results['recall_macro']:>15.4f} {svm_results['recall_macro']:>15.4f}")
    print(f"  {'F1-Score (macro)':<20} {nb_results['f1_macro']:>15.4f} {svm_results['f1_macro']:>15.4f}")
    print(f"  {'─'*50}")

    nb_cv_f1 = nb_cv["f1_macro"]["mean"]
    svm_cv_f1 = svm_cv["f1_macro"]["mean"]
    print(f"  {'CV F1 (5-fold)':<20} {nb_cv_f1:>15.4f} {svm_cv_f1:>15.4f}")
    print(f"{'='*60}")

    winner = "Linear SVM" if svm_results["f1_macro"] > nb_results["f1_macro"] else "Multinomial Naive Bayes"
    print(f"\n Winner (by F1-Score): {winner}")
    print(f"\n  All results saved to: model/results/")
    print(f"  All models saved to:  model/saved_models/\n")


if __name__ == "__main__":
    main()
