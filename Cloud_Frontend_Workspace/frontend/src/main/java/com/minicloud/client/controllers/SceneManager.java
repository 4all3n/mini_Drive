package com.minicloud.client.controllers;


import com.minicloud.client.network.AuthNetworkService;
import com.minicloud.client.network.FileNetworkService;
import com.minicloud.client.ui.DashboardScreen;
import com.minicloud.client.ui.LoginScreen;
import com.minicloud.client.ui.RegisterScreen;
import javafx.scene.Scene;
import javafx.stage.Stage;

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

        // --- NEW: Load files immediately when the dashboard opens ---
        java.util.List<DashboardScreen.FileRecord> existingFiles = fileNetworkService.fetchUserFiles(username);
        dashboard.getTable().getItems().addAll(existingFiles);
        
        dashboard.getUploadButton().setOnAction(e -> {
            javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
            fileChooser.setTitle("Select File to Upload");
            java.io.File selectedFile = fileChooser.showOpenDialog(primaryStage);
            
            if (selectedFile != null) {
               dashboard.getUploadButton().setText("Uploading...");
                dashboard.getUploadButton().setDisable(true);

                // Now it returns a String instead of a boolean
                String resultMessage = fileNetworkService.uploadFile(selectedFile, username);
                boolean success = "SUCCESS".equals(resultMessage);
                
                if (success) {
                    dashboard.getTable().getItems().clear(); 
                    java.util.List<DashboardScreen.FileRecord> freshFiles = fileNetworkService.fetchUserFiles(username);
                    dashboard.getTable().getItems().addAll(freshFiles); 
                }

                // Show the dynamic alert message
                javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                    success ? javafx.scene.control.Alert.AlertType.INFORMATION : javafx.scene.control.Alert.AlertType.ERROR
                );
                alert.setTitle("Upload Status");
                alert.setHeaderText(null);
                // If success, say so. Otherwise, show the exact error message from the server!
                alert.setContentText(success ? "File uploaded successfully!" : resultMessage);
                alert.showAndWait();

                dashboard.getUploadButton().setText("+ Upload File");
                dashboard.getUploadButton().setDisable(false);
            }
        });

        Scene scene = new Scene(dashboard.getView(), 800, 600);
        primaryStage.setTitle("Mini Cloud - Dashboard");
        primaryStage.setScene(scene);
    }
}