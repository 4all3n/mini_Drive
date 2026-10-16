import os
import json
import numpy as np
import matplotlib
matplotlib.use("Agg")
import matplotlib.pyplot as plt
import seaborn as sns

# Style
plt.rcParams.update({
    "figure.facecolor": "white",
    "axes.facecolor": "white",
    "font.size": 12,
    "axes.titlesize": 14,
    "axes.labelsize": 12,
})
sns.set_theme(style="whitegrid")

RESULTS_DIR = os.path.join(os.path.dirname(__file__), "results")
COLORS = {"Naive Bayes": "#3b82f6", "Linear SVM": "#10b981"}


def load_results():
    path = os.path.join(RESULTS_DIR, "evaluation_results.json")
    with open(path, "r") as f:
        return json.load(f)


def plot_accuracy_comparison(results):
    nb_acc = results["naive_bayes"]["train_test"]["accuracy"]
    svm_acc = results["linear_svm"]["train_test"]["accuracy"]

    fig, ax = plt.subplots(figsize=(8, 5))
    models = ["Naive Bayes", "Linear SVM"]
    accuracies = [nb_acc, svm_acc]
    colors = [COLORS["Naive Bayes"], COLORS["Linear SVM"]]

    bars = ax.bar(models, accuracies, color=colors, width=0.5, edgecolor="black", linewidth=0.5)
    for bar, acc in zip(bars, accuracies):
        ax.text(bar.get_x() + bar.get_width() / 2, bar.get_height() + 0.005,
                f"{acc:.2%}", ha="center", va="bottom", fontweight="bold", fontsize=13)

    ax.set_ylabel("Accuracy")
    ax.set_title("Model Accuracy Comparison: Naive Bayes vs Linear SVM")
    ax.set_ylim(0, 1.12)
    ax.yaxis.set_major_formatter(plt.FuncFormatter(lambda x, _: f"{x:.0%}"))
    plt.tight_layout()
    plt.savefig(os.path.join(RESULTS_DIR, "accuracy_comparison.png"), dpi=200)
    plt.close()
    print("accuracy_comparison.png")


def plot_f1_per_class(results):
    nb_classes = results["naive_bayes"]["train_test"]["per_class"]
    svm_classes = results["linear_svm"]["train_test"]["per_class"]
    categories = sorted(nb_classes.keys())

    nb_f1 = [nb_classes[c]["f1-score"] for c in categories]
    svm_f1 = [svm_classes[c]["f1-score"] for c in categories]

    x = np.arange(len(categories))
    width = 0.35

    fig, ax = plt.subplots(figsize=(12, 6))
    bars1 = ax.bar(x - width / 2, nb_f1, width, label="Naive Bayes",
                   color=COLORS["Naive Bayes"], edgecolor="black", linewidth=0.5)
    bars2 = ax.bar(x + width / 2, svm_f1, width, label="Linear SVM",
                   color=COLORS["Linear SVM"], edgecolor="black", linewidth=0.5)

    for bar, val in zip(bars1, nb_f1):
        ax.text(bar.get_x() + bar.get_width() / 2, bar.get_height() + 0.01,
                f"{val:.2f}", ha="center", va="bottom", fontsize=9)
    for bar, val in zip(bars2, svm_f1):
        ax.text(bar.get_x() + bar.get_width() / 2, bar.get_height() + 0.01,
                f"{val:.2f}", ha="center", va="bottom", fontsize=9)

    ax.set_ylabel("F1-Score")
    ax.set_title("Per-Category F1-Score: Naive Bayes vs Linear SVM")
    ax.set_xticks(x)
    ax.set_xticklabels(categories, rotation=15, ha="right")
    ax.set_ylim(0, 1.15)
    ax.legend()
    plt.tight_layout()
    plt.savefig(os.path.join(RESULTS_DIR, "f1_per_class.png"), dpi=200)
    plt.close()
    print("f1_per_class.png")


def plot_confusion_matrix(results, model_key, model_name):
    cm = np.array(results[model_key]["train_test"]["confusion_matrix"])
    labels = results[model_key]["train_test"]["labels"]

    fig, ax = plt.subplots(figsize=(9, 7))
    sns.heatmap(cm, annot=True, fmt="d", cmap="Blues", xticklabels=labels,
                yticklabels=labels, ax=ax, linewidths=0.5, linecolor="gray")
    ax.set_xlabel("Predicted Label")
    ax.set_ylabel("True Label")
    ax.set_title(f"Confusion Matrix — {model_name}")
    plt.xticks(rotation=20, ha="right")
    plt.yticks(rotation=0)
    plt.tight_layout()

    fname = f"confusion_matrix_{'nb' if 'naive' in model_key else 'svm'}.png"
    plt.savefig(os.path.join(RESULTS_DIR, fname), dpi=200)
    plt.close()
    print(f"{fname}")


