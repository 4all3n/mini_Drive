package com.minicloud.client.ui.dialogs;

import com.minicloud.client.ui.IconFactory;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;
import javafx.scene.text.TextAlignment;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 100% Everforest Dark Themed In-App File & Folder Picker Dialog.
 * Replaces native unthemed OS GTK dialogs with an integrated Everforest UI:
 * - Quick Places sidebar (Home, Desktop, Documents, Downloads, Pictures, Music, Videos, Root)
 * - Navigation history (Back, Forward, Up, Home)
 * - Path breadcrumb / direct path editing
 * - Dual view (Grid view with large Everforest icons & Detailed Table view)
 * - File type filtering & live search
 * - Single/Multiple file selection and folder selection mode
 * - Fallback button for native system chooser
 */
public class FilePickerDialog extends Stage {

    public enum Mode {
        OPEN_FILE,
        OPEN_MULTIPLE_FILES,
        SELECT_FOLDER
    }

    public static class FileItem {
        private final File file;
        private final String name;
        private final boolean directory;
        private final long length;
        private final long lastModified;
        private final String formattedSize;
        private final String formattedDate;
        private final String type;

        private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm");

        public FileItem(File file) {
            this.file = file;
            String n = file.getName();
            this.name = (n == null || n.isEmpty()) ? file.getAbsolutePath() : n;
            this.directory = file.isDirectory();
            this.length = this.directory ? 0 : file.length();
            this.lastModified = file.lastModified();
            this.formattedSize = this.directory ? "--" : formatFileSize(this.length);
            this.formattedDate = DATE_FORMAT.format(new Date(this.lastModified));
            this.type = this.directory ? "Folder" : extractExtension(this.name).toUpperCase() + " File";
        }

        public File getFile() { return file; }
        public String getName() { return name; }
        public boolean isDirectory() { return directory; }
        public long getLength() { return length; }
        public long getLastModified() { return lastModified; }
        public String getFormattedSize() { return formattedSize; }
        public String getFormattedDate() { return formattedDate; }
        public String getType() { return type; }

        private static String formatFileSize(long bytes) {
            if (bytes < 1024) return bytes + " B";
            int exp = (int) (Math.log(bytes) / Math.log(1024));
            char pre = "KMGTPE".charAt(exp - 1);
            return String.format(Locale.US, "%.1f %cB", bytes / Math.pow(1024, exp), pre);
        }

        private static String extractExtension(String name) {
            int dot = name.lastIndexOf('.');
            return (dot > 0 && dot < name.length() - 1) ? name.substring(dot + 1) : "";
        }
    }

    private final Mode mode;
    private File currentDirectory;
    private final Stack<File> backHistory = new Stack<>();
    private final Stack<File> forwardHistory = new Stack<>();

    private final ObservableList<FileItem> masterItems = FXCollections.observableArrayList();
    private final FilteredList<FileItem> filteredItems = new FilteredList<>(masterItems, p -> true);

    private final TextField pathField = new TextField();
    private final TextField searchField = new TextField();
    private final Button backButton = new Button();
    private final Button forwardButton = new Button();
    private final Button upButton = new Button();
    private final Button homeButton = new Button();
    private final ToggleButton showHiddenBtn = new ToggleButton("Hidden");
    private final Button gridViewBtn = new Button();
    private final Button listViewBtn = new Button();

    private final TableView<FileItem> tableView = new TableView<>();
    private final ScrollPane gridScrollPane = new ScrollPane();
    private final FlowPane gridFlowPane = new FlowPane();
    private final StackPane viewStack = new StackPane();

    private final Label selectionLabel = new Label("No selection");
    private final ComboBox<String> filterComboBox = new ComboBox<>();
    private final Button confirmButton = new Button("Select");
    private final Button cancelButton = new Button("Cancel");
    private final Button systemChooserBtn = new Button("System Chooser...");

    private final List<File> selectedFilesResult = new ArrayList<>();
    private final Map<FileItem, VBox> cardMap = new HashMap<>();
    private FileItem singleSelectedGridItem = null;

