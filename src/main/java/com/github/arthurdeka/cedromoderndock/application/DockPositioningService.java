package com.github.arthurdeka.cedromoderndock.application;

import com.github.arthurdeka.cedromoderndock.model.DockHorizontalAnchor;
import com.github.arthurdeka.cedromoderndock.model.DockModel;
import com.github.arthurdeka.cedromoderndock.model.DockPositioningMode;
import com.github.arthurdeka.cedromoderndock.model.DockVerticalAnchor;
import javafx.geometry.Rectangle2D;
import javafx.stage.Screen;
import javafx.stage.Stage;

public class DockPositioningService {
    private final DockService dockService;

    public DockPositioningService(DockService dockService) {
        this.dockService = dockService;
    }

    public DockPositioningMode getPositioningMode() {
        return dockService.getDock().getPositioningMode();
    }

    public void setPositioningMode(DockPositioningMode positioningMode) {
        dockService.getDock().setPositioningMode(positioningMode);
        dockService.saveChanges();
    }

    public DockVerticalAnchor getVerticalAnchor() {
        return dockService.getDock().getVerticalAnchor();
    }

    public void setVerticalAnchor(DockVerticalAnchor verticalAnchor) {
        dockService.getDock().setVerticalAnchor(verticalAnchor);
        dockService.saveChanges();
    }

    public DockHorizontalAnchor getHorizontalAnchor() {
        return dockService.getDock().getHorizontalAnchor();
    }

    public void setHorizontalAnchor(DockHorizontalAnchor horizontalAnchor) {
        dockService.getDock().setHorizontalAnchor(horizontalAnchor);
        dockService.saveChanges();
    }

    public int getScreenEdgeSpacing() {
        return dockService.getDock().getTopSpacing();
    }

    public void setScreenEdgeSpacing(int spacing) {
        setTopSpacing(spacing);
        setLeftSpacing(spacing);
        setRightSpacing(spacing);
        setBottomSpacing(spacing);
    }

    public int getTopSpacing() {
        return dockService.getDock().getTopSpacing();
    }

    public void setTopSpacing(int spacing) {
        dockService.getDock().setTopSpacing(Math.max(0, spacing));
        dockService.saveChanges();
    }

    public int getLeftSpacing() {
        return dockService.getDock().getLeftSpacing();
    }

    public void setLeftSpacing(int spacing) {
        dockService.getDock().setLeftSpacing(Math.max(0, spacing));
        dockService.saveChanges();
    }

    public int getRightSpacing() {
        return dockService.getDock().getRightSpacing();
    }

    public void setRightSpacing(int spacing) {
        dockService.getDock().setRightSpacing(Math.max(0, spacing));
        dockService.saveChanges();
    }

    public int getBottomSpacing() {
        return dockService.getDock().getBottomSpacing();
    }

    public void setBottomSpacing(int spacing) {
        dockService.getDock().setBottomSpacing(Math.max(0, spacing));
        dockService.saveChanges();
    }

    public boolean isDynamicPositioning() {
        return getPositioningMode() == DockPositioningMode.DYNAMIC;
    }

    public void applyPosition(Stage stage) {
        if (stage == null) {
            return;
        }

        DockModel dock = dockService.getDock();
        if (dock.getPositioningMode() == DockPositioningMode.DYNAMIC) {
            stage.setX(dock.getDockPositionX());
            stage.setY(dock.getDockPositionY());
            return;
        }

        Rectangle2D bounds = Screen.getPrimary().getVisualBounds();
        double stageWidth = stage.getWidth();
        double stageHeight = stage.getHeight();

        stage.setX(resolveHorizontalPosition(bounds, stageWidth, dock));
        stage.setY(resolveVerticalPosition(bounds, stageHeight, dock));
    }

    private double resolveHorizontalPosition(
            Rectangle2D bounds,
            double stageWidth,
            DockModel dock
    ) {
        return switch (dock.getHorizontalAnchor()) {
            case LEFT -> bounds.getMinX() + dock.getLeftSpacing();
            case MIDDLE -> bounds.getMinX() + ((bounds.getWidth() - stageWidth) / 2);
            case RIGHT -> bounds.getMaxX() - stageWidth - dock.getRightSpacing();
        };
    }

    private double resolveVerticalPosition(
            Rectangle2D bounds,
            double stageHeight,
            DockModel dock
    ) {
        return switch (dock.getVerticalAnchor()) {
            case TOP -> bounds.getMinY() + dock.getTopSpacing();
            case MIDDLE -> bounds.getMinY() + ((bounds.getHeight() - stageHeight) / 2);
            case DOWN -> bounds.getMaxY() - stageHeight - dock.getBottomSpacing();
        };
    }
}
