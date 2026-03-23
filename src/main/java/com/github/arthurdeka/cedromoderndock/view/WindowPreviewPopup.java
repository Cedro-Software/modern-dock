package com.github.arthurdeka.cedromoderndock.view;

import com.github.arthurdeka.cedromoderndock.application.DockTheme;
import com.github.arthurdeka.cedromoderndock.util.NativeWindowUtils;
import javafx.beans.value.ChangeListener;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Popup;
import javafx.stage.Screen;

import java.util.List;
import java.util.function.Consumer;

public class WindowPreviewPopup extends Popup {

    private final VBox container;
    private Node currentTarget;
    private Node currentDock;
    // Reposition whenever popup size changes (content can grow/shrink).
    private final ChangeListener<Number> sizeListener = (obs, old, val) -> reposition();

    public WindowPreviewPopup() {
        this.container = new VBox();
        this.container.setPadding(new Insets(10));
        this.container.setSpacing(5);
        this.container.setAlignment(Pos.CENTER_LEFT);

        // Prevent auto-hide when clicking inside the popup
        this.setAutoHide(false);
        this.getContent().add(container);

        widthProperty().addListener(sizeListener);
        heightProperty().addListener(sizeListener);
    }

    public void updateContent(
            List<NativeWindowUtils.WindowInfo> windows,
            Image appIcon,
            DockTheme dockTheme,
            Consumer<NativeWindowUtils.WindowInfo> activateWindowAction
    ) {
        container.getChildren().clear();

        // Apply style from dock model so the popup matches the dock theme.
        String style = String.format(
                "-fx-background-color: rgba(%s %s); -fx-background-radius: %s;",
                dockTheme.colorRgb(),
                dockTheme.transparency(),
                dockTheme.borderRounding()
        );
        container.setStyle(style);

        // Choose a readable text color based on dock background brightness.
        Color textColor = getTextColorForBackground(dockTheme.colorRgb());

        for (NativeWindowUtils.WindowInfo window : windows) {
            HBox item = createWindowItem(window, appIcon, textColor, activateWindowAction);
            container.getChildren().add(item);
        }
    }

    private Color getTextColorForBackground(String dockColorRGB) {
        try {
            // Remove commas and trim
            String cleanRGB = dockColorRGB.replace(",", " ").trim();
            String[] parts = cleanRGB.split("\\s+");
            if (parts.length >= 3) {
                int r = Integer.parseInt(parts[0]);
                int g = Integer.parseInt(parts[1]);
                int b = Integer.parseInt(parts[2]);
                // Calculate brightness to choose black/white contrast.
                double brightness = (r * 0.299 + g * 0.587 + b * 0.114);
                return brightness > 128 ? Color.BLACK : Color.WHITE;
            }
        } catch (NumberFormatException e) {
            // Ignore, default to white
        }
        return Color.WHITE;
    }

    private HBox createWindowItem(
            NativeWindowUtils.WindowInfo window,
            Image appIcon,
            Color textColor,
            Consumer<NativeWindowUtils.WindowInfo> activateWindowAction
    ) {
        HBox item = new HBox();
        item.setSpacing(10);
        item.setPadding(new Insets(5, 10, 5, 10));
        item.setAlignment(Pos.CENTER_LEFT);
        item.setStyle("-fx-background-color: transparent; -fx-background-radius: 5; -fx-cursor: hand;");

        // Icon
        ImageView iconView = new ImageView(appIcon);
        iconView.setFitWidth(16);
        iconView.setFitHeight(16);
        iconView.setPreserveRatio(true);

        // Title
        Label titleLabel = new Label(window.title());
        titleLabel.setTextFill(textColor);
        titleLabel.setStyle("-fx-font-size: 12px;");

        item.getChildren().addAll(iconView, titleLabel);

        // Hover effect for better visual feedback.
        item.setOnMouseEntered(e -> {
            item.setStyle("-fx-background-color: rgba(255, 255, 255, 0.2); -fx-background-radius: 5; -fx-cursor: hand;");
            titleLabel.setTextFill(Color.WHITE); // Always white on hover for better contrast against hover bg
        });
        item.setOnMouseExited(e -> {
            item.setStyle("-fx-background-color: transparent; -fx-background-radius: 5; -fx-cursor: hand;");
            titleLabel.setTextFill(textColor);
        });

        // Click action
        item.setOnMouseClicked(e -> {
            activateWindowAction.accept(window);
            this.hide();
        });

        return item;
    }

    public VBox getContainer() {
        return container;
    }

    public void showAbove(Node target, Node dockContainer) {
        this.currentTarget = target;
        this.currentDock = dockContainer;
        Bounds bounds = target.localToScreen(target.getBoundsInLocal());

        // Initial show, then reposition to avoid flicker.
        if (!isShowing()) {
            this.show(target, bounds.getMinX(), bounds.getMinY());
        }
        reposition();
    }

    private void reposition() {
        if (currentTarget == null || !isShowing()) return;
        Bounds bounds = currentTarget.localToScreen(currentTarget.getBoundsInLocal());
        if (bounds == null) return;

        double w = getWidth();
        double h = getHeight();
        double gap = 5;

        // Center horizontally over the target icon.
        setX(bounds.getMinX() + (bounds.getWidth() / 2) - (w / 2));

        Rectangle2D screenBounds = getScreenBounds(bounds);
        Bounds dockBounds = getDockBounds(bounds);
        double yAbove = dockBounds.getMinY() - h - gap;
        double yBelow = dockBounds.getMaxY() + gap;

        // If there's no room above the dock, place below instead.
        if (yAbove < screenBounds.getMinY() + gap) {
            setY(yBelow);
        } else {
            setY(yAbove);
        }
    }

    private Rectangle2D getScreenBounds(Bounds bounds) {
        // Use the screen containing the target bounds.
        for (Screen screen : Screen.getScreensForRectangle(
                bounds.getMinX(),
                bounds.getMinY(),
                bounds.getWidth(),
                bounds.getHeight()
        )) {
            return screen.getVisualBounds();
        }
        return Screen.getPrimary().getVisualBounds();
    }

    private Bounds getDockBounds(Bounds fallbackBounds) {
        if (currentDock == null) {
            return fallbackBounds;
        }
        Bounds dockBounds = currentDock.localToScreen(currentDock.getBoundsInLocal());
        return dockBounds != null ? dockBounds : fallbackBounds;
    }
}
