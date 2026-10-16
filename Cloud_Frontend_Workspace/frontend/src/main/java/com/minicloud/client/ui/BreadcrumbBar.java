package com.minicloud.client.ui;

import com.minicloud.client.models.BreadcrumbRecord;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

import java.util.List;
import java.util.function.Consumer;

/**
 * Clickable breadcrumb navigation bar (e.g., "My Drive > Documents > Reports").
 */
public class BreadcrumbBar {

    private final HBox container;
    private Consumer<BreadcrumbRecord> onNavigate;

    public BreadcrumbBar() {
        container = new HBox(6);
        container.setAlignment(Pos.CENTER_LEFT);
        container.setPadding(new Insets(8, 12, 8, 12));
        container.setStyle("-fx-background-color: #1a1a1a; -fx-border-color: #2D2D2D; -fx-border-radius: 5; -fx-background-radius: 5;");

        setBreadcrumbs(List.of(new BreadcrumbRecord(null, "My Drive")));
    }

    public HBox getContainer() {
        return container;
    }

    public void setOnNavigate(Consumer<BreadcrumbRecord> onNavigate) {
        this.onNavigate = onNavigate;
    }

    /**
     * Updates the breadcrumb trail with the given list of segments.
     */
    public void setBreadcrumbs(List<BreadcrumbRecord> trail) {
        container.getChildren().clear();

        if (trail == null || trail.isEmpty()) {
            trail = List.of(new BreadcrumbRecord(null, "My Drive"));
        }

        for (int i = 0; i < trail.size(); i++) {
            BreadcrumbRecord item = trail.get(i);
            boolean isLast = (i == trail.size() - 1);

            Button btn = new Button(item.getName());
            if (isLast) {
                btn.setStyle("-fx-background-color: transparent; -fx-text-fill: #ffffff; -fx-font-weight: bold; -fx-cursor: default; -fx-padding: 2 6;");
            } else {
                btn.setStyle("-fx-background-color: transparent; -fx-text-fill: #3b82f6; -fx-cursor: hand; -fx-padding: 2 6;");
                btn.setOnAction(e -> {
                    if (onNavigate != null) {
                        onNavigate.accept(item);
                    }
                });
            }

            container.getChildren().add(btn);

            if (!isLast) {
                Label separator = new Label("›");
                separator.setStyle("-fx-text-fill: #666666; -fx-font-size: 14px; -fx-font-weight: bold;");
                container.getChildren().add(separator);
            }
        }
    }
}
