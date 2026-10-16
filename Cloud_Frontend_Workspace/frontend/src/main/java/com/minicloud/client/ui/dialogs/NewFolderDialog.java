package com.minicloud.client.ui.dialogs;

import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.util.Optional;

/**
 * Modal dialog for creating a new virtual folder.
 */
public class NewFolderDialog {

    public static Optional<String> show(String currentFolderName) {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("New Folder");
        dialog.setHeaderText("Create a new folder in " + (currentFolderName != null ? currentFolderName : "My Drive"));

        ButtonType createButtonType = new ButtonType("Create", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(createButtonType, ButtonType.CANCEL);

        VBox content = new VBox(10);
        content.setPadding(new Insets(20));

        TextField nameField = new TextField();
        nameField.setPromptText("Folder Name");
        nameField.setText("Untitled folder");
        nameField.selectAll();

        content.getChildren().addAll(new Label("Folder Name:"), nameField);
        dialog.getDialogPane().setContent(content);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == createButtonType) {
                String text = nameField.getText().trim();
                return text.isEmpty() ? "Untitled folder" : text;
            }
            return null;
        });

        return dialog.showAndWait();
    }
}