def plot_dataset_distribution(results):
    cats = results["dataset"]["categories"]
    categories = list(cats.keys())
    counts = list(cats.values())

    fig, ax = plt.subplots(figsize=(10, 5))
    colors = sns.color_palette("Set2", len(categories))
    bars = ax.bar(categories, counts, color=colors, edgecolor="black", linewidth=0.5)

    for bar, count in zip(bars, counts):
        ax.text(bar.get_x() + bar.get_width() / 2, bar.get_height() + 1,
                str(count), ha="center", va="bottom", fontweight="bold")

    ax.set_ylabel("Number of Samples")
    ax.set_title(f"Dataset Distribution (Total: {results['dataset']['total_samples']} samples)")
    ax.set_xlabel("Category")
    plt.xticks(rotation=15, ha="right")
    plt.tight_layout()
    plt.savefig(os.path.join(RESULTS_DIR, "dataset_distribution.png"), dpi=200)
    plt.close()
    print("dataset_distribution.png")

def plot_metrics_summary(results):
    nb = results["naive_bayes"]["train_test"]
    svm = results["linear_svm"]["train_test"]
    nb_cv = results["naive_bayes"]["cross_validation"]
    svm_cv = results["linear_svm"]["cross_validation"]

    fig, ax = plt.subplots(figsize=(10, 4))
    ax.axis("off")

    col_labels = ["Metric", "Naive Bayes", "Linear SVM"]
    row_data = [
        ["Accuracy (Test)", f"{nb['accuracy']:.4f}", f"{svm['accuracy']:.4f}"],
        ["Precision (Macro)", f"{nb['precision_macro']:.4f}", f"{svm['precision_macro']:.4f}"],
        ["Recall (Macro)", f"{nb['recall_macro']:.4f}", f"{svm['recall_macro']:.4f}"],
        ["F1-Score (Macro)", f"{nb['f1_macro']:.4f}", f"{svm['f1_macro']:.4f}"],
        ["CV Accuracy (5-fold)", f"{nb_cv['accuracy']['mean']:.4f} ± {nb_cv['accuracy']['std']:.4f}",
         f"{svm_cv['accuracy']['mean']:.4f} ± {svm_cv['accuracy']['std']:.4f}"],
        ["CV F1 (5-fold)", f"{nb_cv['f1_macro']['mean']:.4f} ± {nb_cv['f1_macro']['std']:.4f}",
         f"{svm_cv['f1_macro']['mean']:.4f} ± {svm_cv['f1_macro']['std']:.4f}"],
    ]

    table = ax.table(
        cellText=row_data, colLabels=col_labels, loc="center",
        cellLoc="center", colColours=["#e2e8f0", "#dbeafe", "#d1fae5"],
    )
    table.auto_set_font_size(False)
    table.set_fontsize(12)
    table.scale(1.2, 1.8)

    # Bold header
    for key, cell in table.get_celld().items():
        if key[0] == 0:
            cell.set_text_props(fontweight="bold")

    ax.set_title("Comparative Evaluation Summary: Naive Bayes vs Linear SVM",
                 fontsize=14, fontweight="bold", pad=20)
    plt.tight_layout()
    plt.savefig(os.path.join(RESULTS_DIR, "metrics_summary.png"), dpi=200)
    plt.close()
    print("metrics_summary.png")

def plot_cv_boxplot(results):
    nb_folds = results["naive_bayes"]["cross_validation"]["f1_macro"]["per_fold"]
    svm_folds = results["linear_svm"]["cross_validation"]["f1_macro"]["per_fold"]

    fig, ax = plt.subplots(figsize=(8, 5))
    try:
        bp = ax.boxplot(
            [nb_folds, svm_folds],
            tick_labels=["Naive Bayes", "Linear SVM"],
            patch_artist=True,
            widths=0.4,
        )
    except TypeError:
        bp = ax.boxplot(
            [nb_folds, svm_folds],
            labels=["Naive Bayes", "Linear SVM"],
            patch_artist=True,
            widths=0.4,
        )

    bp["boxes"][0].set_facecolor(COLORS["Naive Bayes"])
    bp["boxes"][1].set_facecolor(COLORS["Linear SVM"])
    for box in bp["boxes"]:
        box.set_alpha(0.7)

    ax.set_ylabel("F1-Score (Macro)")
    ax.set_title("5-Fold Cross-Validation F1-Score Distribution")
    ax.set_ylim(0, 1.1)
    plt.tight_layout()
    plt.savefig(os.path.join(RESULTS_DIR, "cross_validation_boxplot.png"), dpi=200)
    plt.close()
    print("cross_validation_boxplot.png")


def main():
    print(f"\n{'='*60}")
    print(f"  GENERATING EVALUATION CHARTS")
    print(f"{'='*60}")

    results = load_results()

    plot_accuracy_comparison(results)
    plot_f1_per_class(results)
    plot_confusion_matrix(results, "naive_bayes", "Multinomial Naive Bayes")
    plot_confusion_matrix(results, "linear_svm", "Linear SVM (LinearSVC)")
    plot_dataset_distribution(results)
    plot_metrics_summary(results)
    plot_cv_boxplot(results)

    print(f"\n{'='*60}")
    print(f"  All 7 charts saved to: model/results/")
    print(f"{'='*60}\n")


if __name__ == "__main__":
    main()
