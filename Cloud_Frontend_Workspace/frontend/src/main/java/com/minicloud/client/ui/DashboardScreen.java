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
    private Button downloadButton;
    private Button deleteButton;
    private Button logoutButton;
    private TableView<FileRecord> table;

    public static class FileRecord {
        private Long id;
        private String fileName;
        private String fileType;
        private String fileSize;
        private String category; // NEW FIELD

        public FileRecord(Long id, String fileName, String fileType, String fileSize, String category) {
            this.id = id;
            this.fileName = fileName;
            this.fileType = fileType;
            this.fileSize = fileSize;
            this.category = category;
        }
        public Long getId() { return id; }
        public String getFileName() { return fileName; }
        public String getFileType() { return fileType; }
        public String getFileSize() { return fileSize; }
        public String getCategory() { return category; } // NEW GETTER
    }

    public DashboardScreen(String username) {
        view = new BorderPane();
        view.setStyle("-fx-background-color: #121212;");

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

        VBox mainContent = new VBox(20);
        mainContent.setPadding(new Insets(30));

        HBox headerBox = new HBox(20);
        headerBox.setAlignment(Pos.CENTER_LEFT);
        
        Label sectionTitle = new Label("My Files");
        sectionTitle.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        sectionTitle.setStyle("-fx-text-fill: #ffffff;");

        uploadButton = new Button("+ Upload File");
        uploadButton.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5; -fx-padding: 8 15; -fx-cursor: hand;");

        downloadButton = new Button("Download Selected");
        downloadButton.setStyle("-fx-background-color: #10b981; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5; -fx-padding: 8 15; -fx-cursor: hand;");
        
        deleteButton = new Button("Delete Selected");
        deleteButton.setStyle("-fx-background-color: transparent; -fx-border-color: #ef4444; -fx-text-fill: #ef4444; -fx-border-radius: 5; -fx-cursor: hand;");

        headerBox.getChildren().addAll(sectionTitle, uploadButton, downloadButton, deleteButton);

        table = new TableView<>();
        table.setStyle("-fx-control-inner-background: #1E1E1E; -fx-background-color: #1E1E1E; -fx-table-cell-border-color: #2D2D2D; -fx-text-fill: white; -fx-border-color: #2D2D2D; -fx-border-radius: 5;");
        
        TableColumn<FileRecord, String> nameCol = new TableColumn<>("File Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("fileName"));
        nameCol.setPrefWidth(250);

        TableColumn<FileRecord, String> typeCol = new TableColumn<>("Type");
        typeCol.setCellValueFactory(new PropertyValueFactory<>("fileType"));
        typeCol.setPrefWidth(100);

        TableColumn<FileRecord, String> sizeCol = new TableColumn<>("Size");
        sizeCol.setCellValueFactory(new PropertyValueFactory<>("fileSize"));
        sizeCol.setPrefWidth(100);

        // --- NEW AI CATEGORY COLUMN ---
        TableColumn<FileRecord, String> catCol = new TableColumn<>("AI Category");
        catCol.setCellValueFactory(new PropertyValueFactory<>("category"));
        catCol.setPrefWidth(150);
        catCol.setStyle("-fx-text-fill: #10b981; -fx-font-weight: bold;"); 

        table.getColumns().addAll(nameCol, typeCol, sizeCol, catCol);

        mainContent.getChildren().addAll(headerBox, table);
        view.setCenter(mainContent);
    }

    public BorderPane getView() { return view; }
    public Button getLogoutButton() { return logoutButton; }
    public Button getUploadButton() { return uploadButton; }
    public Button getDownloadButton() { return downloadButton; }
    public Button getDeleteButton() { return deleteButton; }
    public TableView<FileRecord> getTable() { return table; }
}