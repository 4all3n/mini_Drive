package com.minicloud.client.ui;

import com.minicloud.client.models.DriveItem;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 * Main dashboard screen redesigned as a modern split-pane Drive interface.
 * Left: Hierarchical folder tree sidebar
 * Right: Breadcrumbs, search & category filters, action buttons, and drive table
 */
public class DashboardScreen {

    private final BorderPane view;
    private final SplitPane splitPane;
    private final FolderTreePane folderTreePane;
    private final BreadcrumbBar breadcrumbBar;
    private final TableView<DriveItem> table;
    private final ObservableList<DriveItem> masterData;
    private final FilteredList<DriveItem> filteredData;

    private final Button uploadButton;
    private final Button newFolderButton;
    private final Button downloadButton;
    private final Button deleteButton;
    private final Button refreshButton;
    private final Button logoutButton;
    private final TextField searchField;
    private final ComboBox<String> categoryComboBox;
    private final Label storageStatsLabel;

    public static class FileRecord {
        private Long id;
        private String fileName;
        private String fileType;
        private String fileSize;
        private String category;
        private String location;

        public FileRecord(Long id, String fileName, String fileType, String fileSize, String category) {
            this(id, fileName, fileType, fileSize, category, "My Drive");
        }

        public FileRecord(Long id, String fileName, String fileType, String fileSize, String category, String location) {
            this.id = id;
            this.fileName = fileName;
            this.fileType = fileType;
            this.fileSize = fileSize;
            this.category = category;
            this.location = location != null ? location : "My Drive";
        }
        public Long getId() { return id; }
        public String getFileName() { return fileName; }
        public String getFileType() { return fileType; }
        public String getFileSize() { return fileSize; }
        public String getCategory() { return category; }
        public String getLocation() { return location; }
    }

    public DashboardScreen(String username) {
        view = new BorderPane();
        view.setStyle("-fx-background-color: #121212;");

        // 1. TOP NAVIGATION
        HBox topNav = new HBox(15);
        topNav.setPadding(new Insets(12, 20, 12, 20));
        topNav.setStyle("-fx-background-color: #1E1E1E; -fx-border-color: #2D2D2D; -fx-border-width: 0 0 1 0;");
        topNav.setAlignment(Pos.CENTER_LEFT);

        Label brandLabel = new Label("Mini Cloud Drive");
        brandLabel.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        brandLabel.setStyle("-fx-text-fill: #3b82f6;");

        HBox spacer = new HBox();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        storageStatsLabel = new Label("Storage: calculating...");
        storageStatsLabel.setStyle("-fx-text-fill: #888888; -fx-font-size: 12px;");

        Label userLabel = new Label("Welcome, " + username);
        userLabel.setStyle("-fx-text-fill: #e0e0e0; -fx-font-weight: bold;");

        logoutButton = new Button("Logout");
        logoutButton.setStyle("-fx-background-color: transparent; -fx-border-color: #ef4444; -fx-text-fill: #ef4444; -fx-border-radius: 5; -fx-cursor: hand; -fx-padding: 4 12;");

        topNav.getChildren().addAll(brandLabel, spacer, storageStatsLabel, userLabel, logoutButton);
        view.setTop(topNav);

        // 2. FOLDER TREE SIDEBAR (LEFT)
        folderTreePane = new FolderTreePane();

        // 3. MAIN CONTENT (RIGHT)
        VBox rightContent = new VBox(12);
        rightContent.setPadding(new Insets(15, 20, 15, 20));
        rightContent.setStyle("-fx-background-color: #121212;");
        VBox.setVgrow(rightContent, Priority.ALWAYS);

        // Actions & Filters Toolbar
        HBox toolbar = new HBox(10);
        toolbar.setAlignment(Pos.CENTER_LEFT);

        newFolderButton = new Button("📁 + New Folder");
        newFolderButton.setStyle("-fx-background-color: #2563eb; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5; -fx-padding: 7 14; -fx-cursor: hand;");

        uploadButton = new Button("⬆ Upload File");
        uploadButton.setStyle("-fx-background-color: #059669; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5; -fx-padding: 7 14; -fx-cursor: hand;");

        downloadButton = new Button("⬇ Download");
        downloadButton.setStyle("-fx-background-color: #10b981; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5; -fx-padding: 7 14; -fx-cursor: hand;");

        deleteButton = new Button("🗑 Delete");
        deleteButton.setStyle("-fx-background-color: transparent; -fx-border-color: #ef4444; -fx-text-fill: #ef4444; -fx-border-radius: 5; -fx-cursor: hand; -fx-padding: 6 12;");

        refreshButton = new Button("🔄 Refresh");
        refreshButton.setStyle("-fx-background-color: #333333; -fx-text-fill: #e0e0e0; -fx-border-radius: 5; -fx-cursor: hand; -fx-padding: 7 12;");

        HBox toolSpacer = new HBox();
        HBox.setHgrow(toolSpacer, Priority.ALWAYS);

        // Instant Search Box
        searchField = new TextField();
        searchField.setPromptText("🔍 Search files & folders...");
        searchField.setPrefWidth(200);
        searchField.setStyle("-fx-background-color: #1E1E1E; -fx-text-fill: white; -fx-border-color: #2D2D2D; -fx-border-radius: 5; -fx-padding: 6 10;");

        // Category Filter
        categoryComboBox = new ComboBox<>();
        categoryComboBox.getItems().addAll("All Categories", "Documents", "Code", "Media", "Archives", "Uncategorized", "Folder");
        categoryComboBox.setValue("All Categories");
        categoryComboBox.setStyle("-fx-background-color: #1E1E1E; -fx-text-fill: white; -fx-border-color: #2D2D2D; -fx-border-radius: 5;");

        toolbar.getChildren().addAll(newFolderButton, uploadButton, downloadButton, deleteButton, refreshButton, toolSpacer, searchField, categoryComboBox);

        // Breadcrumb Navigation Bar
        breadcrumbBar = new BreadcrumbBar();

        // Central Drive Table
        masterData = FXCollections.observableArrayList();
        filteredData = new FilteredList<>(masterData, p -> true);

        table = new TableView<>();
        table.setStyle("-fx-control-inner-background: #1E1E1E; -fx-background-color: #1E1E1E; -fx-table-cell-border-color: #2D2D2D; -fx-text-fill: white; -fx-border-color: #2D2D2D; -fx-border-radius: 5;");
        VBox.setVgrow(table, Priority.ALWAYS);

        TableColumn<DriveItem, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("displayName"));
        nameCol.setPrefWidth(260);

