package com.minicloud.client.ui.dialogs;

import com.minicloud.client.ui.IconFactory;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.shape.Rectangle;
import javafx.stage.Modality;
import javafx.stage.Window;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Storage Analytics Visualization Dialog.
 * Displays overall storage quota usage (500 MB limit) and a category-wise
 * distribution breakdown via an interactive JavaFX PieChart styled with
 * the Everforest dark palette.
 */
public class StorageAnalyticsDialog {

    private static final Map<String, String> CATEGORY_COLORS = new LinkedHashMap<>();

    static {
        CATEGORY_COLORS.put("Documents", "#83c092");     // Everforest Aqua
        CATEGORY_COLORS.put("Code", "#a7c080");          // Everforest Green
        CATEGORY_COLORS.put("Media", "#e67e80");         // Everforest Red/Coral
        CATEGORY_COLORS.put("Archives", "#e69875");      // Everforest Orange
        CATEGORY_COLORS.put("Executables", "#d699b6");   // Everforest Purple
        CATEGORY_COLORS.put("System & Logs", "#859289"); // Everforest Grey
        CATEGORY_COLORS.put("Uncategorized", "#dbbc7f"); // Everforest Yellow
    }

    public static class StorageStatsModel {
        public String username = "";
        public long totalFiles = 0;
        public long totalSizeBytes = 0;
        public double totalSizeMB = 0.0;
        public long quotaSizeBytes = 500L * 1024L * 1024L;
        public double quotaSizeMB = 500.0;
        public double quotaUsedPercent = 0.0;
        public final Map<String, Long> categorySizes = new LinkedHashMap<>();
        public final Map<String, Long> categoryCounts = new LinkedHashMap<>();
        public final Map<String, Double> categorySizeMB = new LinkedHashMap<>();
    }

    public static void show(Window owner, String statsJson) {
        StorageStatsModel model = parseStatsJson(statsJson);

        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Storage Analytics & Quota");
        if (owner != null) {
            dialog.initOwner(owner);
        }
        dialog.initModality(Modality.WINDOW_MODAL);

        try {
            dialog.getDialogPane().getStylesheets().add(
                    StorageAnalyticsDialog.class.getResource("/css/everforest.css").toExternalForm()
            );
        } catch (Exception ignored) {}

        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

        VBox mainContainer = new VBox(16);
        mainContainer.setPadding(new Insets(18));
        mainContainer.setPrefWidth(680);
        mainContainer.setPrefHeight(530);

        // 1. Header with Chart Icon and Title
        HBox headerBox = new HBox(12);
        headerBox.setAlignment(Pos.CENTER_LEFT);
        headerBox.getChildren().addAll(
                IconFactory.chartIcon(22),
                new VBox(2,
                        createLabel("Storage Analytics", "-fx-font-size: 17px; -fx-font-weight: bold; -fx-text-fill: #d3c6aa;"),
                        createLabel("Overview of storage consumption across AI-classified categories", "-fx-font-size: 11px; -fx-text-fill: #859289;")
                )
        );

        // 2. Storage Quota Overview Card
        VBox quotaCard = createQuotaCard(model);

        // 3. Middle Content: PieChart + Category Breakdown Grid
        HBox contentBox = new HBox(18);
        contentBox.setAlignment(Pos.TOP_LEFT);
        VBox.setVgrow(contentBox, Priority.ALWAYS);

        if (model.totalFiles == 0) {
            VBox emptyBox = new VBox(10);
            emptyBox.setAlignment(Pos.CENTER);
            emptyBox.setPrefHeight(250);
            HBox.setHgrow(emptyBox, Priority.ALWAYS);
            emptyBox.setStyle("-fx-background-color: #323c41; -fx-background-radius: 8; -fx-padding: 30;");

            Label emptyLabel = new Label("No files stored in drive yet.");
            emptyLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #d3c6aa;");
            Label emptySub = new Label("Upload files to visualize category distribution and storage analytics.");
            emptySub.setStyle("-fx-font-size: 12px; -fx-text-fill: #859289;");
            emptyBox.getChildren().addAll(emptyLabel, emptySub);
            contentBox.getChildren().add(emptyBox);
        } else {
            // Pie Chart on the left
            VBox chartBox = new VBox(8);
            chartBox.setAlignment(Pos.CENTER);
            chartBox.setPrefWidth(320);

            PieChart pieChart = createPieChart(model);
            chartBox.getChildren().add(pieChart);

            // Detailed Breakdown List on the right
            VBox breakdownBox = createBreakdownBox(model);
            HBox.setHgrow(breakdownBox, Priority.ALWAYS);

            contentBox.getChildren().addAll(chartBox, breakdownBox);
        }

        mainContainer.getChildren().addAll(headerBox, quotaCard, contentBox);
        dialog.getDialogPane().setContent(mainContainer);

        dialog.showAndWait();
    }

