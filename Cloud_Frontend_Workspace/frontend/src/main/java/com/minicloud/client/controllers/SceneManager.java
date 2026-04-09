package com.minicloud.client.controllers;

import com.minicloud.client.network.AuthNetworkService;
import com.minicloud.client.network.FileNetworkService;
import com.minicloud.client.ui.DashboardScreen;
import com.minicloud.client.ui.LoginScreen;
import com.minicloud.client.ui.RegisterScreen;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.util.List;

public class SceneManager {

    private Stage primaryStage;
    private AuthNetworkService networkService;
    private FileNetworkService fileNetworkService;

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
            
            if(user.isEmpty() || pass.isEmpty()) {
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
            
            if(user.isEmpty() || pass.isEmpty() || confirmPass.isEmpty()) {
                registerScreen.getStatusLabel().setText("All fields are required.");
                registerScreen.getStatusLabel().setStyle("-fx-text-fill: red;");
                return;
            }

            if(!pass.equals(confirmPass)) {
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
        primaryStage.setTitle("Mini Cloud - Register");
        primaryStage.setScene(scene);
    }

    public void showDashboardScreen(String username) {
        DashboardScreen dashboard = new DashboardScreen(username);
        
        dashboard.getLogoutButton().setOnAction(e -> showLoginScreen());

        // Load files immediately
        List<DashboardScreen.FileRecord> existingFiles = fileNetworkService.fetchUserFiles(username);
        dashboard.getTable().getItems().addAll(existingFiles);
        
        // Upload Button Logic
        dashboard.getUploadButton().setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Select File to Upload");
            File selectedFile = fileChooser.showOpenDialog(primaryStage);
            
            if (selectedFile != null) {
                dashboard.getUploadButton().setText("Uploading...");
                dashboard.getUploadButton().setDisable(true);

                String resultMessage = fileNetworkService.uploadFile(selectedFile, username);
                boolean success = "SUCCESS".equals(resultMessage);
                
                if (success) {
                    dashboard.getTable().getItems().clear(); 
                    dashboard.getTable().getItems().addAll(fileNetworkService.fetchUserFiles(username)); 
                }

                showAlert(success ? Alert.AlertType.INFORMATION : Alert.AlertType.ERROR,
                    "Upload Status", success ? "File uploaded successfully!" : resultMessage);

                dashboard.getUploadButton().setText("+ Upload File");
                dashboard.getUploadButton().setDisable(false);
            }
        });

        // Download Button Logic
        dashboard.getDownloadButton().setOnAction(e -> {
            DashboardScreen.FileRecord selected = dashboard.getTable().getSelectionModel().getSelectedItem();
            if (selected == null) {
                showAlert(Alert.AlertType.WARNING, "No File Selected", "Please click a file in the table first.");
                return;
            }

            DirectoryChooser directoryChooser = new DirectoryChooser();
            directoryChooser.setTitle("Select Save Location");
            File saveDir = directoryChooser.showDialog(primaryStage);

            if (saveDir != null) {
                dashboard.getDownloadButton().setText("Downloading...");
                dashboard.getDownloadButton().setDisable(true);

                boolean success = fileNetworkService.downloadFile(selected.getId(), selected.getFileName(), saveDir);
                
                showAlert(success ? Alert.AlertType.INFORMATION : Alert.AlertType.ERROR,
                        "Download Status", success ? "File saved successfully!" : "Failed to download file.");
                
                dashboard.getDownloadButton().setText("Download Selected");
                dashboard.getDownloadButton().setDisable(false);
            }
        });

        // Delete Button Logic
        dashboard.getDeleteButton().setOnAction(e -> {
            DashboardScreen.FileRecord selected = dashboard.getTable().getSelectionModel().getSelectedItem();
            if (selected == null) {
                showAlert(Alert.AlertType.WARNING, "No File Selected", "Please click a file in the table first.");
                return;
            }

            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Confirm Deletion");
            confirm.setHeaderText("Delete " + selected.getFileName() + "?");
            confirm.setContentText("This cannot be undone.");
            
            if (confirm.showAndWait().get() == ButtonType.OK) {
                boolean success = fileNetworkService.deleteFile(selected.getId());
                if (success) {
                    dashboard.getTable().getItems().clear();
                    dashboard.getTable().getItems().addAll(fileNetworkService.fetchUserFiles(username));
                } else {
                    showAlert(Alert.AlertType.ERROR, "Delete Status", "Failed to delete file from server.");
                }
            }
        });

        Scene scene = new Scene(dashboard.getView(), 800, 600);
        primaryStage.setTitle("Mini Cloud - Dashboard");
        primaryStage.setScene(scene);
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}