        TableColumn<DriveItem, String> locCol = new TableColumn<>("Location");
        locCol.setCellValueFactory(new PropertyValueFactory<>("location"));
        locCol.setPrefWidth(160);
        locCol.setStyle("-fx-text-fill: #9ca3af;");

        TableColumn<DriveItem, String> typeCol = new TableColumn<>("Type");
        typeCol.setCellValueFactory(new PropertyValueFactory<>("type"));
        typeCol.setPrefWidth(100);

        TableColumn<DriveItem, String> sizeCol = new TableColumn<>("Size");
        sizeCol.setCellValueFactory(new PropertyValueFactory<>("size"));
        sizeCol.setPrefWidth(90);

        TableColumn<DriveItem, String> catCol = new TableColumn<>("AI Category");
        catCol.setCellValueFactory(new PropertyValueFactory<>("category"));
        catCol.setPrefWidth(130);
        catCol.setStyle("-fx-text-fill: #10b981; -fx-font-weight: bold;");

        table.getColumns().addAll(nameCol, locCol, typeCol, sizeCol, catCol);

        // Connect Category Filter to FilteredList
        categoryComboBox.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());

        SortedList<DriveItem> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(table.comparatorProperty());
        table.setItems(sortedData);

        rightContent.getChildren().addAll(toolbar, breadcrumbBar.getContainer(), table);

        // 4. SPLIT PANE INTEGRATION
        splitPane = new SplitPane();
        splitPane.getItems().addAll(folderTreePane.getContainer(), rightContent);
        splitPane.setDividerPositions(0.24);
        splitPane.setStyle("-fx-background-color: #121212; -fx-box-border: transparent;");

        view.setCenter(splitPane);
    }

    public void applyFilters() {
        String selectedCat = categoryComboBox.getValue();

        filteredData.setPredicate(item -> {
            boolean matchesCategory = true;
            if (selectedCat != null && !selectedCat.equals("All Categories")) {
                if (selectedCat.equals("Folder")) {
                    matchesCategory = item.isFolder();
                } else {
                    matchesCategory = !item.isFolder() && selectedCat.equalsIgnoreCase(item.getCategory());
                }
            }
            return matchesCategory;
        });
    }

    public BorderPane getView() { return view; }
    public FolderTreePane getFolderTreePane() { return folderTreePane; }
    public BreadcrumbBar getBreadcrumbBar() { return breadcrumbBar; }
    public TableView<DriveItem> getTable() { return table; }
    public ObservableList<DriveItem> getMasterData() { return masterData; }

    public Button getUploadButton() { return uploadButton; }
    public Button getNewFolderButton() { return newFolderButton; }
    public Button getDownloadButton() { return downloadButton; }
    public Button getDeleteButton() { return deleteButton; }
    public Button getRefreshButton() { return refreshButton; }
    public Button getLogoutButton() { return logoutButton; }
    public TextField getSearchField() { return searchField; }
    public ComboBox<String> getCategoryComboBox() { return categoryComboBox; }
    public Label getStorageStatsLabel() { return storageStatsLabel; }
}