    private static VBox createQuotaCard(StorageStatsModel model) {
        VBox card = new VBox(8);
        card.setPadding(new Insets(12, 14, 12, 14));
        card.setStyle("-fx-background-color: #323c41; -fx-background-radius: 8; -fx-border-color: #3a454a; -fx-border-radius: 8;");

        HBox textRow = new HBox(10);
        textRow.setAlignment(Pos.CENTER_LEFT);

        Label quotaText = createLabel(
                String.format(Locale.US, "Used: %.2f MB of %.0f MB", model.totalSizeMB, model.quotaSizeMB),
                "-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #d3c6aa;"
        );

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label percentBadge = new Label(String.format(Locale.US, "%.1f%% Used", model.quotaUsedPercent));
        String badgeColor = model.quotaUsedPercent >= 90 ? "#e67e80" : (model.quotaUsedPercent >= 70 ? "#e69875" : "#a7c080");
        percentBadge.setStyle(String.format("-fx-background-color: %s22; -fx-text-fill: %s; -fx-border-color: %s; -fx-border-radius: 10; -fx-background-radius: 10; -fx-padding: 2 8; -fx-font-size: 11px; -fx-font-weight: bold;", badgeColor, badgeColor, badgeColor));

        textRow.getChildren().addAll(quotaText, spacer, percentBadge);

        // Progress Bar
        ProgressBar progressBar = new ProgressBar(Math.min(model.quotaUsedPercent / 100.0, 1.0));
        progressBar.setMaxWidth(Double.MAX_VALUE);
        progressBar.setPrefHeight(12);
        progressBar.setStyle(String.format("-fx-accent: %s;", badgeColor));

        // Subtext row
        HBox subRow = new HBox(10);
        double freeMB = Math.max(0.0, model.quotaSizeMB - model.totalSizeMB);
        Label freeLabel = createLabel(String.format(Locale.US, "%.2f MB remaining free", freeMB), "-fx-font-size: 11px; -fx-text-fill: #859289;");

        Region subSpacer = new Region();
        HBox.setHgrow(subSpacer, Priority.ALWAYS);

        Label fileCountLabel = createLabel(String.format(Locale.US, "Total Files: %d", model.totalFiles), "-fx-font-size: 11px; -fx-text-fill: #859289;");
        subRow.getChildren().addAll(freeLabel, subSpacer, fileCountLabel);

        card.getChildren().addAll(textRow, progressBar, subRow);
        return card;
    }

