package com.minicloud.client.ui;

import com.minicloud.client.models.DriveItem;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;

/**
 * Factory for creating contextual right-click menus on Drive items (files and folders).
 * Uses vector graphics from IconFactory and Everforest styling (no emojis).
 */
public class ContextMenuFactory {

    public interface ContextMenuListener {
        void onOpen(DriveItem item);
        void onPreview(DriveItem item);
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
            MenuItem openItem = new MenuItem("Open Folder");
            openItem.setGraphic(IconFactory.openFolderIcon(14));
            openItem.setOnAction(e -> listener.onOpen(item));

            MenuItem renameItem = new MenuItem("Rename");
            renameItem.setGraphic(IconFactory.renameIcon(14));
            renameItem.setOnAction(e -> listener.onRename(item));

            MenuItem moveItem = new MenuItem("Move To...");
            moveItem.setGraphic(IconFactory.moveIcon(14));
            moveItem.setOnAction(e -> listener.onMove(item));

            MenuItem propItem = new MenuItem("Properties");
            propItem.setGraphic(IconFactory.infoIcon(14));
            propItem.setOnAction(e -> listener.onProperties(item));

            MenuItem deleteItem = new MenuItem("Delete Folder");
            deleteItem.setGraphic(IconFactory.deleteIcon(14));
            deleteItem.setStyle("-fx-text-fill: #e67e80;");
            deleteItem.setOnAction(e -> listener.onDelete(item));

            menu.getItems().addAll(openItem, new SeparatorMenuItem(), renameItem, moveItem, propItem, new SeparatorMenuItem(), deleteItem);

        } else {
            MenuItem previewItem = new MenuItem("Preview");
            previewItem.setGraphic(IconFactory.previewIcon(14));
            previewItem.setOnAction(e -> listener.onPreview(item));

            MenuItem downloadItem = new MenuItem("Download");
            downloadItem.setGraphic(IconFactory.downloadIcon(14));
            downloadItem.setOnAction(e -> listener.onDownload(item));

            MenuItem renameItem = new MenuItem("Rename");
            renameItem.setGraphic(IconFactory.renameIcon(14));
            renameItem.setOnAction(e -> listener.onRename(item));

            MenuItem moveItem = new MenuItem("Move To...");
            moveItem.setGraphic(IconFactory.moveIcon(14));
            moveItem.setOnAction(e -> listener.onMove(item));

            MenuItem copyItem = new MenuItem("Make a Copy");
            copyItem.setGraphic(IconFactory.copyIcon(14));
            copyItem.setOnAction(e -> listener.onCopy(item));

            MenuItem propItem = new MenuItem("Properties");
            propItem.setGraphic(IconFactory.infoIcon(14));
            propItem.setOnAction(e -> listener.onProperties(item));

            MenuItem deleteItem = new MenuItem("Delete File");
            deleteItem.setGraphic(IconFactory.deleteIcon(14));
            deleteItem.setStyle("-fx-text-fill: #e67e80;");
            deleteItem.setOnAction(e -> listener.onDelete(item));

            menu.getItems().addAll(previewItem, downloadItem, new SeparatorMenuItem(), renameItem, moveItem, copyItem, propItem, new SeparatorMenuItem(), deleteItem);
        }

        return menu;
    }
}
