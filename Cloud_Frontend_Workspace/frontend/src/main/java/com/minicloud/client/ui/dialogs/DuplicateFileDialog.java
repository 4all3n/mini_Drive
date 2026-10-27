package com.minicloud.client.ui.dialogs;

import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.util.Optional;

/**
 * Modal dialog presented when an uploaded or dropped file collides with an existing file in the current folder.
 * Provides explicit choices: Overwrite, Rename (with editable/suggested name), or Cancel.
 */
public class DuplicateFileDialog {

    public enum Choice {
        OVERWRITE,
        RENAME,
        CANCEL
    }

    public static class Resolution {
        private final Choice choice;
        private final String newName;

        public Resolution(Choice choice, String newName) {
            this.choice = choice;
            this.newName = newName;
        }

        public Choice getChoice() { return choice; }
        public String getNewName() { return newName; }
    }

    public static Optional<Resolution> show(String existingFileName, String suggestedNewName) {
        Dialog<Resolution> dialog = new Dialog<>();
        dialog.setTitle("File Conflict");
        dialog.setHeaderText("A file named \"" + existingFileName + "\" already exists in this folder.");

        try {
            dialog.getDialogPane().getStylesheets().add(
                    DuplicateFileDialog.class.getResource("/css/everforest.css").toExternalForm()
            );
        } catch (Exception ignored) {}

        ButtonType overwriteType = new ButtonType("Overwrite", ButtonBar.ButtonData.OK_DONE);
        ButtonType renameType = new ButtonType("Rename", ButtonBar.ButtonData.OTHER);
        ButtonType cancelType = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);

        dialog.getDialogPane().getButtonTypes().addAll(overwriteType, renameType, cancelType);

        VBox content = new VBox(12);
        content.setPadding(new Insets(15));

        Label promptLabel = new Label("What would you like to do?");
        promptLabel.setStyle("-fx-text-fill: #d3c6aa;");

        Label renameLabel = new Label("If choosing Rename, specify the new filename:");
        renameLabel.setStyle("-fx-text-fill: #859289; -fx-font-size: 11px;");

        TextField renameField = new TextField(suggestedNewName);
        renameField.setPrefWidth(300);
        renameField.selectAll();

        content.getChildren().addAll(promptLabel, renameLabel, renameField);
        dialog.getDialogPane().setContent(content);

        dialog.setResultConverter(button -> {
            if (button == overwriteType) {
                return new Resolution(Choice.OVERWRITE, existingFileName);
            } else if (button == renameType) {
                String name = renameField.getText().trim();
                return new Resolution(Choice.RENAME, name.isEmpty() ? suggestedNewName : name);
            }
            return new Resolution(Choice.CANCEL, null);
        });

        return dialog.showAndWait();
    }
}