    private static PieChart createPieChart(StorageStatsModel model) {
        ObservableList<PieChart.Data> chartData = FXCollections.observableArrayList();

        for (Map.Entry<String, Long> entry : model.categorySizes.entrySet()) {
            if (entry.getValue() > 0) {
                double sizeMB = entry.getValue() / (1024.0 * 1024.0);
                chartData.add(new PieChart.Data(entry.getKey(), Math.max(sizeMB, 0.001)));
            }
        }

        PieChart chart = new PieChart(chartData);
        chart.setLegendVisible(false);
        chart.setLabelsVisible(true);
        chart.setPrefSize(300, 260);

        // Apply Everforest colors and tooltips
        for (PieChart.Data data : chartData) {
            String catName = data.getName();
            String colorHex = CATEGORY_COLORS.getOrDefault(catName, "#7daea3");

            applySliceColor(data, colorHex);

            long bytes = model.categorySizes.getOrDefault(catName, 0L);
            long count = model.categoryCounts.getOrDefault(catName, 0L);
            double pct = model.totalSizeBytes > 0 ? (bytes * 100.0 / model.totalSizeBytes) : 0.0;

            String tooltipText = String.format(Locale.US, "%s\nSize: %s\nFiles: %d\nShare: %.1f%%",
                    catName, formatBytes(bytes), count, pct);
            Tooltip tooltip = new Tooltip(tooltipText);
            tooltip.setStyle("-fx-font-size: 12px;");

            if (data.getNode() != null) {
                Tooltip.install(data.getNode(), tooltip);
            }
            data.nodeProperty().addListener((obs, oldNode, newNode) -> {
                if (newNode != null) {
                    Tooltip.install(newNode, tooltip);
                    applySliceColor(data, colorHex);
                }
            });
        }

        return chart;
    }

    private static void applySliceColor(PieChart.Data data, String colorHex) {
        if (data.getNode() != null) {
            data.getNode().setStyle(String.format("-fx-pie-color: %s;", colorHex));
        }
        data.nodeProperty().addListener((obs, oldVal, node) -> {
            if (node != null) {
                node.setStyle(String.format("-fx-pie-color: %s;", colorHex));
            }
        });
    }

    private static VBox createBreakdownBox(StorageStatsModel model) {
        VBox container = new VBox(8);
        container.setPadding(new Insets(10));
        container.setStyle("-fx-background-color: #323c41; -fx-background-radius: 8; -fx-border-color: #3a454a; -fx-border-radius: 8;");

        Label title = createLabel("Category Breakdown", "-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #d3c6aa;");
        container.getChildren().add(title);

        VBox rowsBox = new VBox(6);

        // Sort categories by size descending
        List<Map.Entry<String, Long>> sorted = new ArrayList<>(model.categorySizes.entrySet());
        sorted.sort((a, b) -> Long.compare(b.getValue(), a.getValue()));

        for (Map.Entry<String, Long> entry : sorted) {
            String cat = entry.getKey();
            long bytes = entry.getValue();
            long count = model.categoryCounts.getOrDefault(cat, 0L);
            double pct = model.totalSizeBytes > 0 ? (bytes * 100.0 / model.totalSizeBytes) : 0.0;
            String color = CATEGORY_COLORS.getOrDefault(cat, "#7daea3");

            HBox row = new HBox(8);
            row.setAlignment(Pos.CENTER_LEFT);
            row.setPadding(new Insets(4, 6, 4, 6));
            row.setStyle("-fx-background-color: #2b3339; -fx-background-radius: 5;");

            // Color Swatch
            Rectangle swatch = new Rectangle(10, 10);
            swatch.setArcWidth(3);
            swatch.setArcHeight(3);
            swatch.setStyle(String.format("-fx-fill: %s;", color));

            Label nameLabel = createLabel(cat, "-fx-font-weight: bold; -fx-font-size: 12px; -fx-text-fill: #d3c6aa;");
            nameLabel.setPrefWidth(100);

            Label countLabel = createLabel(count + (count == 1 ? " file" : " files"), "-fx-font-size: 11px; -fx-text-fill: #859289;");
            countLabel.setPrefWidth(65);

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            Label sizeLabel = createLabel(formatBytes(bytes), "-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #d3c6aa;");

            Label pctLabel = createLabel(String.format(Locale.US, "%.1f%%", pct), String.format("-fx-font-size: 11px; -fx-text-fill: %s; -fx-font-weight: bold;", color));
            pctLabel.setPrefWidth(45);
            pctLabel.setAlignment(Pos.CENTER_RIGHT);

            row.getChildren().addAll(swatch, nameLabel, countLabel, spacer, sizeLabel, pctLabel);
            rowsBox.getChildren().add(row);
        }

        ScrollPane scroll = new ScrollPane(rowsBox);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        scroll.setPrefHeight(200);
        VBox.setVgrow(scroll, Priority.ALWAYS);

        container.getChildren().add(scroll);
        return container;
    }

