package com.minicloud.client.ui.dialogs;

import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.util.Optional;

/**
 * Modal dialog for renaming a file or virtual folder.
 */
public class RenameDialog {

    public static Optional<String> show(String currentName, boolean isFolder) {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle(isFolder ? "Rename Folder" : "Rename File");
        dialog.setHeaderText("Enter a new name for " + currentName);

        ButtonType renameButtonType = new ButtonType("Rename", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(renameButtonType, ButtonType.CANCEL);

        VBox content = new VBox(10);
        content.setPadding(new Insets(20));

        TextField nameField = new TextField();
        nameField.setText(currentName);
        nameField.selectAll();

        content.getChildren().addAll(new Label("New Name:"), nameField);
        dialog.getDialogPane().setContent(content);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == renameButtonType) {
                String text = nameField.getText().trim();
                return text.isEmpty() ? null : text;
            }
            return null;
        });

        return dialog.showAndWait();
    }
}
