package com.minicloud.client.ui.dialogs;

import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;

/**
 * Modal dialog displaying detailed properties of a file or folder.
 */
public class PropertiesDialog {

    public static void show(String title, String rawJsonOrInfo) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Properties - " + title);
        alert.setHeaderText("Item Information");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(8);
        grid.setPadding(new Insets(15));

        int row = 0;
        if (rawJsonOrInfo != null && rawJsonOrInfo.startsWith("{")) {
            // Strip outer braces and parse key-value pairs
            String content = rawJsonOrInfo.replace("{", "").replace("}", "").trim();
            String[] pairs = content.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
            for (String pair : pairs) {
                String[] kv = pair.split(":", 2);
                if (kv.length == 2) {
                    String key = kv[0].replace("\"", "").trim();
                    String val = kv[1].replace("\"", "").trim();

                    Label keyLabel = new Label(formatKey(key) + ":");
                    keyLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #555555;");

                    Label valLabel = new Label(val);
                    valLabel.setStyle("-fx-text-fill: #111111;");

                    grid.add(keyLabel, 0, row);
                    grid.add(valLabel, 1, row);
                    row++;
                }
            }
        } else {
            grid.add(new Label(rawJsonOrInfo != null ? rawJsonOrInfo : "No properties available."), 0, 0);
        }

        alert.getDialogPane().setContent(grid);
        alert.showAndWait();
    }

    private static String formatKey(String key) {
        if (key == null || key.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < key.length(); i++) {
            char c = key.charAt(i);
            if (i == 0) {
                sb.append(Character.toUpperCase(c));
            } else if (Character.isUpperCase(c)) {
                sb.append(" ").append(c);
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }
}
