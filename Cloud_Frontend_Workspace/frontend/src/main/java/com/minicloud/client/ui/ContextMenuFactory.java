package com.minicloud.client.ui;

import com.minicloud.client.models.DriveItem;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;

import java.util.function.Consumer;

/**
 * Factory for creating contextual right-click menus on Drive items (files and folders).
 */
public class ContextMenuFactory {

    public interface ContextMenuListener {
        void onOpen(DriveItem item);
        void onDownload(DriveItem item);
        void onRename(DriveItem item);
        void onMove(DriveItem item);
        void onCopy(DriveItem item);
        void onDelete(DriveItem item);
        void onProperties(DriveItem item);
    }

    public static ContextMenu create(DriveItem item, ContextMenuListener listener) {
        if (item == null || listener == null) return null;

        ContextMenu menu = new ContextMenu();

        if (item.isFolder()) {
            MenuItem openItem = new MenuItem("📂 Open Folder");
            openItem.setOnAction(e -> listener.onOpen(item));

            MenuItem renameItem = new MenuItem("✏ Rename");
            renameItem.setOnAction(e -> listener.onRename(item));

            MenuItem moveItem = new MenuItem("📦 Move To...");
            moveItem.setOnAction(e -> listener.onMove(item));

            MenuItem propItem = new MenuItem("ℹ Properties");
            propItem.setOnAction(e -> listener.onProperties(item));

            MenuItem deleteItem = new MenuItem("🗑 Delete Folder");
            deleteItem.setStyle("-fx-text-fill: #ef4444;");
            deleteItem.setOnAction(e -> listener.onDelete(item));

            menu.getItems().addAll(openItem, new SeparatorMenuItem(), renameItem, moveItem, propItem, new SeparatorMenuItem(), deleteItem);

        } else {
            MenuItem downloadItem = new MenuItem("⬇ Download");
            downloadItem.setOnAction(e -> listener.onDownload(item));

            MenuItem renameItem = new MenuItem("✏ Rename");
            renameItem.setOnAction(e -> listener.onRename(item));

            MenuItem moveItem = new MenuItem("📦 Move To...");
            moveItem.setOnAction(e -> listener.onMove(item));

            MenuItem copyItem = new MenuItem("📄 Make a Copy");
            copyItem.setOnAction(e -> listener.onCopy(item));

            MenuItem propItem = new MenuItem("ℹ Properties");
            propItem.setOnAction(e -> listener.onProperties(item));

            MenuItem deleteItem = new MenuItem("🗑 Delete File");
            deleteItem.setStyle("-fx-text-fill: #ef4444;");
            deleteItem.setOnAction(e -> listener.onDelete(item));

            menu.getItems().addAll(downloadItem, new SeparatorMenuItem(), renameItem, moveItem, copyItem, propItem, new SeparatorMenuItem(), deleteItem);
        }

        return menu;
    }
}