    private static StorageStatsModel parseStatsJson(String json) {
        StorageStatsModel model = new StorageStatsModel();
        if (json == null || json.trim().isEmpty()) {
            return model;
        }

        model.username = extractStringField(json, "username");
        model.totalFiles = extractLongField(json, "totalFiles", 0L);
        model.totalSizeBytes = extractLongField(json, "totalSizeBytes", 0L);
        model.totalSizeMB = extractDoubleField(json, "totalSizeMB", 0.0);
        model.quotaSizeBytes = extractLongField(json, "quotaSizeBytes", 500L * 1024L * 1024L);
        model.quotaSizeMB = extractDoubleField(json, "quotaSizeMB", 500.0);
        model.quotaUsedPercent = extractDoubleField(json, "quotaUsedPercent", 0.0);

        parseNestedLongMap(json, "categorySizes", model.categorySizes);
        parseNestedLongMap(json, "categoryCounts", model.categoryCounts);
        parseNestedDoubleMap(json, "categorySizeMB", model.categorySizeMB);

        return model;
    }

    private static void parseNestedLongMap(String json, String mapKey, Map<String, Long> target) {
        String pattern = "\"" + mapKey + "\"\\s*:\\s*\\{([^}]*)\\}";
        Matcher m = Pattern.compile(pattern).matcher(json);
        if (m.find()) {
            String block = m.group(1).trim();
            if (!block.isEmpty()) {
                String[] entries = block.split(",");
                for (String entry : entries) {
                    String[] kv = entry.split(":", 2);
                    if (kv.length == 2) {
                        String key = kv[0].replace("\"", "").trim();
                        try {
                            long val = Long.parseLong(kv[1].trim());
                            target.put(key, val);
                        } catch (NumberFormatException ignored) {}
                    }
                }
            }
        }
    }

    private static void parseNestedDoubleMap(String json, String mapKey, Map<String, Double> target) {
        String pattern = "\"" + mapKey + "\"\\s*:\\s*\\{([^}]*)\\}";
        Matcher m = Pattern.compile(pattern).matcher(json);
        if (m.find()) {
            String block = m.group(1).trim();
            if (!block.isEmpty()) {
                String[] entries = block.split(",");
                for (String entry : entries) {
                    String[] kv = entry.split(":", 2);
                    if (kv.length == 2) {
                        String key = kv[0].replace("\"", "").trim();
                        try {
                            double val = Double.parseDouble(kv[1].trim());
                            target.put(key, val);
                        } catch (NumberFormatException ignored) {}
                    }
                }
            }
        }
    }

    private static String extractStringField(String json, String field) {
        Matcher m = Pattern.compile("\"" + field + "\"\\s*:\\s*\"([^\"]*)\"").matcher(json);
        return m.find() ? m.group(1) : "";
    }

    private static long extractLongField(String json, String field, long fallback) {
        Matcher m = Pattern.compile("\"" + field + "\"\\s*:\\s*([0-9]+)").matcher(json);
        if (m.find()) {
            try {
                return Long.parseLong(m.group(1));
            } catch (NumberFormatException ignored) {}
        }
        return fallback;
    }

    private static double extractDoubleField(String json, String field, double fallback) {
        Matcher m = Pattern.compile("\"" + field + "\"\\s*:\\s*\"?([0-9.]+)\"?").matcher(json);
        if (m.find()) {
            try {
                return Double.parseDouble(m.group(1));
            } catch (NumberFormatException ignored) {}
        }
        return fallback;
    }

    private static String formatBytes(long bytes) {
        if (bytes < 1024) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(1024));
        String pre = "KMGTPE".charAt(exp - 1) + "";
        return String.format(Locale.US, "%.1f %sB", bytes / Math.pow(1024, exp), pre);
    }

    private static Label createLabel(String text, String style) {
        Label label = new Label(text);
        if (style != null && !style.isEmpty()) {
            label.setStyle(style);
        }
        return label;
    }
}
