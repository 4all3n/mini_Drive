package com.minicloud.client.ui;

import com.minicloud.client.models.DriveItem;
import javafx.collections.ListChangeListener;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Grid / Icon view for Drive items (files and folders).
 * Auto-wrapping responsive FlowPane inside a ScrollPane matching GNOME Files / Windows Explorer style.
 */
public class DriveGridView {

    private final ScrollPane scrollPane;
    private final FlowPane flowPane;
    private final FilteredList<DriveItem> items;
    private DriveItem selectedItem = null;
    private final Map<DriveItem, VBox> cardMap = new HashMap<>();

    private Consumer<DriveItem> onItemSelected;
    private Consumer<DriveItem> onItemDoubleClicked;
    private BiConsumer<DriveItem, javafx.scene.input.ContextMenuEvent> onItemContextMenu;

    public DriveGridView(FilteredList<DriveItem> items) {
        this.items = items;

        flowPane = new FlowPane();
        flowPane.setHgap(14);
        flowPane.setVgap(16);
        flowPane.setPadding(new Insets(16));
        flowPane.setStyle("-fx-background-color: #2b3339;");

        scrollPane = new ScrollPane(flowPane);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: #2b3339; -fx-background-color: #2b3339; -fx-border-color: transparent;");

        // Re-render when filtered items change
        items.addListener((ListChangeListener<DriveItem>) c -> render());

        render();
    }

    public ScrollPane getContainer() {
        return scrollPane;
    }

    public void setOnItemSelected(Consumer<DriveItem> onItemSelected) {
        this.onItemSelected = onItemSelected;
    }

    public void setOnItemDoubleClicked(Consumer<DriveItem> onItemDoubleClicked) {
        this.onItemDoubleClicked = onItemDoubleClicked;
    }

    public void setOnItemContextMenu(BiConsumer<DriveItem, javafx.scene.input.ContextMenuEvent> onItemContextMenu) {
        this.onItemContextMenu = onItemContextMenu;
    }

    public DriveItem getSelectedItem() {
        return selectedItem;
    }

    public void setSelectedItem(DriveItem item) {
        this.selectedItem = item;
        updateSelectionStyles();
    }

    public void render() {
        flowPane.getChildren().clear();
        cardMap.clear();

        if (items.isEmpty()) {
            Label emptyLabel = new Label("This folder is empty");
            emptyLabel.setStyle("-fx-text-fill: #7a8478; -fx-font-size: 14px; -fx-padding: 40;");
            flowPane.getChildren().add(emptyLabel);
            return;
        }

        for (DriveItem item : items) {
            VBox card = createCard(item);
            cardMap.put(item, card);
            flowPane.getChildren().add(card);
        }

        updateSelectionStyles();
    }

    private VBox createCard(DriveItem item) {
        VBox card = new VBox(6);
        card.setAlignment(Pos.TOP_CENTER);
        card.setPrefWidth(96);
        card.setMaxWidth(96);
        card.setPrefHeight(108);
        card.setPadding(new Insets(8, 4, 8, 4));

        // Large 52px icon matching the user screenshot
        Region icon = IconFactory.largeIcon(item.getName(), item.getType(), item.isFolder(), 52);

        Label label = new Label(item.getName());
        label.setWrapText(true);
        label.setMaxWidth(88);
        label.setMaxHeight(36);
        label.setTextAlignment(TextAlignment.CENTER);
        label.setAlignment(Pos.TOP_CENTER);
        label.setStyle("-fx-text-fill: #d3c6aa; -fx-font-size: 11px;");

        card.getChildren().addAll(icon, label);

        card.setOnMouseEntered(e -> {
            if (item != selectedItem) {
                card.setStyle("-fx-background-color: #323c41; -fx-background-radius: 8; -fx-cursor: hand;");
            }
        });

        card.setOnMouseExited(e -> {
            if (item != selectedItem) {
                card.setStyle("-fx-background-color: transparent; -fx-background-radius: 8;");
            }
        });

        card.setOnMouseClicked(e -> {
            if (e.getButton() == MouseButton.PRIMARY) {
                selectedItem = item;
                updateSelectionStyles();
                if (onItemSelected != null) {
                    onItemSelected.accept(item);
                }
                if (e.getClickCount() == 2) {
                    if (onItemDoubleClicked != null) {
                        onItemDoubleClicked.accept(item);
                    }
                }
            }
        });

        card.setOnContextMenuRequested(e -> {
            selectedItem = item;
            updateSelectionStyles();
            if (onItemContextMenu != null) {
                onItemContextMenu.accept(item, e);
            }
        });

        return card;
    }

    private void updateSelectionStyles() {
        for (Map.Entry<DriveItem, VBox> entry : cardMap.entrySet()) {
            DriveItem item = entry.getKey();
            VBox card = entry.getValue();
            if (item == selectedItem) {
                card.setStyle("-fx-background-color: #3a4145; -fx-border-color: #7daea3; -fx-border-width: 1.5; -fx-border-radius: 8; -fx-background-radius: 8;");
            } else {
                card.setStyle("-fx-background-color: transparent; -fx-background-radius: 8;");
            }
        }
    }
}
