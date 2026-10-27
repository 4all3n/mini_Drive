package com.minicloud.client.ui.dialogs;

import com.minicloud.client.models.DriveItem;
import com.minicloud.client.network.FileNetworkService;
import com.minicloud.client.ui.IconFactory;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Locale;

/**
 * In-App File Quick Preview Dialog.
 * Implemented as a dedicated Stage (avoiding AWT/GTK3 modal deadlocks on Linux).
 * Supports images (.png, .jpg, .gif, .bmp, .webp), code & text (.py, .java, .txt, .json, .sql, .md, etc.),
 * and binary files with direct system viewer dispatch via ProcessBuilder (xdg-open/open/rundll32).
 */
public class FilePreviewDialog extends Stage {

    public static void show(Window owner, DriveItem item, FileNetworkService networkService, Runnable downloadAction) {
        if (item == null || item.isFolder()) return;

        FilePreviewDialog preview = new FilePreviewDialog(owner, item, networkService, downloadAction);
        preview.showAndWait();
    }

    public FilePreviewDialog(Window owner, DriveItem item, FileNetworkService networkService, Runnable downloadAction) {
        initModality(Modality.APPLICATION_MODAL);
        if (owner != null) {
            initOwner(owner);
        }
        setTitle("Preview - " + item.getName());

        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #2b3339;");
        root.setPrefWidth(780);
        root.setPrefHeight(560);

        // 1. Header Bar
        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(10, 16, 10, 16));
        header.setStyle("-fx-background-color: #323c41; -fx-border-color: #3a454a; -fx-border-width: 0 0 1 0;");

        Node fileIcon = IconFactory.iconForType(item.getName(), item.getType(), false, 24);

        VBox titleBox = new VBox(2);
        Label nameLabel = new Label(item.getName());
        nameLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #d3c6aa;");

        HBox metaRow = new HBox(8);
        metaRow.setAlignment(Pos.CENTER_LEFT);

        Label catBadge = new Label(item.getCategory() != null ? item.getCategory() : "Uncategorized");
        catBadge.setStyle("-fx-background-color: #a7c08022; -fx-text-fill: #a7c080; -fx-border-color: #a7c080; -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 1 6; -fx-font-size: 10px; -fx-font-weight: bold;");

        Label sizeLabel = new Label("Size: " + item.getSize());
        sizeLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #859289;");

        Label locLabel = new Label("Location: " + item.getLocation());
        locLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #859289;");

        metaRow.getChildren().addAll(catBadge, sizeLabel, locLabel);
        titleBox.getChildren().addAll(nameLabel, metaRow);

        header.getChildren().addAll(fileIcon, titleBox);
        root.setTop(header);

        // 2. Central Content Pane
        StackPane contentPane = new StackPane();
        contentPane.setStyle("-fx-background-color: #1e2326; -fx-background-radius: 6; -fx-border-color: #3a454a; -fx-border-radius: 6;");
        BorderPane.setMargin(contentPane, new Insets(14, 16, 10, 16));

        VBox loadingBox = new VBox(12);
        loadingBox.setAlignment(Pos.CENTER);
        ProgressIndicator spinner = new ProgressIndicator();
        spinner.setPrefSize(40, 40);
        Label loadingText = new Label("Fetching file content from secure cloud storage...");
        loadingText.setStyle("-fx-text-fill: #859289; -fx-font-size: 12px;");
        loadingBox.getChildren().addAll(spinner, loadingText);
        contentPane.getChildren().add(loadingBox);

        root.setCenter(contentPane);

        // 3. Bottom Button Bar
        HBox bottomBar = new HBox(10);
        bottomBar.setAlignment(Pos.CENTER_RIGHT);
        bottomBar.setPadding(new Insets(10, 16, 12, 16));
        bottomBar.setStyle("-fx-background-color: #323c41; -fx-border-color: #3a454a; -fx-border-width: 1 0 0 0;");

        Button downloadBtn = new Button("Download");
        downloadBtn.setGraphic(IconFactory.downloadIcon(14));
        downloadBtn.getStyleClass().add("btn-primary");
        downloadBtn.setOnAction(e -> {
            close();
            if (downloadAction != null) {
                Platform.runLater(downloadAction);
            }
        });

        Button closeBtn = new Button("Close");
        closeBtn.setOnAction(e -> close());

        bottomBar.getChildren().addAll(downloadBtn, closeBtn);
        root.setBottom(bottomBar);

        Scene scene = new Scene(root, 780, 560);
        try {
            scene.getStylesheets().add(
                    getClass().getResource("/css/everforest.css").toExternalForm()
            );
        } catch (Exception ignored) {}

        setScene(scene);

