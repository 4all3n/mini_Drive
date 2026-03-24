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
        view = new VBox(); 
        view.setAlignment(Pos.CENTER);
        view.setStyle("-fx-background-color: #121212;");

        VBox card = new VBox(20);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(40));
        card.setMaxWidth(350);
        card.setStyle("-fx-background-color: #1E1E1E; -fx-background-radius: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.6), 15, 0, 0, 5);");

        Label titleLabel = new Label("Join Mini Cloud");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        titleLabel.setStyle("-fx-text-fill: #ffffff;");

        String inputStyle = "-fx-background-color: #2D2D2D; -fx-text-fill: white; -fx-prompt-text-fill: #888888; -fx-background-radius: 5; -fx-border-color: #444444; -fx-border-radius: 5; -fx-padding: 10;";

        usernameField = new TextField();
        usernameField.setPromptText("Choose a Username");
        usernameField.setStyle(inputStyle);
        usernameField.setMaxWidth(280);

        passwordField = new PasswordField();
        passwordField.setPromptText("Create a Password");
        passwordField.setStyle(inputStyle);
        passwordField.setMaxWidth(280);

        confirmPasswordField = new PasswordField();
        confirmPasswordField.setPromptText("Confirm Password");
        confirmPasswordField.setStyle(inputStyle);
        confirmPasswordField.setMaxWidth(280);

        registerButton = new Button("Register");
        registerButton.setStyle("-fx-background-color: #10b981; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5; -fx-padding: 10; -fx-cursor: hand;");
        registerButton.setPrefWidth(280);

        switchToLoginButton = new Button("Already have an account? Log in");
        switchToLoginButton.setStyle("-fx-background-color: transparent; -fx-text-fill: #3b82f6; -fx-cursor: hand;");

        statusLabel = new Label();

        card.getChildren().addAll(titleLabel, usernameField, passwordField, confirmPasswordField, registerButton, statusLabel, switchToLoginButton);
        view.getChildren().add(card);
    }

    public VBox getView() { return view; }
    public TextField getUsernameField() { return usernameField; }
    public PasswordField getPasswordField() { return passwordField; }
    public PasswordField getConfirmPasswordField() { return confirmPasswordField; }
    public Button getRegisterButton() { return registerButton; }
    public Button getSwitchToLoginButton() { return switchToLoginButton; }
    public Label getStatusLabel() { return statusLabel; }
}