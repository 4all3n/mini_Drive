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

public class RegisterScreen {

    private VBox view;
    private TextField usernameField;
    private PasswordField passwordField;
    private PasswordField confirmPasswordField;
    private Button registerButton;
    private Button switchToLoginButton;
    private Label statusLabel;

    public RegisterScreen() {
        // 1. Initialize the layout container
        view = new VBox(15); 
        view.setAlignment(Pos.CENTER);
        view.setPadding(new Insets(40));
        view.setStyle("-fx-background-color: #f4f4f4;");

        // 2. Create UI Components
        Label titleLabel = new Label("Create an Account");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 24));

        usernameField = new TextField();
        usernameField.setPromptText("Choose a Username");
        usernameField.setMaxWidth(250);

        passwordField = new PasswordField();
        passwordField.setPromptText("Create a Password");
        passwordField.setMaxWidth(250);

        confirmPasswordField = new PasswordField();
        confirmPasswordField.setPromptText("Confirm Password");
        confirmPasswordField.setMaxWidth(250);

        registerButton = new Button("Register");
        // Using a green color to indicate a "create/new" action
        registerButton.setStyle("-fx-background-color: #28a745; -fx-text-fill: white; -fx-font-weight: bold;");
        registerButton.setPrefWidth(250);

        switchToLoginButton = new Button("Already have an account? Log in.");
        switchToLoginButton.setStyle("-fx-background-color: transparent; -fx-text-fill: #0078D7; -fx-underline: true;");

        statusLabel = new Label(); // Empty label for error/success messages

        // 3. Add components to the layout
        view.getChildren().addAll(
            titleLabel, 
            usernameField, 
            passwordField, 
            confirmPasswordField,
            registerButton, 
            statusLabel, 
            switchToLoginButton
        );
    }

    // Getters for the Controller to access these elements
    public VBox getView() { return view; }
    public TextField getUsernameField() { return usernameField; }
    public PasswordField getPasswordField() { return passwordField; }
    public PasswordField getConfirmPasswordField() { return confirmPasswordField; }
    public Button getRegisterButton() { return registerButton; }
    public Button getSwitchToLoginButton() { return switchToLoginButton; }
    public Label getStatusLabel() { return statusLabel; }
}