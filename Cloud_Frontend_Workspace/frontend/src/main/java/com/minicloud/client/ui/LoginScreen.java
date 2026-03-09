package com.minicloud.client.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class LoginScreen {

    private VBox view;
    private TextField usernameField;
    private PasswordField passwordField;
    private Button loginButton;
    private Button switchToRegisterButton;
    private Label statusLabel;

    public LoginScreen() {
        // 1. Initialize the layout container
        view = new VBox(15); // 15px spacing between elements
        view.setAlignment(Pos.CENTER);
        view.setPadding(new Insets(40));
        view.setStyle("-fx-background-color: #f4f4f4;");

        // 2. Create UI Components
        Label titleLabel = new Label("Mini Cloud Login");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 24));

        usernameField = new TextField();
        usernameField.setPromptText("Enter Username");
        usernameField.setMaxWidth(250);

        passwordField = new PasswordField();
        passwordField.setPromptText("Enter Password");
        passwordField.setMaxWidth(250);

        loginButton = new Button("Login");
        loginButton.setStyle("-fx-background-color: #0078D7; -fx-text-fill: white; -fx-font-weight: bold;");
        loginButton.setPrefWidth(250);

        switchToRegisterButton = new Button("Don't have an account? Register here.");
        switchToRegisterButton.setStyle("-fx-background-color: transparent; -fx-text-fill: #0078D7; -fx-underline: true;");

        statusLabel = new Label(); // Empty label for error/success messages

        // 3. Add components to the layout
        view.getChildren().addAll(
            titleLabel, 
            usernameField, 
            passwordField, 
            loginButton, 
            statusLabel, 
            switchToRegisterButton
        );
        
        // Note: Button click logic will be handled by our Controller later, keeping UI and Logic separate!
    }

    // Getters so the Controller and MainApp can access these elements
    public VBox getView() { return view; }
    public Button getLoginButton() { return loginButton; }
    public Button getSwitchToRegisterButton() { return switchToRegisterButton; }
    public TextField getUsernameField() { return usernameField; }
    public PasswordField getPasswordField() { return passwordField; }
    public Label getStatusLabel() { return statusLabel; }
}