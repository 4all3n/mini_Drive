package com.minicloud.client;

import com.minicloud.client.controllers.SceneManager;
import javafx.application.Application;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        SceneManager sceneManager = new SceneManager(primaryStage);
        
        sceneManager.showLoginScreen();
        
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}