        // Asynchronous byte fetch
        new Thread(() -> {
            byte[] fileBytes = networkService != null ? networkService.fetchFileBytes(item.getId()) : null;

            Platform.runLater(() -> {
                contentPane.getChildren().clear();
                if (fileBytes == null) {
                    contentPane.getChildren().add(createErrorView("Could not retrieve file content from server."));
                } else if (fileBytes.length == 0) {
                    contentPane.getChildren().add(createEmptyFileView(item));
                } else {
                    renderFilePreview(contentPane, item, fileBytes, downloadAction, this);
                }
            });
        }).start();
    }

    private static void renderFilePreview(StackPane container, DriveItem item, byte[] bytes, Runnable downloadAction, Stage parentStage) {
        String name = item.getName().toLowerCase(Locale.ROOT);
        String category = item.getCategory() != null ? item.getCategory().toLowerCase(Locale.ROOT) : "";

        // 1. Image Preview
        if (isImageExtension(name)) {
            try {
                Image image = new Image(new ByteArrayInputStream(bytes));
                if (!image.isError()) {
                    ImageView imageView = new ImageView(image);
                    imageView.setPreserveRatio(true);
                    imageView.setSmooth(true);
                    imageView.setFitWidth(730);
                    imageView.setFitHeight(410);

                    StackPane imageWrapper = new StackPane(imageView);
                    imageWrapper.setAlignment(Pos.CENTER);
                    imageWrapper.setPadding(new Insets(10));

                    ScrollPane scrollPane = new ScrollPane(imageWrapper);
                    scrollPane.setFitToWidth(true);
                    scrollPane.setFitToHeight(true);
                    scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");

                    Label dimLabel = new Label(String.format(Locale.US, "Resolution: %.0f × %.0f px", image.getWidth(), image.getHeight()));
                    dimLabel.setStyle("-fx-text-fill: #859289; -fx-font-size: 11px; -fx-padding: 4 10;");

                    VBox box = new VBox(4, scrollPane, dimLabel);
                    box.setAlignment(Pos.BOTTOM_RIGHT);
                    container.getChildren().add(box);
                    return;
                }
            } catch (Exception ignored) {}
        }

        // 2. Code / Text Preview
        if (isTextOrCode(name, category)) {
            try {
                String text = new String(bytes, StandardCharsets.UTF_8);

                TextArea textArea = new TextArea(text);
                textArea.setEditable(false);
                textArea.setWrapText(false);
                textArea.setStyle(
                        "-fx-font-family: 'Consolas', 'DejaVu Sans Mono', 'Courier New', monospace; " +
                        "-fx-font-size: 12px; " +
                        "-fx-control-inner-background: #1e2326; " +
                        "-fx-text-fill: #d3c6aa; " +
                        "-fx-highlight-fill: #3a454a; " +
                        "-fx-highlight-text-fill: #d3c6aa;"
                );
                VBox.setVgrow(textArea, Priority.ALWAYS);

                long lines = text.lines().count();
                HBox statusRow = new HBox(12);
                statusRow.setAlignment(Pos.CENTER_LEFT);
                statusRow.setPadding(new Insets(4, 8, 4, 8));

                Label linesLabel = new Label("Lines: " + lines);
                linesLabel.setStyle("-fx-text-fill: #859289; -fx-font-size: 11px;");

                Label encLabel = new Label("Encoding: UTF-8");
                encLabel.setStyle("-fx-text-fill: #859289; -fx-font-size: 11px;");

                Label sizeLabel = new Label("Raw Bytes: " + bytes.length);
                sizeLabel.setStyle("-fx-text-fill: #859289; -fx-font-size: 11px;");

                statusRow.getChildren().addAll(linesLabel, encLabel, sizeLabel);

                VBox textBox = new VBox(textArea, statusRow);
                VBox.setVgrow(textBox, Priority.ALWAYS);
                container.getChildren().add(textBox);
                return;
            } catch (Exception ignored) {}
        }

        // 3. Fallback: Binary / PDF / Rich Metadata Card with direct system viewer dispatch
        container.getChildren().add(createBinaryCard(item, bytes, downloadAction, parentStage));
    }

    private static VBox createBinaryCard(DriveItem item, byte[] bytes, Runnable downloadAction, Stage parentStage) {
        VBox card = new VBox(14);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(30));

        Region bigIcon = IconFactory.largeFileIcon(item.getName(), item.getType(), 64);

        Label nameLabel = new Label(item.getName());
        nameLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #d3c6aa;");

        Label noticeLabel = new Label("Binary or non-text document format. You can open directly in your system app or download.");
        noticeLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #859289;");

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(6);
        grid.setAlignment(Pos.CENTER);

        addRow(grid, 0, "Category:", item.getCategory());
        addRow(grid, 1, "File Type:", item.getType());
        addRow(grid, 2, "Size:", item.getSize() + " (" + (bytes != null ? bytes.length : 0) + " bytes)");
        addRow(grid, 3, "Upload Date:", item.getUploadDate());

        HBox btnRow = new HBox(12);
        btnRow.setAlignment(Pos.CENTER);

        Button openInAppBtn = new Button("Open in System Viewer");
        openInAppBtn.setGraphic(IconFactory.previewIcon(14));
        openInAppBtn.getStyleClass().add("btn-primary");
        openInAppBtn.setOnAction(e -> openWithSystemViewer(item.getName(), bytes));

        Button dlButton = new Button("Download to View Locally");
        dlButton.setGraphic(IconFactory.downloadIcon(14));
        dlButton.getStyleClass().add("btn-success");
        dlButton.setOnAction(e -> {
            if (parentStage != null) {
                parentStage.close();
            }
            if (downloadAction != null) {
                Platform.runLater(downloadAction);
            }
        });

        btnRow.getChildren().addAll(openInAppBtn, dlButton);

        card.getChildren().addAll(bigIcon, nameLabel, noticeLabel, grid, btnRow);
        return card;
    }

    private static void openWithSystemViewer(String fileName, byte[] bytes) {
        if (bytes == null || bytes.length == 0) return;

        new Thread(() -> {
            try {
                String prefix = "minidrive_preview_";
                String suffix = ".tmp";
                int dot = fileName.lastIndexOf('.');
                if (dot != -1) {
                    suffix = fileName.substring(dot);
                }
                File temp = File.createTempFile(prefix, suffix);
                temp.deleteOnExit();
                Files.write(temp.toPath(), bytes);

                String os = System.getProperty("os.name", "").toLowerCase(Locale.ROOT);
                ProcessBuilder pb;
                if (os.contains("win")) {
                    pb = new ProcessBuilder("rundll32", "url.dll,FileProtocolHandler", temp.getAbsolutePath());
                } else if (os.contains("mac")) {
                    pb = new ProcessBuilder("open", temp.getAbsolutePath());
                } else {
                    // Linux: xdg-open spawns the native desktop viewer (Evince, Chrome, Okular, etc.) safely detached
                    pb = new ProcessBuilder("xdg-open", temp.getAbsolutePath());
                }
                pb.start();
                System.out.println("[FilePreviewDialog] Launched system viewer for: " + temp.getAbsolutePath());
            } catch (Exception ex) {
                System.err.println("[FilePreviewDialog] Failed to launch system viewer: " + ex.getMessage());
            }
        }).start();
    }

    private static void addRow(GridPane grid, int row, String label, String val) {
        Label l = new Label(label);
        l.setStyle("-fx-text-fill: #859289; -fx-font-weight: bold; -fx-font-size: 12px;");
        Label v = new Label(val != null ? val : "--");
        v.setStyle("-fx-text-fill: #d3c6aa; -fx-font-size: 12px;");
        grid.add(l, 0, row);
        grid.add(v, 1, row);
    }

    private static VBox createEmptyFileView(DriveItem item) {
        VBox box = new VBox(10);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(30));

        Region icon = IconFactory.fileIcon(48);
        Label title = new Label("Empty File");
        title.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #dbbc7f;");

        Label msg = new Label("This file contains 0 bytes.");
        msg.setStyle("-fx-font-size: 12px; -fx-text-fill: #859289;");

        box.getChildren().addAll(icon, title, msg);
        return box;
    }

    private static VBox createErrorView(String message) {
        VBox box = new VBox(10);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(30));

        Label title = new Label("Preview Unavailable");
        title.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #e67e80;");

        Label msg = new Label(message);
        msg.setStyle("-fx-font-size: 12px; -fx-text-fill: #859289;");

        box.getChildren().addAll(title, msg);
        return box;
    }

    private static boolean isImageExtension(String name) {
        return name.endsWith(".png") || name.endsWith(".jpg") || name.endsWith(".jpeg")
                || name.endsWith(".gif") || name.endsWith(".bmp") || name.endsWith(".webp");
    }

    private static boolean isTextOrCode(String name, String category) {
        if ("code".equalsIgnoreCase(category) || "documents".equalsIgnoreCase(category)) {
            // Check text-based doc/code extensions
            if (name.endsWith(".txt") || name.endsWith(".md") || name.endsWith(".csv") || name.endsWith(".log")
                    || name.endsWith(".properties") || name.endsWith(".env") || name.endsWith(".json")
                    || name.endsWith(".xml") || name.endsWith(".yaml") || name.endsWith(".yml")
                    || name.endsWith(".ini") || name.endsWith(".conf")) {
                return true;
            }
        }
        return name.endsWith(".py") || name.endsWith(".java") || name.endsWith(".c")
                || name.endsWith(".cpp") || name.endsWith(".h") || name.endsWith(".cs")
                || name.endsWith(".js") || name.endsWith(".ts") || name.endsWith(".html")
                || name.endsWith(".css") || name.endsWith(".json") || name.endsWith(".xml")
                || name.endsWith(".yml") || name.endsWith(".yaml") || name.endsWith(".sql")
                || name.endsWith(".sh") || name.endsWith(".bat") || name.endsWith(".ps1")
                || name.endsWith(".md") || name.endsWith(".txt") || name.endsWith(".log")
                || name.endsWith(".gradle") || name.endsWith(".csv") || name.endsWith(".ini")
                || name.endsWith(".conf") || name.endsWith(".properties");
    }
}
