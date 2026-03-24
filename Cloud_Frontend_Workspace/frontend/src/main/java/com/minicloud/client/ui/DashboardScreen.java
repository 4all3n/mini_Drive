package com.minicloud.client.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class DashboardScreen {

    private BorderPane view;
    private Button uploadButton;
    private Button logoutButton;
    
    // 1. Declare the table at the class level so the whole file can see it
    private TableView<FileRecord> table; 

    public static class FileRecord {
        private String fileName;
        private String fileType;
        private String fileSize;

        public FileRecord(String fileName, String fileType, String fileSize) {
            this.fileName = fileName;
            this.fileType = fileType;
            this.fileSize = fileSize;
        }
        public String getFileName() { return fileName; }
        public String getFileType() { return fileType; }
        public String getFileSize() { return fileSize; }
    }

    public DashboardScreen(String username) {
        view = new BorderPane();
        view.setStyle("-fx-background-color: #121212;");

        // --- TOP NAV BAR ---
        HBox topNav = new HBox(20);
        topNav.setPadding(new Insets(15, 20, 15, 20));
        topNav.setStyle("-fx-background-color: #1E1E1E; -fx-border-color: #2D2D2D; -fx-border-width: 0 0 1 0;");
        topNav.setAlignment(Pos.CENTER_LEFT);

        Label brandLabel = new Label("Mini Cloud");
        brandLabel.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        brandLabel.setStyle("-fx-text-fill: #ffffff;");

        Label userLabel = new Label("Welcome, " + username);
        userLabel.setStyle("-fx-text-fill: #a0a0a0; -fx-padding: 0 0 0 200;"); 

        logoutButton = new Button("Logout");
        logoutButton.setStyle("-fx-background-color: transparent; -fx-border-color: #ef4444; -fx-text-fill: #ef4444; -fx-border-radius: 5; -fx-cursor: hand;");

        topNav.getChildren().addAll(brandLabel, userLabel, logoutButton);
        view.setTop(topNav);

        // --- MAIN CONTENT ---
        VBox mainContent = new VBox(20);
        mainContent.setPadding(new Insets(30));

        HBox headerBox = new HBox(20);
        headerBox.setAlignment(Pos.CENTER_LEFT);
        
        Label sectionTitle = new Label("My Files");
        sectionTitle.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        sectionTitle.setStyle("-fx-text-fill: #ffffff;");

        uploadButton = new Button("+ Upload File");
        uploadButton.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5; -fx-padding: 8 15; -fx-cursor: hand;");

        headerBox.getChildren().addAll(sectionTitle, uploadButton);

        // 2. Initialize the class-level variable here (removed 'TableView<FileRecord>' from the start)
        table = new TableView<>(); 
        table.setStyle("-fx-control-inner-background: #1E1E1E; -fx-background-color: #1E1E1E; -fx-table-cell-border-color: #2D2D2D; -fx-text-fill: white; -fx-border-color: #2D2D2D; -fx-border-radius: 5;");
        
        TableColumn<FileRecord, String> nameCol = new TableColumn<>("File Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("fileName"));
        nameCol.setPrefWidth(300);

        TableColumn<FileRecord, String> typeCol = new TableColumn<>("Type");
        typeCol.setCellValueFactory(new PropertyValueFactory<>("fileType"));
        typeCol.setPrefWidth(120);

        TableColumn<FileRecord, String> sizeCol = new TableColumn<>("Size");
        sizeCol.setCellValueFactory(new PropertyValueFactory<>("fileSize"));
        sizeCol.setPrefWidth(120);

        table.getColumns().addAll(nameCol, typeCol, sizeCol);

        mainContent.getChildren().addAll(headerBox, table);
        view.setCenter(mainContent);
    }

    public BorderPane getView() { return view; }
    public Button getLogoutButton() { return logoutButton; }
    public Button getUploadButton() { return uploadButton; }
    
    // 3. Add the getter so SceneManager can inject the file data
    public TableView<FileRecord> getTable() { return table; } 
}