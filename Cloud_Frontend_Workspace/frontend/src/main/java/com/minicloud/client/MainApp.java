package com.minicloud.client;

import com.minicloud.client.controllers.SceneManager;
import javafx.application.Application;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        // 1. Initialize the SceneManager and give it the primary window
        SceneManager sceneManager = new SceneManager(primaryStage);
        
        // 2. Tell the manager to show the login screen first
        sceneManager.showLoginScreen();
        
        // 3. Display the window
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}