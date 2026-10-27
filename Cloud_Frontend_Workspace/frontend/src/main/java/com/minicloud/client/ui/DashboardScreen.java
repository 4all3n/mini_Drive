package com.minicloud.client.ui;

import com.minicloud.client.models.DriveItem;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Main dashboard screen redesigned as a modern split-pane Drive interface.
 * Left: Hierarchical folder tree sidebar
 * Right: Breadcrumbs, search & category filters, action buttons, and dual view (Grid / Table)
 */
public class DashboardScreen {

    private final BorderPane view;
    private final SplitPane splitPane;
    private final FolderTreePane folderTreePane;
    private final BreadcrumbBar breadcrumbBar;
    private final TableView<DriveItem> table;
    private final DriveGridView driveGridView;
    private final StackPane viewStack;
    private final ObservableList<DriveItem> masterData;
    private final FilteredList<DriveItem> filteredData;

    private final Button uploadButton;
    private final Button newFolderButton;
    private final Button downloadButton;
    private final Button deleteButton;
    private final Button refreshButton;
    private final Button logoutButton;
    private final Button storageAnalyticsButton;
    private final Button gridViewButton;
    private final Button listViewButton;
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

        // 1. TOP NAVIGATION
        HBox topNav = new HBox(15);
        topNav.setPadding(new Insets(10, 20, 10, 20));
        topNav.setAlignment(Pos.CENTER_LEFT);
        topNav.getStyleClass().add("top-nav");

        Node cloudIcon = IconFactory.cloudIcon(20);
        Label brandLabel = new Label("Mini Cloud Drive");
        brandLabel.getStyleClass().add("brand-label");

        HBox brandBox = new HBox(8, cloudIcon, brandLabel);
        brandBox.setAlignment(Pos.CENTER_LEFT);

        HBox spacer = new HBox();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        storageAnalyticsButton = new Button("Storage Analytics");
        storageAnalyticsButton.setGraphic(IconFactory.chartIcon(14));
        storageAnalyticsButton.setTooltip(new Tooltip("View storage breakdown & quota usage"));

        storageStatsLabel = new Label("Storage: calculating...");
        storageStatsLabel.getStyleClass().add("storage-label");
        storageStatsLabel.setCursor(Cursor.HAND);
        storageStatsLabel.setTooltip(new Tooltip("Click to view detailed storage analytics and quota distribution"));

        Label userLabel = new Label("Welcome, " + username);
        userLabel.setStyle("-fx-font-weight: bold;");

        logoutButton = new Button("Logout");
        logoutButton.setGraphic(IconFactory.logoutIcon(14));
        logoutButton.getStyleClass().add("btn-danger");

        topNav.getChildren().addAll(brandBox, spacer, storageAnalyticsButton, storageStatsLabel, userLabel, logoutButton);
        view.setTop(topNav);

        // 2. FOLDER TREE SIDEBAR (LEFT)
        folderTreePane = new FolderTreePane();

        // 3. MAIN CONTENT (RIGHT)
        VBox rightContent = new VBox(10);
        rightContent.setPadding(new Insets(12, 16, 12, 16));
        VBox.setVgrow(rightContent, Priority.ALWAYS);

        // Actions & Filters Toolbar
        HBox toolbar = new HBox(8);
        toolbar.setAlignment(Pos.CENTER_LEFT);

        newFolderButton = new Button("New Folder");
        newFolderButton.setGraphic(IconFactory.newFolderIcon(14));
        newFolderButton.getStyleClass().add("btn-primary");

        uploadButton = new Button("Upload");
        uploadButton.setGraphic(IconFactory.uploadIcon(14));
        uploadButton.getStyleClass().add("btn-success");

        downloadButton = new Button("Download");
        downloadButton.setGraphic(IconFactory.downloadIcon(14));

        deleteButton = new Button("Delete");
        deleteButton.setGraphic(IconFactory.deleteIcon(14));
        deleteButton.getStyleClass().add("btn-danger");

        refreshButton = new Button("Refresh");
        refreshButton.setGraphic(IconFactory.refreshIcon(14));

        HBox toolSpacer = new HBox();
        HBox.setHgrow(toolSpacer, Priority.ALWAYS);

        // Instant Search Box
        searchField = new TextField();
        searchField.setPromptText("Search files & folders...");
        searchField.setPrefWidth(200);

        // Category Filter
        categoryComboBox = new ComboBox<>();
        categoryComboBox.getItems().addAll("All Categories", "Documents", "Code", "Media", "Archives", "Executables", "System & Logs", "Uncategorized", "Folder");
        categoryComboBox.setValue("All Categories");

        // View Mode Toggle (Grid View ⊞ vs List View ☰)
        gridViewButton = new Button();
        gridViewButton.setGraphic(IconFactory.gridIcon(14));
        gridViewButton.setTooltip(new Tooltip("Grid View"));
        gridViewButton.getStyleClass().add("btn-primary"); // Active by default

        listViewButton = new Button();
        listViewButton.setGraphic(IconFactory.listIcon(14));
        listViewButton.setTooltip(new Tooltip("List View"));

        HBox viewToggleBox = new HBox(2, gridViewButton, listViewButton);
        viewToggleBox.setAlignment(Pos.CENTER);

        toolbar.getChildren().addAll(
                newFolderButton, uploadButton, downloadButton, deleteButton, refreshButton,
                toolSpacer, searchField, categoryComboBox, viewToggleBox
        );

