package com.minicloud.client.ui.dialogs;

import com.minicloud.client.models.FolderRecord;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.Optional;

/**
 * Modal dialog for selecting a destination folder to move an item into.
 */
public class MoveDialog {

    public static class MoveTarget {
        private final Long folderId;
        private final String displayName;

        public MoveTarget(Long folderId, String displayName) {
            this.folderId = folderId;
            this.displayName = displayName;
        }

        public Long getFolderId() { return folderId; }
        public String getDisplayName() { return displayName; }

        @Override
        public String toString() {
            return displayName;
        }
    }

    public static Optional<MoveTarget> show(String itemName, List<FolderRecord> availableFolders, Long currentFolderId) {
        Dialog<MoveTarget> dialog = new Dialog<>();
        dialog.setTitle("Move Item");
        dialog.setHeaderText("Choose destination for \"" + itemName + "\"");

        ButtonType moveButtonType = new ButtonType("Move Here", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(moveButtonType, ButtonType.CANCEL);

        VBox content = new VBox(12);
        content.setPadding(new Insets(20));

        ComboBox<MoveTarget> comboBox = new ComboBox<>();
        comboBox.setPrefWidth(300);

        MoveTarget rootTarget = new MoveTarget(null, "☁  My Drive (Root)");
        comboBox.getItems().add(rootTarget);

        if (availableFolders != null) {
            for (FolderRecord fr : availableFolders) {
                comboBox.getItems().add(new MoveTarget(fr.getId(), "📁  " + fr.getName()));
            }
        }

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
