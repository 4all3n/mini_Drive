package com.minicloud.client;

import com.minicloud.client.controllers.SceneManager;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {
    static {
        // Enforce GTK 3 for JavaFX on Linux to ensure dark mode integration and modern dialog support
        System.setProperty("jdk.gtk.version", "3");
    }

    @Override
    public void start(Stage primaryStage) {
        SceneManager sceneManager = new SceneManager(primaryStage);
        // Show login screen first
        sceneManager.showLoginScreen();
        // Show stage
        primaryStage.show();
        // Apply Everforest theme if scene is already created
        Scene scene = primaryStage.getScene();
        if (scene != null) {
            scene.getStylesheets().add(getClass().getResource("/css/everforest.css").toExternalForm());
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