        // Breadcrumb Navigation Bar
        breadcrumbBar = new BreadcrumbBar();

        // Data lists
        masterData = FXCollections.observableArrayList();
        filteredData = new FilteredList<>(masterData, p -> true);

        // View Mode 1: Central Drive Table (List View)
        table = new TableView<>();
        VBox.setVgrow(table, Priority.ALWAYS);

        TableColumn<DriveItem, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("displayName"));
        nameCol.setPrefWidth(280);
        nameCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(item);
                    DriveItem driveItem = getTableRow() != null ? getTableRow().getItem() : null;
                    if (driveItem == null && getIndex() >= 0 && getIndex() < getTableView().getItems().size()) {
                        driveItem = getTableView().getItems().get(getIndex());
                    }
                    if (driveItem != null) {
                        setGraphic(IconFactory.iconForType(driveItem.getName(), driveItem.getType(), driveItem.isFolder(), 16));
                    } else {
                        setGraphic(null);
                    }
                    setGraphicTextGap(10);
                    setAlignment(Pos.CENTER_LEFT);
                }
            }
        });

        TableColumn<DriveItem, String> locCol = new TableColumn<>("Location");
        locCol.setCellValueFactory(new PropertyValueFactory<>("location"));
        locCol.setPrefWidth(160);
        locCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item);
                    setStyle("-fx-text-fill: #859289; -fx-alignment: CENTER-LEFT;");
                }
            }
        });

        TableColumn<DriveItem, String> typeCol = new TableColumn<>("Type");
        typeCol.setCellValueFactory(new PropertyValueFactory<>("type"));
        typeCol.setPrefWidth(110);

        TableColumn<DriveItem, String> sizeCol = new TableColumn<>("Size");
        sizeCol.setCellValueFactory(new PropertyValueFactory<>("size"));
        sizeCol.setPrefWidth(90);

        TableColumn<DriveItem, String> catCol = new TableColumn<>("AI Category");
        catCol.setCellValueFactory(new PropertyValueFactory<>("category"));
        catCol.setPrefWidth(140);
        catCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    if ("Folder".equalsIgnoreCase(item)) {
                        setStyle("-fx-text-fill: #7a8478; -fx-alignment: CENTER-LEFT;");
                    } else {
                        setStyle("-fx-text-fill: #83c092; -fx-font-weight: bold; -fx-alignment: CENTER-LEFT;");
                    }
                }
            }
        });

        table.getColumns().addAll(nameCol, locCol, typeCol, sizeCol, catCol);

        SortedList<DriveItem> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(table.comparatorProperty());
        table.setItems(sortedData);

        // View Mode 2: Grid View (Large Icons like GNOME Files / Windows Explorer)
        driveGridView = new DriveGridView(filteredData);
        VBox.setVgrow(driveGridView.getContainer(), Priority.ALWAYS);

        // Synchronize selection between Table and Grid View
        table.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && newVal != driveGridView.getSelectedItem()) {
                driveGridView.setSelectedItem(newVal);
            }
        });

        driveGridView.setOnItemSelected(item -> {
            if (item != null && item != table.getSelectionModel().getSelectedItem()) {
                table.getSelectionModel().select(item);
            }
        });

        viewStack = new StackPane(table, driveGridView.getContainer());
        VBox.setVgrow(viewStack, Priority.ALWAYS);

        // Default to Grid View (as requested)
        setViewMode(true);

        gridViewButton.setOnAction(e -> setViewMode(true));
        listViewButton.setOnAction(e -> setViewMode(false));

        // Connect Category Filter to FilteredList
        categoryComboBox.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());

        rightContent.getChildren().addAll(toolbar, breadcrumbBar.getContainer(), viewStack);

        // 4. SPLIT PANE INTEGRATION
        splitPane = new SplitPane();
        splitPane.getItems().addAll(folderTreePane.getContainer(), rightContent);
        splitPane.setDividerPositions(0.22);

        view.setCenter(splitPane);
    }

    public void setViewMode(boolean isGrid) {
        if (isGrid) {
            driveGridView.getContainer().setVisible(true);
            driveGridView.getContainer().setManaged(true);
            table.setVisible(false);
            table.setManaged(false);

            if (!gridViewButton.getStyleClass().contains("btn-primary")) {
                gridViewButton.getStyleClass().add("btn-primary");
            }
            listViewButton.getStyleClass().remove("btn-primary");
        } else {
            driveGridView.getContainer().setVisible(false);
            driveGridView.getContainer().setManaged(false);
            table.setVisible(true);
            table.setManaged(true);

            if (!listViewButton.getStyleClass().contains("btn-primary")) {
                listViewButton.getStyleClass().add("btn-primary");
            }
            gridViewButton.getStyleClass().remove("btn-primary");
        }
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
    public DriveGridView getDriveGridView() { return driveGridView; }
    public ObservableList<DriveItem> getMasterData() { return masterData; }

    public Button getUploadButton() { return uploadButton; }
    public Button getNewFolderButton() { return newFolderButton; }
    public Button getDownloadButton() { return downloadButton; }
    public Button getDeleteButton() { return deleteButton; }
    public Button getRefreshButton() { return refreshButton; }
    public Button getLogoutButton() { return logoutButton; }
    public Button getStorageAnalyticsButton() { return storageAnalyticsButton; }
    public TextField getSearchField() { return searchField; }
    public ComboBox<String> getCategoryComboBox() { return categoryComboBox; }
    public Label getStorageStatsLabel() { return storageStatsLabel; }
}
