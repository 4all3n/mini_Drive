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
 * Styled according to Everforest theme with vector icons and clean typography.
 */
public class BreadcrumbBar {

    private final HBox container;
    private Consumer<BreadcrumbRecord> onNavigate;

    public BreadcrumbBar() {
        container = new HBox(6);
        container.setAlignment(Pos.CENTER_LEFT);
        container.setPadding(new Insets(6, 12, 6, 12));
        container.getStyleClass().add("breadcrumb-bar");

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
            boolean isRoot = (i == 0);

            Button btn = new Button(item.getName());
            if (isRoot) {
                btn.setGraphic(IconFactory.cloudIcon(14));
            }

            if (isLast) {
                btn.setStyle("-fx-background-color: transparent; -fx-text-fill: #d3c6aa; -fx-font-weight: bold; -fx-cursor: default; -fx-padding: 3 8;");
            } else {
                btn.setStyle("-fx-background-color: transparent; -fx-text-fill: #7daea3; -fx-cursor: hand; -fx-padding: 3 8;");
                btn.setOnAction(e -> {
                    if (onNavigate != null) {
                        onNavigate.accept(item);
                    }
                });
            }

            container.getChildren().add(btn);

            if (!isLast) {
                Label separator = new Label("›");
                separator.setStyle("-fx-text-fill: #7a8478; -fx-font-size: 15px; -fx-font-weight: bold;");
                container.getChildren().add(separator);
            }
        }
    }
}
