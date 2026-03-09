package com.minicloud.client.controllers;

import com.minicloud.client.network.AuthNetworkService;
import com.minicloud.client.ui.LoginScreen;
import com.minicloud.client.ui.RegisterScreen;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class SceneManager {

    private Stage primaryStage;
    private AuthNetworkService networkService; // Add the network service

    public SceneManager(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.networkService = new AuthNetworkService(); // Initialize it
    }

    public void showLoginScreen() {
        LoginScreen loginScreen = new LoginScreen();
        
        // Navigation
        loginScreen.getSwitchToRegisterButton().setOnAction(e -> showRegisterScreen());
        
        // --- NEW: Login Logic ---
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

            // Call the network service
            boolean success = networkService.authenticateUser(user, pass);
            
            if (success) {
                loginScreen.getStatusLabel().setText("Login Successful!");
                loginScreen.getStatusLabel().setStyle("-fx-text-fill: green;");
                // Later, this is where you will do: showDashboardScreen();
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
        
        // Navigation
        registerScreen.getSwitchToLoginButton().setOnAction(e -> showLoginScreen());

        // --- NEW: Register Logic ---
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

            // Call the network service
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
}