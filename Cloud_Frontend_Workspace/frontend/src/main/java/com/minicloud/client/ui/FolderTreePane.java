package com.minicloud.client.ui;

import com.minicloud.client.models.FolderRecord;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Left sidebar component displaying the virtual folder hierarchy in a TreeView.
 * Supports navigation and folder selection.
 * Styled with Everforest dark theme; uses IconFactory for icons without emojis.
 */
public class FolderTreePane {

    private final VBox container;
    private final TreeView<FolderNode> treeView;
    private final TreeItem<FolderNode> rootItem;
    private Consumer<FolderNode> onFolderSelected;
    private boolean updating = false;

    public static class FolderNode {
        private final Long id;
        private String name;
        private final boolean root;

        public FolderNode(Long id, String name, boolean root) {
            this.id = id;
            this.name = name;
            this.root = root;
        }

        public Long getId() { return id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public boolean isRoot() { return root; }

        @Override
        public String toString() {
            return name;
        }
    }

    public FolderTreePane() {
        container = new VBox(10);
        container.setPadding(new Insets(15, 10, 15, 10));
        container.getStyleClass().add("sidebar");
        container.setPrefWidth(220);
        container.setMinWidth(180);

        Label title = new Label("FOLDERS");
        title.getStyleClass().add("sidebar-title");

        FolderNode rootNode = new FolderNode(null, "My Drive", true);
        rootItem = new TreeItem<>(rootNode);
        rootItem.setExpanded(true);

        treeView = new TreeView<>(rootItem);
        treeView.setShowRoot(true);

        // User click selection listener - only fires when user interacts, not during programmatic updates
        treeView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (!updating && newVal != null && onFolderSelected != null) {
                onFolderSelected.accept(newVal.getValue());
            }
        });

        // Set custom cell factory to display colored folder/cloud icon
        treeView.setCellFactory(tv -> new TreeCell<FolderNode>() {
            @Override
            protected void updateItem(FolderNode item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(item.getName());
                    if (item.isRoot()) {
                        setGraphic(IconFactory.cloudIcon(16));
                    } else {
                        setGraphic(IconFactory.folderIcon(16));
                    }
                }
            }
        });
        container.getChildren().addAll(title, treeView);
    }

    public VBox getContainer() {
        return container;
    }

    public TreeView<FolderNode> getTreeView() {
        return treeView;
    }

    public void setOnFolderSelected(Consumer<FolderNode> onFolderSelected) {
        this.onFolderSelected = onFolderSelected;
    }

    /**
     * Rebuilds the folder tree from a flat list of FolderRecords.
     * Guarded by updating flag so listeners do not fire during clear/populate.
     */
    public void populateTree(List<FolderRecord> allFolders) {
        updating = true;
        try {
            rootItem.getChildren().clear();

            if (allFolders == null || allFolders.isEmpty()) {
                return;
            }

            Map<Long, TreeItem<FolderNode>> itemMap = new HashMap<>();

            // 1. Create TreeItems for each folder
            for (FolderRecord fr : allFolders) {
                FolderNode node = new FolderNode(fr.getId(), fr.getName(), false);
                TreeItem<FolderNode> item = new TreeItem<>(node);
                item.setExpanded(true);
                itemMap.put(fr.getId(), item);
            }

            // 2. Build parent-child relationships
            for (FolderRecord fr : allFolders) {
                TreeItem<FolderNode> item = itemMap.get(fr.getId());
                Long parentId = fr.getParentFolderId();
                if (parentId == null) {
                    rootItem.getChildren().add(item);
                } else if (itemMap.containsKey(parentId)) {
                    itemMap.get(parentId).getChildren().add(item);
                } else {
                    rootItem.getChildren().add(item);
                }
            }
        } finally {
            updating = false;
        }
    }

    /**
     * Selects a folder node by ID (or root if null).
     * Guarded by updating flag so programmatic selection does not re-trigger onFolderSelected.
     */
    public void selectFolder(Long folderId) {
        updating = true;
        try {
            if (folderId == null) {
                treeView.getSelectionModel().select(rootItem);
                return;
            }
            if (!selectNodeRecursive(rootItem, folderId)) {
                treeView.getSelectionModel().clearSelection();
            }
        } finally {
            updating = false;
        }
    }

    private boolean selectNodeRecursive(TreeItem<FolderNode> current, Long targetId) {
        if (current.getValue() != null && targetId.equals(current.getValue().getId())) {
            treeView.getSelectionModel().select(current);
            // Ensure ancestors are expanded so the selected node is visible
            TreeItem<FolderNode> parent = current.getParent();
            while (parent != null) {
                parent.setExpanded(true);
                parent = parent.getParent();
            }
            return true;
        }
        for (TreeItem<FolderNode> child : current.getChildren()) {
            if (selectNodeRecursive(child, targetId)) {
                return true;
            }
        }
        return false;
    }
}
