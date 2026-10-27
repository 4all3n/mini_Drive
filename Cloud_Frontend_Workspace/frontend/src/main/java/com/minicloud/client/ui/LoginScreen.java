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
 * Modern login screen styled with Everforest dark theme.
 */
public class LoginScreen {

    private final VBox view;
    private final TextField usernameField;
    private final PasswordField passwordField;
    private final Button loginButton;
    private final Button switchToRegisterButton;
    private final Label statusLabel;

    public LoginScreen() {
        view = new VBox();
        view.setAlignment(Pos.CENTER);
        view.setStyle("-fx-background-color: #2b3339;");

        // The Login "Card" (Floating surface)
        VBox card = new VBox(16);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(35, 30, 35, 30));
        card.setMaxWidth(360);
        card.setStyle("-fx-background-color: #323c41; -fx-background-radius: 8; -fx-border-color: #445055; -fx-border-radius: 8; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.35), 16, 0.1, 0, 4);");

        // Cloud brand icon + title
        javafx.scene.Node logo = IconFactory.cloudIcon(40);

        Label titleLabel = new Label("Mini Cloud");
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
        titleLabel.setStyle("-fx-text-fill: #a7c080;");

        Label subTitle = new Label("Sign in to your intelligent drive");
        subTitle.setStyle("-fx-text-fill: #859289; -fx-font-size: 12px;");

        String inputStyle = "-fx-background-color: #2b3339; -fx-text-fill: #d3c6aa; -fx-prompt-text-fill: #7a8478; -fx-background-radius: 5; -fx-border-color: #445055; -fx-border-radius: 5; -fx-padding: 9 12;";

        usernameField = new TextField();
        usernameField.setPromptText("Username");
        usernameField.setStyle(inputStyle);
        usernameField.setMaxWidth(300);

        passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        passwordField.setStyle(inputStyle);
        passwordField.setMaxWidth(300);

        loginButton = new Button("Sign In");
        loginButton.setStyle("-fx-background-color: #7daea3; -fx-text-fill: #2b3339; -fx-font-weight: bold; -fx-background-radius: 5; -fx-padding: 9 14; -fx-cursor: hand;");
        loginButton.setPrefWidth(300);

        switchToRegisterButton = new Button("Don't have an account? Register");
        switchToRegisterButton.setStyle("-fx-background-color: transparent; -fx-text-fill: #7daea3; -fx-cursor: hand; -fx-font-size: 12px;");

        statusLabel = new Label();
        statusLabel.setStyle("-fx-font-size: 12px;");

        card.getChildren().addAll(logo, titleLabel, subTitle, usernameField, passwordField, loginButton, statusLabel, switchToRegisterButton);
        view.getChildren().add(card);
    }

    public VBox getView() { return view; }
    public Button getLoginButton() { return loginButton; }
    public Button getSwitchToRegisterButton() { return switchToRegisterButton; }
    public TextField getUsernameField() { return usernameField; }
    public PasswordField getPasswordField() { return passwordField; }
    public Label getStatusLabel() { return statusLabel; }
}
