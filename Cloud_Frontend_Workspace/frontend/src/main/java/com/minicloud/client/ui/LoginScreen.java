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
        // Main Background
        view = new VBox(); 
        view.setAlignment(Pos.CENTER);
        view.setStyle("-fx-background-color: #121212;"); // Deep dark background

        // The Login "Card" (Floating surface)
        VBox card = new VBox(20);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(40));
        card.setMaxWidth(350);
        card.setStyle("-fx-background-color: #1E1E1E; -fx-background-radius: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.6), 15, 0, 0, 5);");

        Label titleLabel = new Label("Mini Cloud");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 28));
        titleLabel.setStyle("-fx-text-fill: #ffffff;");

        Label subTitle = new Label("Sign in to continue");
        subTitle.setStyle("-fx-text-fill: #a0a0a0;");

        String inputStyle = "-fx-background-color: #2D2D2D; -fx-text-fill: white; -fx-prompt-text-fill: #888888; -fx-background-radius: 5; -fx-border-color: #444444; -fx-border-radius: 5; -fx-padding: 10;";

        usernameField = new TextField();
        usernameField.setPromptText("Username");
        usernameField.setStyle(inputStyle);
        usernameField.setMaxWidth(280);

        passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        passwordField.setStyle(inputStyle);
        passwordField.setMaxWidth(280);

        loginButton = new Button("Login");
        loginButton.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5; -fx-padding: 10; -fx-cursor: hand;");
        loginButton.setPrefWidth(280);

        switchToRegisterButton = new Button("Create an account");
        switchToRegisterButton.setStyle("-fx-background-color: transparent; -fx-text-fill: #3b82f6; -fx-cursor: hand;");

        statusLabel = new Label(); 

        card.getChildren().addAll(titleLabel, subTitle, usernameField, passwordField, loginButton, statusLabel, switchToRegisterButton);
        view.getChildren().add(card);
    }

    public VBox getView() { return view; }
    public Button getLoginButton() { return loginButton; }
    public Button getSwitchToRegisterButton() { return switchToRegisterButton; }
    public TextField getUsernameField() { return usernameField; }
    public PasswordField getPasswordField() { return passwordField; }
    public Label getStatusLabel() { return statusLabel; }
}