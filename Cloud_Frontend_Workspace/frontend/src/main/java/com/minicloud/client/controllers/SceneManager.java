package com.minicloud.client.controllers;

import com.minicloud.client.models.BreadcrumbRecord;
import com.minicloud.client.models.DriveItem;
import com.minicloud.client.models.FolderRecord;
import com.minicloud.client.network.AuthNetworkService;
import com.minicloud.client.network.FileNetworkService;
import com.minicloud.client.ui.ContextMenuFactory;
import com.minicloud.client.ui.DashboardScreen;
import com.minicloud.client.ui.LoginScreen;
import com.minicloud.client.ui.RegisterScreen;
import com.minicloud.client.ui.dialogs.DuplicateFileDialog;
import com.minicloud.client.ui.dialogs.FilePickerDialog;
import com.minicloud.client.ui.dialogs.FilePreviewDialog;
import com.minicloud.client.ui.dialogs.MoveDialog;
import com.minicloud.client.ui.dialogs.NewFolderDialog;
import com.minicloud.client.ui.dialogs.PropertiesDialog;
import com.minicloud.client.ui.dialogs.RenameDialog;
import com.minicloud.client.ui.dialogs.StorageAnalyticsDialog;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.TableRow;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Controller orchestrating UI transitions, event routing, and API interactions.
 */
public class SceneManager {

    private final Stage primaryStage;
    private final AuthNetworkService networkService;
    private final FileNetworkService fileNetworkService;
    private Long currentFolderId = null;
    private List<FolderRecord> cachedFolders = new ArrayList<>();
    private String cachedStatsJson = null;

    public SceneManager(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.networkService = new AuthNetworkService();
        this.fileNetworkService = new FileNetworkService();
    }

    public void showLoginScreen() {
        LoginScreen loginScreen = new LoginScreen();
        
        loginScreen.getSwitchToRegisterButton().setOnAction(e -> showRegisterScreen());
        
        loginScreen.getLoginButton().setOnAction(e -> {
            String user = loginScreen.getUsernameField().getText();
            String pass = loginScreen.getPasswordField().getText();
            
            if (user.isEmpty() || pass.isEmpty()) {
                loginScreen.getStatusLabel().setText("Fields cannot be empty.");
                loginScreen.getStatusLabel().setStyle("-fx-text-fill: red;");
                return;
            }

            loginScreen.getStatusLabel().setText("Connecting to server...");
            loginScreen.getStatusLabel().setStyle("-fx-text-fill: orange;");

            boolean success = networkService.authenticateUser(user, pass);
            
            if (success) {
                showDashboardScreen(user);
            } else {
                loginScreen.getStatusLabel().setText("Login Failed or Server Offline.");
                loginScreen.getStatusLabel().setStyle("-fx-text-fill: red;");
            }
        });

        Scene scene = new Scene(loginScreen.getView(), 400, 500);
        // Apply Everforest theme to the login scene
        scene.getStylesheets().add(getClass().getResource("/css/everforest.css").toExternalForm());
        primaryStage.setTitle("Mini Cloud - Login");
        primaryStage.setScene(scene);
    }

    public void showRegisterScreen() {
        RegisterScreen registerScreen = new RegisterScreen();
        
        registerScreen.getSwitchToLoginButton().setOnAction(e -> showLoginScreen());

        registerScreen.getRegisterButton().setOnAction(e -> {
            String user = registerScreen.getUsernameField().getText();
            String pass = registerScreen.getPasswordField().getText();
            String confirmPass = registerScreen.getConfirmPasswordField().getText();
            
            if (user.isEmpty() || pass.isEmpty() || confirmPass.isEmpty()) {
                registerScreen.getStatusLabel().setText("All fields are required.");
                registerScreen.getStatusLabel().setStyle("-fx-text-fill: red;");
                return;
            }

            if (!pass.equals(confirmPass)) {
                registerScreen.getStatusLabel().setText("Passwords do not match.");
                registerScreen.getStatusLabel().setStyle("-fx-text-fill: red;");
                return;
            }

            registerScreen.getStatusLabel().setText("Creating account...");
            registerScreen.getStatusLabel().setStyle("-fx-text-fill: orange;");

            boolean success = networkService.registerUser(user, pass);
            
            if (success) {
                registerScreen.getStatusLabel().setText("Account Created! Please Log in.");
                registerScreen.getStatusLabel().setStyle("-fx-text-fill: green;");
            } else {
                registerScreen.getStatusLabel().setText("Registration Failed");
                registerScreen.getStatusLabel().setStyle("-fx-text-fill: red;");
            }
        }); 

        Scene scene = new Scene(registerScreen.getView(), 400, 500);
        scene.getStylesheets().add(getClass().getResource("/css/everforest.css").toExternalForm());
        primaryStage.setTitle("Mini Cloud - Register");
        primaryStage.setScene(scene);
    }

