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

/**
 * Modern registration screen styled with Everforest dark theme.
 */
public class RegisterScreen {

    private final VBox view;
    private final TextField usernameField;
    private final PasswordField passwordField;
    private final PasswordField confirmPasswordField;
    private final Button registerButton;
    private final Button switchToLoginButton;
    private final Label statusLabel;

    public RegisterScreen() {
        view = new VBox();
        view.setAlignment(Pos.CENTER);
        view.setStyle("-fx-background-color: #2b3339;");

        VBox card = new VBox(14);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(35, 30, 35, 30));
        card.setMaxWidth(360);
        card.setStyle("-fx-background-color: #323c41; -fx-background-radius: 8; -fx-border-color: #445055; -fx-border-radius: 8; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.35), 16, 0.1, 0, 4);");

        javafx.scene.Node logo = IconFactory.cloudIcon(40);

        Label titleLabel = new Label("Create Account");
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 22));
        titleLabel.setStyle("-fx-text-fill: #a7c080;");

        Label subTitle = new Label("Get started with your Mini Cloud Drive");
        subTitle.setStyle("-fx-text-fill: #859289; -fx-font-size: 12px;");

        String inputStyle = "-fx-background-color: #2b3339; -fx-text-fill: #d3c6aa; -fx-prompt-text-fill: #7a8478; -fx-background-radius: 5; -fx-border-color: #445055; -fx-border-radius: 5; -fx-padding: 9 12;";

        usernameField = new TextField();
        usernameField.setPromptText("Choose a Username");
        usernameField.setStyle(inputStyle);
        usernameField.setMaxWidth(300);

        passwordField = new PasswordField();
        passwordField.setPromptText("Create a Password");
        passwordField.setStyle(inputStyle);
        passwordField.setMaxWidth(300);

        confirmPasswordField = new PasswordField();
        confirmPasswordField.setPromptText("Confirm Password");
        confirmPasswordField.setStyle(inputStyle);
        confirmPasswordField.setMaxWidth(300);

        registerButton = new Button("Register");
        registerButton.setStyle("-fx-background-color: #a7c080; -fx-text-fill: #2b3339; -fx-font-weight: bold; -fx-background-radius: 5; -fx-padding: 9 14; -fx-cursor: hand;");
        registerButton.setPrefWidth(300);

        switchToLoginButton = new Button("Already have an account? Sign In");
        switchToLoginButton.setStyle("-fx-background-color: transparent; -fx-text-fill: #7daea3; -fx-cursor: hand; -fx-font-size: 12px;");

        statusLabel = new Label();
        statusLabel.setStyle("-fx-font-size: 12px;");

        card.getChildren().addAll(logo, titleLabel, subTitle, usernameField, passwordField, confirmPasswordField, registerButton, statusLabel, switchToLoginButton);
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