    public FilePickerDialog(Window owner, String title, Mode mode, File initialDirectory) {
        this.mode = mode;
        initModality(Modality.APPLICATION_MODAL);
        if (owner != null) {
            initOwner(owner);
        }
        setTitle(title != null ? title : (mode == Mode.SELECT_FOLDER ? "Select Folder" : "Select File"));

        // Determine starting directory
        if (initialDirectory != null && initialDirectory.exists() && initialDirectory.isDirectory()) {
            this.currentDirectory = initialDirectory;
        } else {
            String userHome = System.getProperty("user.home");
            File homeDir = new File(userHome);
            this.currentDirectory = homeDir.exists() ? homeDir : new File("/");
        }

        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #2b3339;");

        // 1. TOP HEADER & NAVIGATION BAR
        VBox topBox = new VBox(8);
        topBox.setPadding(new Insets(12, 16, 10, 16));
        topBox.setStyle("-fx-background-color: #323c41; -fx-border-color: #445055; -fx-border-width: 0 0 1 0;");

        HBox headerLine = new HBox(10);
        headerLine.setAlignment(Pos.CENTER_LEFT);
        Region headerIcon = mode == Mode.SELECT_FOLDER ? IconFactory.folderIcon(20) : IconFactory.uploadIcon(20);
        Label headerTitle = new Label(getTitle());
        headerTitle.setStyle("-fx-text-fill: #a7c080; -fx-font-weight: bold; -fx-font-size: 15px;");
        headerLine.getChildren().addAll(headerIcon, headerTitle);

        HBox navBar = new HBox(8);
        navBar.setAlignment(Pos.CENTER_LEFT);

        backButton.setGraphic(IconFactory.arrowBackIcon(14));
        backButton.setTooltip(new Tooltip("Back"));
        backButton.setOnAction(e -> navigateBack());

        forwardButton.setGraphic(IconFactory.arrowForwardIcon(14));
        forwardButton.setTooltip(new Tooltip("Forward"));
        forwardButton.setOnAction(e -> navigateForward());

        upButton.setGraphic(IconFactory.arrowUpIcon(14));
        upButton.setTooltip(new Tooltip("Up one level"));
        upButton.setOnAction(e -> navigateUp());

        homeButton.setGraphic(IconFactory.homeIcon(14));
        homeButton.setTooltip(new Tooltip("Home"));
        homeButton.setOnAction(e -> navigateTo(new File(System.getProperty("user.home")), true));

        pathField.setStyle("-fx-background-color: #2b3339; -fx-text-fill: #d3c6aa; -fx-border-color: #445055; -fx-border-radius: 4; -fx-background-radius: 4; -fx-padding: 5 8;");
        HBox.setHgrow(pathField, Priority.ALWAYS);
        pathField.setOnAction(e -> {
            String text = pathField.getText().trim();
            if (text.startsWith("~")) {
                text = System.getProperty("user.home") + text.substring(1);
            }
            File target = new File(text);
            if (target.exists() && target.isDirectory()) {
                navigateTo(target, true);
            } else {
                pathField.setText(currentDirectory.getAbsolutePath());
            }
        });

        searchField.setPromptText("Filter name...");
        searchField.setPrefWidth(130);
        searchField.setStyle("-fx-background-color: #2b3339; -fx-text-fill: #d3c6aa; -fx-prompt-text-fill: #7a8478; -fx-border-color: #445055; -fx-border-radius: 4; -fx-background-radius: 4; -fx-padding: 5 8;");
        searchField.textProperty().addListener((obs, oldVal, newVal) -> applyFilters());

        showHiddenBtn.setStyle("-fx-background-color: #3a454a; -fx-text-fill: #859289; -fx-font-size: 11px; -fx-padding: 5 9;");
        showHiddenBtn.selectedProperty().addListener((obs, oldVal, newVal) -> {
            showHiddenBtn.setStyle(newVal ? "-fx-background-color: #7daea3; -fx-text-fill: #2b3339; -fx-font-size: 11px; -fx-padding: 5 9;"
                                          : "-fx-background-color: #3a454a; -fx-text-fill: #859289; -fx-font-size: 11px; -fx-padding: 5 9;");
            applyFilters();
        });

        gridViewBtn.setGraphic(IconFactory.gridIcon(14));
        gridViewBtn.setTooltip(new Tooltip("Grid View"));
        gridViewBtn.setOnAction(e -> setViewMode(true));

        listViewBtn.setGraphic(IconFactory.listIcon(14));
        listViewBtn.setTooltip(new Tooltip("List View"));
        listViewBtn.setOnAction(e -> setViewMode(false));

        navBar.getChildren().addAll(backButton, forwardButton, upButton, homeButton, pathField, searchField, showHiddenBtn, gridViewBtn, listViewBtn);
        topBox.getChildren().addAll(headerLine, navBar);
        root.setTop(topBox);

        // 2. LEFT SIDEBAR (PLACES / SHORTCUTS)
        VBox sidebar = new VBox(4);
        sidebar.setPadding(new Insets(12, 8, 12, 8));
        sidebar.setPrefWidth(180);
        sidebar.setStyle("-fx-background-color: #2b3339; -fx-border-color: #445055; -fx-border-width: 0 1 0 0;");

        Label placesLabel = new Label("SHORTCUTS");
        placesLabel.setStyle("-fx-text-fill: #7a8478; -fx-font-size: 11px; -fx-font-weight: bold; -fx-padding: 0 0 6 8;");
        sidebar.getChildren().add(placesLabel);

        String userHome = System.getProperty("user.home");
        addPlaceItem(sidebar, "Home", userHome, IconFactory.homeIcon(16));
        addPlaceItem(sidebar, "Desktop", userHome + "/Desktop", IconFactory.desktopIcon(16));
        addPlaceItem(sidebar, "Documents", userHome + "/Documents", IconFactory.documentsIcon(16));
        addPlaceItem(sidebar, "Downloads", userHome + "/Downloads", IconFactory.downloadsIcon(16));
        addPlaceItem(sidebar, "Pictures", userHome + "/Pictures", IconFactory.picturesIcon(16));
        addPlaceItem(sidebar, "Music", userHome + "/Music", IconFactory.musicIcon(16));
        addPlaceItem(sidebar, "Videos", userHome + "/Videos", IconFactory.videosIcon(16));
        addPlaceItem(sidebar, "File System", "/", IconFactory.driveIcon(16));

        // 3. CENTER VIEW (Dual mode: TableView / GridView)
        setupTableView();
        setupGridView();

        viewStack.getChildren().addAll(tableView, gridScrollPane);
        VBox.setVgrow(viewStack, Priority.ALWAYS);

        SplitPane centerSplit = new SplitPane();
        centerSplit.getItems().addAll(sidebar, viewStack);
        centerSplit.setDividerPositions(0.23);
        root.setCenter(centerSplit);

        // 4. BOTTOM ACTION BAR
        HBox bottomBar = new HBox(12);
        bottomBar.setPadding(new Insets(10, 16, 12, 16));
        bottomBar.setAlignment(Pos.CENTER_LEFT);
        bottomBar.setStyle("-fx-background-color: #323c41; -fx-border-color: #445055; -fx-border-width: 1 0 0 0;");

        selectionLabel.setStyle("-fx-text-fill: #d3c6aa; -fx-font-size: 12px;");
        HBox.setHgrow(selectionLabel, Priority.ALWAYS);

        filterComboBox.getItems().addAll(
                "All Files (*.*)",
                "Documents (*.pdf, *.docx, *.txt, *.md, *.xlsx)",
                "Images (*.png, *.jpg, *.jpeg, *.gif, *.webp, *.svg)",
                "Archives (*.zip, *.tar, *.gz, *.7z)",
                "Code (*.java, *.py, *.js, *.html, *.css, *.json, *.xml)"
        );
        filterComboBox.setValue("All Files (*.*)");
        filterComboBox.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        if (mode == Mode.SELECT_FOLDER) {
            filterComboBox.setVisible(false);
            filterComboBox.setManaged(false);
        }

        systemChooserBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #859289; -fx-font-size: 11px; -fx-underline: true; -fx-cursor: hand;");
        systemChooserBtn.setOnAction(e -> launchSystemChooser(owner));

        confirmButton.setText(mode == Mode.SELECT_FOLDER ? "Select Folder" : "Upload");
        confirmButton.getStyleClass().add("btn-success");
        confirmButton.setOnAction(e -> confirmSelection());

        cancelButton.setText("Cancel");
        cancelButton.setOnAction(e -> close());

        bottomBar.getChildren().addAll(selectionLabel, systemChooserBtn, filterComboBox, confirmButton, cancelButton);
        root.setBottom(bottomBar);

        // Default to Grid View
        setViewMode(true);

        // Load starting directory
        navigateTo(this.currentDirectory, false);

        // Scene configuration
        Scene scene = new Scene(root, 880, 560);
        try {
            scene.getStylesheets().add(getClass().getResource("/css/everforest.css").toExternalForm());
        } catch (Exception ignored) {}

        // Keyboard shortcuts: ESC = close, ENTER = confirm, BACK_SPACE = up
        scene.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ESCAPE) {
                close();
            } else if (event.getCode() == KeyCode.BACK_SPACE && !pathField.isFocused() && !searchField.isFocused()) {
                navigateUp();
            }
        });

        setScene(scene);
    }

    private void addPlaceItem(VBox container, String title, String path, Region icon) {
        File dir = new File(path);
        if (!dir.exists()) return;

        Button btn = new Button(title);
        btn.setGraphic(icon);
        btn.setGraphicTextGap(10);
        btn.setAlignment(Pos.CENTER_LEFT);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setStyle("-fx-background-color: transparent; -fx-text-fill: #d3c6aa; -fx-font-size: 12px; -fx-padding: 7 10; -fx-background-radius: 5; -fx-cursor: hand;");

        btn.setOnMouseEntered(e -> btn.setStyle("-fx-background-color: #323c41; -fx-text-fill: #a7c080; -fx-font-size: 12px; -fx-padding: 7 10; -fx-background-radius: 5; -fx-cursor: hand;"));
        btn.setOnMouseExited(e -> btn.setStyle("-fx-background-color: transparent; -fx-text-fill: #d3c6aa; -fx-font-size: 12px; -fx-padding: 7 10; -fx-background-radius: 5; -fx-cursor: hand;"));
        btn.setOnAction(e -> navigateTo(dir, true));

        container.getChildren().add(btn);
    }

    private void setupTableView() {
        if (mode == Mode.OPEN_MULTIPLE_FILES) {
            tableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        } else {
            tableView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        }

        TableColumn<FileItem, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameCol.setPrefWidth(320);
        nameCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(item);
                    FileItem fi = getTableRow() != null ? getTableRow().getItem() : null;
                    if (fi != null) {
                        setGraphic(IconFactory.iconForType(fi.getName(), fi.getType(), fi.isDirectory(), 16));
                    } else {
                        setGraphic(null);
                    }
                    setGraphicTextGap(10);
                    setAlignment(Pos.CENTER_LEFT);
                }
            }
        });

        TableColumn<FileItem, String> sizeCol = new TableColumn<>("Size");
        sizeCol.setCellValueFactory(new PropertyValueFactory<>("formattedSize"));
        sizeCol.setPrefWidth(90);

        TableColumn<FileItem, String> typeCol = new TableColumn<>("Type");
        typeCol.setCellValueFactory(new PropertyValueFactory<>("type"));
        typeCol.setPrefWidth(120);

        TableColumn<FileItem, String> dateCol = new TableColumn<>("Modified");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("formattedDate"));
        dateCol.setPrefWidth(150);

        tableView.getColumns().addAll(nameCol, sizeCol, typeCol, dateCol);

        SortedList<FileItem> sorted = new SortedList<>(filteredItems);
        sorted.comparatorProperty().bind(tableView.comparatorProperty());
        tableView.setItems(sorted);

        tableView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> updateSelectionFromTable());

        tableView.setRowFactory(tv -> {
            TableRow<FileItem> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (!row.isEmpty())) {
                    FileItem item = row.getItem();
                    if (item != null) {
                        if (item.isDirectory()) {
                            navigateTo(item.getFile(), true);
                        } else if (mode != Mode.SELECT_FOLDER) {
                            confirmSelection();
                        }
                    }
                }
            });
            return row;
        });
    }

    private void setupGridView() {
        gridFlowPane.setHgap(14);
        gridFlowPane.setVgap(14);
        gridFlowPane.setPadding(new Insets(16));
        gridFlowPane.setStyle("-fx-background-color: #2b3339;");

        gridScrollPane.setContent(gridFlowPane);
        gridScrollPane.setFitToWidth(true);
        gridScrollPane.setStyle("-fx-background: #2b3339; -fx-background-color: #2b3339; -fx-border-color: transparent;");

        filteredItems.addListener((javafx.collections.ListChangeListener<FileItem>) c -> renderGrid());
    }

    private void renderGrid() {
        gridFlowPane.getChildren().clear();
        cardMap.clear();

        if (filteredItems.isEmpty()) {
            Label empty = new Label("This directory is empty");
            empty.setStyle("-fx-text-fill: #7a8478; -fx-font-size: 13px; -fx-padding: 30;");
            gridFlowPane.getChildren().add(empty);
            return;
        }

        for (FileItem item : filteredItems) {
            VBox card = createGridCard(item);
            cardMap.put(item, card);
            gridFlowPane.getChildren().add(card);
        }
        updateGridSelectionStyles();
    }

    private VBox createGridCard(FileItem item) {
        VBox card = new VBox(6);
        card.setAlignment(Pos.TOP_CENTER);
        card.setPrefWidth(96);
        card.setMaxWidth(96);
        card.setPrefHeight(108);
        card.setPadding(new Insets(8, 4, 8, 4));

        Region icon = IconFactory.largeIcon(item.getName(), item.getType(), item.isDirectory(), 48);

        Label label = new Label(item.getName());
        label.setWrapText(true);
        label.setMaxWidth(88);
        label.setMaxHeight(34);
        label.setTextAlignment(TextAlignment.CENTER);
        label.setAlignment(Pos.TOP_CENTER);
        label.setStyle("-fx-text-fill: #d3c6aa; -fx-font-size: 11px;");

        card.getChildren().addAll(icon, label);

        card.setOnMouseEntered(e -> {
            if (item != singleSelectedGridItem) {
                card.setStyle("-fx-background-color: #323c41; -fx-background-radius: 8; -fx-cursor: hand;");
            }
        });

        card.setOnMouseExited(e -> {
            if (item != singleSelectedGridItem) {
                card.setStyle("-fx-background-color: transparent; -fx-background-radius: 8;");
            }
        });

        card.setOnMouseClicked(e -> {
            if (e.getButton() == MouseButton.PRIMARY) {
                singleSelectedGridItem = item;
                updateGridSelectionStyles();
                tableView.getSelectionModel().clearSelection();
                tableView.getSelectionModel().select(item);
                updateSelectionStatus(List.of(item));

                if (e.getClickCount() == 2) {
                    if (item.isDirectory()) {
                        navigateTo(item.getFile(), true);
                    } else if (mode != Mode.SELECT_FOLDER) {
                        confirmSelection();
                    }
                }
            }
        });

        return card;
    }

    private void updateGridSelectionStyles() {
        for (Map.Entry<FileItem, VBox> entry : cardMap.entrySet()) {
            FileItem item = entry.getKey();
            VBox card = entry.getValue();
            if (item == singleSelectedGridItem) {
                card.setStyle("-fx-background-color: #3a4145; -fx-border-color: #7daea3; -fx-border-width: 1.5; -fx-border-radius: 8; -fx-background-radius: 8;");
            } else {
                card.setStyle("-fx-background-color: transparent; -fx-background-radius: 8;");
            }
        }
    }

    private void setViewMode(boolean isGrid) {
        if (isGrid) {
            gridScrollPane.setVisible(true);
            gridScrollPane.setManaged(true);
            tableView.setVisible(false);
            tableView.setManaged(false);
            if (!gridViewBtn.getStyleClass().contains("btn-primary")) {
                gridViewBtn.getStyleClass().add("btn-primary");
            }
            listViewBtn.getStyleClass().remove("btn-primary");
        } else {
            gridScrollPane.setVisible(false);
            gridScrollPane.setManaged(false);
            tableView.setVisible(true);
            tableView.setManaged(true);
            if (!listViewBtn.getStyleClass().contains("btn-primary")) {
                listViewBtn.getStyleClass().add("btn-primary");
            }
            gridViewBtn.getStyleClass().remove("btn-primary");
        }
    }

    public void navigateTo(File directory, boolean recordHistory) {
        if (directory == null || !directory.exists() || !directory.isDirectory()) return;

        if (recordHistory && currentDirectory != null && !currentDirectory.equals(directory)) {
            backHistory.push(currentDirectory);
            forwardHistory.clear();
        }

        this.currentDirectory = directory;
        pathField.setText(directory.getAbsolutePath());
        backButton.setDisable(backHistory.isEmpty());
        forwardButton.setDisable(forwardHistory.isEmpty());
        upButton.setDisable(directory.getParentFile() == null);

        // Scan files
        List<FileItem> items = new ArrayList<>();
        File[] files = directory.listFiles();
        if (files != null) {
            // Sort: folders first, then alphabetically
            Arrays.sort(files, (a, b) -> {
                if (a.isDirectory() && !b.isDirectory()) return -1;
                if (!a.isDirectory() && b.isDirectory()) return 1;
                return a.getName().compareToIgnoreCase(b.getName());
            });

            for (File f : files) {
                if (mode == Mode.SELECT_FOLDER && !f.isDirectory()) {
                    continue; // In folder select mode, only display folders
                }
                items.add(new FileItem(f));
            }
        }

        masterItems.setAll(items);
        singleSelectedGridItem = null;
        tableView.getSelectionModel().clearSelection();

        if (mode == Mode.SELECT_FOLDER) {
            selectionLabel.setText("Current folder: " + currentDirectory.getName());
            confirmButton.setDisable(false);
        } else {
            selectionLabel.setText("No selection");
            confirmButton.setDisable(true);
        }

        renderGrid();
    }

    private void navigateBack() {
        if (!backHistory.isEmpty()) {
            forwardHistory.push(currentDirectory);
            File prev = backHistory.pop();
            navigateTo(prev, false);
        }
    }

    private void navigateForward() {
        if (!forwardHistory.isEmpty()) {
            backHistory.push(currentDirectory);
            File next = forwardHistory.pop();
            navigateTo(next, false);
        }
    }

    private void navigateUp() {
        if (currentDirectory != null && currentDirectory.getParentFile() != null) {
            navigateTo(currentDirectory.getParentFile(), true);
        }
    }

    private void applyFilters() {
        String query = searchField.getText() != null ? searchField.getText().trim().toLowerCase() : "";
        boolean showHidden = showHiddenBtn.isSelected();
        String typeFilter = filterComboBox.getValue();

        filteredItems.setPredicate(item -> {
            String name = item.getName();

            // Hidden files check
            if (!showHidden && name.startsWith(".")) {
                return false;
            }

            // Search query filter
            if (!query.isEmpty() && !name.toLowerCase().contains(query)) {
                return false;
            }

            // Extension / category filter
            if (typeFilter != null && !item.isDirectory() && !typeFilter.startsWith("All")) {
                String ext = "." + FileItem.extractExtension(name).toLowerCase();
                if (typeFilter.startsWith("Documents")) {
                    return ext.equals(".pdf") || ext.equals(".docx") || ext.equals(".doc") || ext.equals(".txt") || ext.equals(".md") || ext.equals(".xlsx");
                } else if (typeFilter.startsWith("Images")) {
                    return ext.equals(".png") || ext.equals(".jpg") || ext.equals(".jpeg") || ext.equals(".gif") || ext.equals(".webp") || ext.equals(".svg");
                } else if (typeFilter.startsWith("Archives")) {
                    return ext.equals(".zip") || ext.equals(".tar") || ext.equals(".gz") || ext.equals(".7z") || ext.equals(".rar");
                } else if (typeFilter.startsWith("Code")) {
                    return ext.equals(".java") || ext.equals(".py") || ext.equals(".js") || ext.equals(".html") || ext.equals(".css") || ext.equals(".json") || ext.equals(".xml");
                }
            }

            return true;
        });

        renderGrid();
    }

    private void updateSelectionFromTable() {
        ObservableList<FileItem> selected = tableView.getSelectionModel().getSelectedItems();
        if (selected == null || selected.isEmpty()) {
            if (mode == Mode.SELECT_FOLDER) {
                selectionLabel.setText("Current folder: " + currentDirectory.getName());
                confirmButton.setDisable(false);
            } else {
                selectionLabel.setText("No selection");
                confirmButton.setDisable(true);
            }
            singleSelectedGridItem = null;
            updateGridSelectionStyles();
            return;
        }

        updateSelectionStatus(selected);
        if (!selected.isEmpty()) {
            singleSelectedGridItem = selected.get(0);
            updateGridSelectionStyles();
        }
    }

    private void updateSelectionStatus(List<FileItem> selected) {
        if (selected.isEmpty()) {
            confirmButton.setDisable(mode != Mode.SELECT_FOLDER);
            return;
        }

        if (selected.size() == 1) {
            FileItem item = selected.get(0);
            if (item.isDirectory()) {
                selectionLabel.setText("Selected Folder: " + item.getName());
                confirmButton.setDisable(false);
            } else {
                selectionLabel.setText("Selected: " + item.getName() + " (" + item.getFormattedSize() + ")");
                confirmButton.setDisable(false);
            }
        } else {
            selectionLabel.setText("Selected: " + selected.size() + " items");
            confirmButton.setDisable(false);
        }
    }

    private void confirmSelection() {
        selectedFilesResult.clear();

        if (mode == Mode.SELECT_FOLDER) {
            FileItem selected = tableView.getSelectionModel().getSelectedItem();
            if (selected == null) selected = singleSelectedGridItem;

            if (selected != null && selected.isDirectory()) {
                selectedFilesResult.add(selected.getFile());
            } else {
                // User chose the current directory
                selectedFilesResult.add(currentDirectory);
            }
            close();
            return;
        }

        // File selection modes
        ObservableList<FileItem> selected = tableView.getSelectionModel().getSelectedItems();
        if ((selected == null || selected.isEmpty()) && singleSelectedGridItem != null) {
            selected = FXCollections.observableArrayList(singleSelectedGridItem);
        }

        if (selected != null) {
            for (FileItem item : selected) {
                if (item != null && !item.isDirectory() && item.getFile().exists()) {
                    selectedFilesResult.add(item.getFile());
                }
            }
        }

        if (!selectedFilesResult.isEmpty()) {
            close();
        }
    }

    private void launchSystemChooser(Window owner) {
        if (mode == Mode.SELECT_FOLDER) {
            DirectoryChooser dc = new DirectoryChooser();
            dc.setTitle("Select Destination Directory");
            if (currentDirectory != null && currentDirectory.exists()) {
                dc.setInitialDirectory(currentDirectory);
            }
            File chosen = dc.showDialog(owner != null ? owner : this);
            if (chosen != null) {
                selectedFilesResult.clear();
                selectedFilesResult.add(chosen);
                close();
            }
        } else {
            FileChooser fc = new FileChooser();
            fc.setTitle(mode == Mode.OPEN_MULTIPLE_FILES ? "Select Files to Upload" : "Select File to Upload");
            if (currentDirectory != null && currentDirectory.exists()) {
                fc.setInitialDirectory(currentDirectory);
            }
            if (mode == Mode.OPEN_MULTIPLE_FILES) {
                List<File> chosen = fc.showOpenMultipleDialog(owner != null ? owner : this);
                if (chosen != null && !chosen.isEmpty()) {
                    selectedFilesResult.clear();
                    selectedFilesResult.addAll(chosen);
                    close();
                }
            } else {
                File chosen = fc.showOpenDialog(owner != null ? owner : this);
                if (chosen != null) {
                    selectedFilesResult.clear();
                    selectedFilesResult.add(chosen);
                    close();
                }
            }
        }
    }

    // ======================== STATIC ENTRY POINTS ========================

    public static Optional<File> showOpenFile(Window owner, String title, File initialDirectory) {
        FilePickerDialog dialog = new FilePickerDialog(owner, title, Mode.OPEN_FILE, initialDirectory);
        dialog.showAndWait();
        return dialog.selectedFilesResult.isEmpty() ? Optional.empty() : Optional.of(dialog.selectedFilesResult.get(0));
    }

    public static List<File> showOpenMultipleFiles(Window owner, String title, File initialDirectory) {
        FilePickerDialog dialog = new FilePickerDialog(owner, title, Mode.OPEN_MULTIPLE_FILES, initialDirectory);
        dialog.showAndWait();
        return dialog.selectedFilesResult;
    }

    public static Optional<File> showSelectFolder(Window owner, String title, File initialDirectory) {
        FilePickerDialog dialog = new FilePickerDialog(owner, title, Mode.SELECT_FOLDER, initialDirectory);
        dialog.showAndWait();
        return dialog.selectedFilesResult.isEmpty() ? Optional.empty() : Optional.of(dialog.selectedFilesResult.get(0));
    }
}