    public void showDashboardScreen(String username) {
        DashboardScreen dashboard = new DashboardScreen(username);
        currentFolderId = null;

        dashboard.getLogoutButton().setOnAction(e -> {
            AuthNetworkService.clearToken();
            showLoginScreen();
        });

        // 1. Initial Data Load
        refreshDriveView(dashboard, username);

        // 2. Breadcrumb Navigation
        dashboard.getBreadcrumbBar().setOnNavigate(crumb -> {
            dashboard.getSearchField().clear();
            currentFolderId = crumb.getId();
            refreshDriveView(dashboard, username);
        });

        // 3. Folder Tree Selection (user clicked on sidebar tree)
        dashboard.getFolderTreePane().setOnFolderSelected(node -> {
            Long selectedId = node.getId();
            if ((currentFolderId == null && selectedId != null) || (currentFolderId != null && !currentFolderId.equals(selectedId))) {
                dashboard.getSearchField().clear();
                currentFolderId = selectedId;
                refreshDriveView(dashboard, username);
            }
        });

        // 4. Shared Context Menu & Item Open Handlers for Table and Grid View
        ContextMenuFactory.ContextMenuListener menuListener = new ContextMenuFactory.ContextMenuListener() {
            @Override
            public void onOpen(DriveItem item) {
                if (item.isFolder()) {
                    dashboard.getSearchField().clear();
                    currentFolderId = item.getId();
                    refreshDriveView(dashboard, username);
                } else {
                    handlePreview(dashboard, item);
                }
            }

            @Override
            public void onPreview(DriveItem item) {
                handlePreview(dashboard, item);
            }

            @Override
            public void onDownload(DriveItem item) {
                handleDownload(dashboard, item);
            }

            @Override
            public void onRename(DriveItem item) {
                handleRename(dashboard, username, item);
            }

            @Override
            public void onMove(DriveItem item) {
                handleMove(dashboard, username, item);
            }

            @Override
            public void onCopy(DriveItem item) {
                handleCopy(dashboard, username, item);
            }

            @Override
            public void onDelete(DriveItem item) {
                handleDelete(dashboard, username, item);
            }

            @Override
            public void onProperties(DriveItem item) {
                handleProperties(item);
            }
        };

        // Double Click on Folder/File in Table
        dashboard.getTable().setRowFactory(tv -> {
            TableRow<DriveItem> row = new TableRow<>();

            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (!row.isEmpty())) {
                    DriveItem item = row.getItem();
                    if (item != null) {
                        menuListener.onOpen(item);
                    }
                }
            });

            // Right-click context menu in Table
            row.itemProperty().addListener((obs, oldVal, item) -> {
                if (item != null) {
                    row.setContextMenu(ContextMenuFactory.create(item, menuListener));
                } else {
                    row.setContextMenu(null);
                }
            });

