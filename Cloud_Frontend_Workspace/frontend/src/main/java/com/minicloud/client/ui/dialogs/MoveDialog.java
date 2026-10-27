package com.minicloud.client.ui.dialogs;

import com.minicloud.client.models.FolderRecord;
import com.minicloud.client.ui.IconFactory;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.Optional;

/**
 * Modal dialog for selecting a destination folder to move an item into.
 * Styled with Everforest dark theme; uses clean labels and vector icons.
 */
public class MoveDialog {

    public static class MoveTarget {
        private final Long folderId;
        private final String displayName;
        private final boolean isRoot;

        public MoveTarget(Long folderId, String displayName, boolean isRoot) {
            this.folderId = folderId;
            this.displayName = displayName;
            this.isRoot = isRoot;
        }

        public Long getFolderId() { return folderId; }
        public String getDisplayName() { return displayName; }
        public boolean isRoot() { return isRoot; }

        @Override
        public String toString() {
            return displayName;
        }
    }

    public static Optional<MoveTarget> show(String itemName, List<FolderRecord> availableFolders, Long currentFolderId) {
        Dialog<MoveTarget> dialog = new Dialog<>();
        dialog.setTitle("Move Item");
        dialog.setHeaderText("Choose destination for \"" + itemName + "\"");

        // Attach stylesheet if available
        try {
            dialog.getDialogPane().getStylesheets().add(
                    MoveDialog.class.getResource("/css/everforest.css").toExternalForm()
            );
        } catch (Exception ignored) {}

        ButtonType moveButtonType = new ButtonType("Move Here", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(moveButtonType, ButtonType.CANCEL);

        VBox content = new VBox(12);
        content.setPadding(new Insets(20));

        ComboBox<MoveTarget> comboBox = new ComboBox<>();
        comboBox.setPrefWidth(300);

        MoveTarget rootTarget = new MoveTarget(null, "My Drive (Root)", true);
        comboBox.getItems().add(rootTarget);

        if (availableFolders != null) {
            for (FolderRecord fr : availableFolders) {
                comboBox.getItems().add(new MoveTarget(fr.getId(), fr.getName(), false));
            }
        }

        comboBox.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(MoveTarget item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(item.getDisplayName());
                    setGraphic(item.isRoot() ? IconFactory.cloudIcon(14) : IconFactory.folderIcon(14));
                }
            }
        });

        comboBox.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(MoveTarget item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(item.getDisplayName());
                    setGraphic(item.isRoot() ? IconFactory.cloudIcon(14) : IconFactory.folderIcon(14));
                }
            }
        });

        comboBox.getSelectionModel().select(rootTarget);

        content.getChildren().addAll(new Label("Select Target Destination:"), comboBox);
        dialog.getDialogPane().setContent(content);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == moveButtonType) {
                return comboBox.getSelectionModel().getSelectedItem();
            }
            return null;
        });

        return dialog.showAndWait();
    }
}