            return row;
        });

        // Double Click & Right Click in Grid View
        dashboard.getDriveGridView().setOnItemDoubleClicked(item -> {
            if (item != null) {
                menuListener.onOpen(item);
            }
        });

        dashboard.getDriveGridView().setOnItemContextMenu((item, event) -> {
            if (item != null) {
                ContextMenu menu = ContextMenuFactory.create(item, menuListener);
                if (menu != null) {
                    menu.show(primaryStage, event.getScreenX(), event.getScreenY());
                }
            }
        });

        // 6. New Folder Button
        dashboard.getNewFolderButton().setOnAction(e -> {
            Optional<String> result = NewFolderDialog.show(currentFolderId == null ? "My Drive" : "Current Folder");
            result.ifPresent(name -> {
                FolderRecord created = fileNetworkService.createFolder(name, currentFolderId);
                if (created != null) {
                    refreshDriveView(dashboard, username);
                } else {
                    showAlert(Alert.AlertType.ERROR, "Create Failed", "Could not create folder.");
                }
            });
        });

        // 7. Upload Button (uses Everforest in-app FilePickerDialog)
        dashboard.getUploadButton().setOnAction(e -> {
            List<File> selectedFiles = FilePickerDialog.showOpenMultipleFiles(primaryStage, "Select Files to Upload", null);
            if (selectedFiles != null && !selectedFiles.isEmpty()) {
                handleUploadFiles(dashboard, username, selectedFiles);
            }
        });

        // 7. Download Selected Button
        dashboard.getDownloadButton().setOnAction(e -> {
            DriveItem selected = dashboard.getTable().getSelectionModel().getSelectedItem();
            if (selected == null) {
                showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a file to download.");
                return;
            }
            if (selected.isFolder()) {
                showAlert(Alert.AlertType.INFORMATION, "Folder Selected", "Folders cannot be directly downloaded as a single file.");
                return;
            }
            handleDownload(dashboard, selected);
        });

        // 8. Delete Selected Button
        dashboard.getDeleteButton().setOnAction(e -> {
            DriveItem selected = dashboard.getTable().getSelectionModel().getSelectedItem();
            if (selected == null) {
                showAlert(Alert.AlertType.WARNING, "No Selection", "Please select an item to delete.");
                return;
            }
            handleDelete(dashboard, username, selected);
        });

        // 9. Refresh Button
        dashboard.getRefreshButton().setOnAction(e -> {
            dashboard.getSearchField().clear();
            refreshDriveView(dashboard, username);
        });

        // 10. Recursive Subfolder Search Handler (with 250ms debounce)
        PauseTransition searchDebounce = new PauseTransition(Duration.millis(250));
        searchDebounce.setOnFinished(e -> {
            String query = dashboard.getSearchField().getText();
            performSearch(dashboard, username, query);
        });

        dashboard.getSearchField().textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null || newVal.trim().isEmpty()) {
                searchDebounce.stop();
                refreshDriveView(dashboard, username);
            } else {
                searchDebounce.playFromStart();
            }
        });

        dashboard.getSearchField().setOnAction(e -> {
            searchDebounce.stop();
            performSearch(dashboard, username, dashboard.getSearchField().getText());
        });

        // 11. Storage Analytics Button & Label
        dashboard.getStorageAnalyticsButton().setOnAction(e -> handleShowStorageAnalytics());
        dashboard.getStorageStatsLabel().setOnMouseClicked(e -> handleShowStorageAnalytics());

        Scene scene = new Scene(dashboard.getView(), 1050, 700);
        scene.getStylesheets().add(getClass().getResource("/css/everforest.css").toExternalForm());
        primaryStage.setTitle("Mini Cloud Drive");
        primaryStage.setScene(scene);
    }

    private void performSearch(DashboardScreen dashboard, String username, String query) {
        if (query == null || query.trim().isEmpty()) {
            refreshDriveView(dashboard, username);
            return;
        }

        new Thread(() -> {
            List<BreadcrumbRecord> breadcrumbs = new ArrayList<>();
            List<DriveItem> results = fileNetworkService.searchDrive(query.trim(), currentFolderId, breadcrumbs);

            Platform.runLater(() -> {
                dashboard.getMasterData().setAll(results);
                dashboard.getBreadcrumbBar().setBreadcrumbs(breadcrumbs);
            });
        }).start();
    }

    private void refreshDriveView(DashboardScreen dashboard, String username) {
        new Thread(() -> {
            List<BreadcrumbRecord> breadcrumbs = new ArrayList<>();
            List<DriveItem> items = fileNetworkService.fetchDriveContents(currentFolderId, breadcrumbs);
            List<FolderRecord> allFolders = fileNetworkService.fetchAllFolders();
            cachedFolders = allFolders;

            String statsJson = fileNetworkService.fetchStorageStats();
            cachedStatsJson = statsJson;

            Platform.runLater(() -> {
                dashboard.getMasterData().setAll(items);
                dashboard.getBreadcrumbBar().setBreadcrumbs(breadcrumbs);
                dashboard.getFolderTreePane().populateTree(allFolders);
                dashboard.getFolderTreePane().selectFolder(currentFolderId);

                if (statsJson != null && statsJson.contains("totalSizeMB")) {
                    String mb = extractJsonValue(statsJson, "totalSizeMB");
                    String count = extractJsonValue(statsJson, "totalFiles");
                    dashboard.getStorageStatsLabel().setText("Used: " + mb + " MB (" + count + " files)");
                }
            });
        }).start();
    }

    private void handleShowStorageAnalytics() {
        if (cachedStatsJson != null) {
            StorageAnalyticsDialog.show(primaryStage, cachedStatsJson);
        } else {
            new Thread(() -> {
                String stats = fileNetworkService.fetchStorageStats();
                cachedStatsJson = stats;
                Platform.runLater(() -> StorageAnalyticsDialog.show(primaryStage, stats));
            }).start();
        }
    }

    private void handlePreview(DashboardScreen dashboard, DriveItem item) {
        FilePreviewDialog.show(primaryStage, item, fileNetworkService, () -> handleDownload(dashboard, item));
    }

    private void handleDownload(DashboardScreen dashboard, DriveItem item) {
        Optional<File> saveDirOpt = FilePickerDialog.showSelectFolder(primaryStage, "Select Destination Folder", null);
        if (saveDirOpt.isPresent()) {
            File saveDir = saveDirOpt.get();
            dashboard.getDownloadButton().setText("Downloading...");
            dashboard.getDownloadButton().setDisable(true);

            new Thread(() -> {
                boolean ok = fileNetworkService.downloadFile(item.getId(), item.getName(), saveDir);
                Platform.runLater(() -> {
                    dashboard.getDownloadButton().setText("Download");
                    dashboard.getDownloadButton().setDisable(false);
                    showAlert(ok ? Alert.AlertType.INFORMATION : Alert.AlertType.ERROR,
                            "Download Status", ok ? "File saved successfully to " + saveDir.getAbsolutePath() : "Download failed.");
                });
            }).start();
        }
    }

    private void handleRename(DashboardScreen dashboard, String username, DriveItem item) {
        Optional<String> newName = RenameDialog.show(item.getName(), item.isFolder());
        newName.ifPresent(name -> {
            boolean ok;
            if (item.isFolder()) {
                ok = fileNetworkService.renameFolder(item.getId(), name);
            } else {
                ok = fileNetworkService.renameFile(item.getId(), name);
            }
            if (ok) {
                refreshDriveView(dashboard, username);
            } else {
                showAlert(Alert.AlertType.ERROR, "Rename Failed", "Could not rename item.");
            }
        });
    }

    private void handleMove(DashboardScreen dashboard, String username, DriveItem item) {
        Optional<MoveDialog.MoveTarget> target = MoveDialog.show(item.getName(), cachedFolders, currentFolderId);
        target.ifPresent(t -> {
            boolean ok;
            if (item.isFolder()) {
                ok = fileNetworkService.moveFolder(item.getId(), t.getFolderId());
            } else {
                ok = fileNetworkService.moveFile(item.getId(), t.getFolderId());
            }
            if (ok) {
                refreshDriveView(dashboard, username);
            } else {
                showAlert(Alert.AlertType.ERROR, "Move Failed", "Cannot move item. Verify destination is not a descendant.");
            }
        });
    }

    private void handleCopy(DashboardScreen dashboard, String username, DriveItem item) {
        if (item.isFolder()) return;
        boolean ok = fileNetworkService.copyFile(item.getId());
        if (ok) {
            refreshDriveView(dashboard, username);
            showAlert(Alert.AlertType.INFORMATION, "Copied", "Created a copy of " + item.getName());
        } else {
            showAlert(Alert.AlertType.ERROR, "Copy Failed", "Could not duplicate file.");
        }
    }

    private void handleDelete(DashboardScreen dashboard, String username, DriveItem item) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        try {
            confirm.getDialogPane().getStylesheets().add(getClass().getResource("/css/everforest.css").toExternalForm());
        } catch (Exception ignored) {}
        confirm.setTitle("Confirm Deletion");
        confirm.setHeaderText("Delete " + (item.isFolder() ? "Folder" : "File") + " \"" + item.getName() + "\"?");
        confirm.setContentText(item.isFolder() ? "All files and subfolders inside will be permanently deleted." : "This cannot be undone.");

        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            boolean ok;
            if (item.isFolder()) {
                ok = fileNetworkService.deleteFolder(item.getId());
            } else {
                ok = fileNetworkService.deleteFile(item.getId());
            }
            if (ok) {
                refreshDriveView(dashboard, username);
            } else {
                showAlert(Alert.AlertType.ERROR, "Delete Failed", "Could not delete item.");
            }
        }
    }

    private void handleUploadFiles(DashboardScreen dashboard, String username, List<File> files) {
        if (files == null || files.isEmpty()) return;

        List<File> allFiles = new ArrayList<>();
        collectFilesRecursively(files, allFiles);
        if (allFiles.isEmpty()) return;

        dashboard.getUploadButton().setText("Uploading...");
        dashboard.getUploadButton().setDisable(true);

        new Thread(() -> {
            try {
                for (File file : allFiles) {
                    if (file == null || !file.exists()) {
                        continue;
                    }
                    String originalName = file.getName();
                    System.out.println("[Upload] Processing: " + originalName);
                    DriveItem existing = findExistingItem(dashboard, originalName);

                    if (existing != null) {
                        // Conflict detected! Ask user: Overwrite, Rename, or Cancel
                        CompletableFuture<Optional<DuplicateFileDialog.Resolution>> future = new CompletableFuture<>();
                        Platform.runLater(() -> {
                            String suggested = generateUniqueName(dashboard, originalName);
                            Optional<DuplicateFileDialog.Resolution> res = DuplicateFileDialog.show(originalName, suggested);
                            future.complete(res);
                        });

                        Optional<DuplicateFileDialog.Resolution> optRes = future.get();
                        if (optRes.isPresent()) {
                            DuplicateFileDialog.Resolution res = optRes.get();
                            if (res.getChoice() == DuplicateFileDialog.Choice.OVERWRITE) {
                                // Delete existing file first then upload new file
                                fileNetworkService.deleteFile(existing.getId());
                                executeSingleUpload(dashboard, username, file, originalName);
                            } else if (res.getChoice() == DuplicateFileDialog.Choice.RENAME) {
                                String chosen = res.getNewName();
                                if (chosen == null || chosen.trim().isEmpty()) {
                                    chosen = generateUniqueName(dashboard, originalName);
                                }
                                executeSingleUpload(dashboard, username, file, chosen.trim());
                            }
                            // Choice.CANCEL ignores the file
                        }
                    } else {
                        // No conflict, upload directly
                        executeSingleUpload(dashboard, username, file, originalName);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                Platform.runLater(() -> {
                    dashboard.getUploadButton().setText("Upload");
                    dashboard.getUploadButton().setDisable(false);
                    refreshDriveView(dashboard, username);
                });
            }
        }).start();
    }

    private void collectFilesRecursively(List<File> input, List<File> output) {
        if (input == null) return;
        for (File f : input) {
            if (f == null || !f.exists()) continue;
            if (f.isDirectory()) {
                File[] children = f.listFiles();
                if (children != null) {
                    collectFilesRecursively(Arrays.asList(children), output);
                }
            } else if (f.isFile()) {
                output.add(f);
            }
        }
    }

    private void executeSingleUpload(DashboardScreen dashboard, String username, File file, String targetFileName) {
        String result = fileNetworkService.uploadFile(file, targetFileName, username, currentFolderId);
        if (!"SUCCESS".equals(result)) {
            Platform.runLater(() -> {
                showAlert(Alert.AlertType.ERROR, "Upload Error", "Failed to upload " + targetFileName + ": " + result);
            });
        }
    }

    private DriveItem findExistingItem(DashboardScreen dashboard, String filename) {
        for (DriveItem item : dashboard.getMasterData()) {
            if (!item.isFolder() && item.getName().equalsIgnoreCase(filename)) {
                return item;
            }
        }
        return null;
    }

    private String generateUniqueName(DashboardScreen dashboard, String filename) {
        String baseName = filename;
        String extension = "";
        int dotIdx = filename.lastIndexOf('.');
        if (dotIdx > 0) {
            baseName = filename.substring(0, dotIdx);
            extension = filename.substring(dotIdx);
        }

        int counter = 1;
        String candidate = baseName + " (" + counter + ")" + extension;
        while (findExistingItem(dashboard, candidate) != null) {
            counter++;
            candidate = baseName + " (" + counter + ")" + extension;
        }
        return candidate;
    }

    private void handleProperties(DriveItem item) {
        String info;
        if (item.isFolder()) {
            info = fileNetworkService.fetchFolderProperties(item.getId());
        } else {
            info = fileNetworkService.fetchFileProperties(item.getId());
        }
        PropertiesDialog.show(item.getName(), info);
    }

    private String extractJsonValue(String json, String key) {
        String pattern = "\"" + key + "\"\\s*:\\s*\"?([^\",}]*)\"?";
        java.util.regex.Matcher m = java.util.regex.Pattern.compile(pattern).matcher(json);
        return m.find() ? m.group(1).trim() : "--";
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        try {
            alert.getDialogPane().getStylesheets().add(getClass().getResource("/css/everforest.css").toExternalForm());
        } catch (Exception ignored) {}
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}