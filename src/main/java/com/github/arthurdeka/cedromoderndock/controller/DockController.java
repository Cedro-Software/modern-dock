package com.github.arthurdeka.cedromoderndock.controller;

import com.github.arthurdeka.cedromoderndock.App;
import com.github.arthurdeka.cedromoderndock.application.AppServices;
import com.github.arthurdeka.cedromoderndock.application.DockTheme;
import com.github.arthurdeka.cedromoderndock.model.DockItem;
import com.github.arthurdeka.cedromoderndock.model.DockPositioningMode;
import com.github.arthurdeka.cedromoderndock.model.DockProgramItemModel;
import com.github.arthurdeka.cedromoderndock.model.DockSettingsItemModel;
import com.github.arthurdeka.cedromoderndock.model.DockWindowsModuleItemModel;
import com.github.arthurdeka.cedromoderndock.util.Logger;
import javafx.animation.PauseTransition;
import javafx.concurrent.Task;
import javafx.util.Duration;
import com.github.arthurdeka.cedromoderndock.util.NativeWindowUtils;
import com.github.arthurdeka.cedromoderndock.view.WindowPreviewPopup;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.github.arthurdeka.cedromoderndock.util.UIUtils.setStageIcon;

public class DockController {

    @FXML
    private AnchorPane rootPane;

    @FXML
    private HBox hBoxContainer;

    private AppServices appServices;
    private Stage stage;
    // Runs native window queries off the FX thread; single daemon thread avoids unbounded thread creation.
    private final ExecutorService windowPreviewExecutor = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "WindowPreviewFetcher");
        t.setDaemon(true);
        return t;
    });
    private WindowPreviewPopup windowPreviewPopup;
    private PauseTransition hideDebounce;
    private boolean isHoveringPopup = false;
    private Button currentHoverButton;
    private Button popupOwnerButton;
    // Monotonic id to ignore stale async results from previous hover requests.
    private int hoverRequestId = 0;

    // variables for the enableDrag function
    private double xOffset = 0;
    private double yOffset = 0;

    // Run when FXML is loaded
    public void handleInitialization() {
        // Popup that lists open windows for a program icon on hover.
        windowPreviewPopup = new WindowPreviewPopup();
        windowPreviewPopup.getContainer().setOnMouseEntered(e -> {
            isHoveringPopup = true;
            hideDebounce.stop();
        });
        windowPreviewPopup.getContainer().setOnMouseExited(e -> {
            isHoveringPopup = false;
            scheduleHide();
        });

        // Small delay prevents flicker when moving between icon and popup.
        hideDebounce = new PauseTransition(Duration.millis(80));
        hideDebounce.setOnFinished(e -> {
            if (shouldHidePreview()) {
                windowPreviewPopup.hide();
                popupOwnerButton = null;
            }
        });

        enableDrag();
        updateDockUI();
    }

    // enables dock drag effect
    private void enableDrag() {
        rootPane.setOnMousePressed(event -> {
            if (!appServices.positioningService().isDynamicPositioning()) {
                return;
            }
            xOffset = event.getSceneX();
            yOffset = event.getSceneY();
        });
        rootPane.setOnMouseDragged(event -> {
            if (!appServices.positioningService().isDynamicPositioning()) {
                return;
            }
            stage.setX(event.getScreenX() - xOffset);
            stage.setY(event.getScreenY() - yOffset);
        });

        // saves the dock position on the model
        rootPane.setOnMouseReleased(event -> {
            if (!appServices.positioningService().isDynamicPositioning()) {
                appServices.positioningService().applyPosition(stage);
                return;
            }
            appServices.dockService().setDockPosition(stage.getX(), stage.getY());
        });
    }

    public void addDockItem(DockItem item) {
        appServices.dockService().addItem(item);
    }

    public void removeDockItem(int index) {
        appServices.dockService().removeItem(index);
    }

    public List<DockItem> getDockItems() {
        return appServices.dockService().getItems();
    }

    public void swapItems(int firstItemIdx, int secondItemIdx) {
        appServices.dockService().swapItems(firstItemIdx, secondItemIdx);
        updateDockUI();
    }

    /* this method updates the DockView (actual rendered Dock) style and saves the changes */
    public void updateDockUI() {
        var dock = appServices.dockService().getDock();
        hBoxContainer.getChildren().clear();
        hBoxContainer.setSpacing(getDockIconsSpacing());
        hBoxContainer.setStyle("-fx-background-color: rgba(" + dock.getDockColorRGB() + " " + dock.getDockTransparency() + ");" + "-fx-background-radius: " + dock.getDockBorderRounding() + ";");

        for (DockItem item : dock.getItems()) {
            Button button = createButton(item);
            if (button != null) {
                hBoxContainer.getChildren().add(button);
            }
        }

        // resize DockView window to account for DockItem additions or removing
        stage.sizeToScene();
        appServices.positioningService().applyPosition(stage);
    }

    private Button createButton(DockItem item) {

        if (item instanceof DockSettingsItemModel) {
            Image icon = new Image(getClass().getResourceAsStream(item.getPath()));
            ImageView imageView = new ImageView(icon);
            imageView.setFitWidth(appServices.appearanceService().getIconsSize());
            imageView.setFitHeight(appServices.appearanceService().getIconsSize());

            Button button = new Button(item.getLabel());
            button.getStyleClass().add("dock-button");
            button.setGraphic(imageView);
            button.setOnAction(e -> appServices.itemActionService().execute(item, this::openSettingsWindow));
            return button;

        } else if (item instanceof DockWindowsModuleItemModel) {
            Image icon = new Image(getClass().getResourceAsStream(item.getPath()));
            ImageView imageView = new ImageView(icon);
            imageView.setFitWidth(appServices.appearanceService().getIconsSize());
            imageView.setFitHeight(appServices.appearanceService().getIconsSize());

            Button button = new Button(item.getLabel());
            button.getStyleClass().add("dock-button");
            button.setGraphic(imageView);
            button.setOnAction(e -> appServices.itemActionService().execute(item, this::openSettingsWindow));
            return button;
        } else if (item instanceof DockProgramItemModel) {
            // Logic for DockProgramItemModel runs on a background thread.
            Path iconPath = appServices.iconGateway().resolveProgramIcon(((DockProgramItemModel) item).getExecutablePath());

            // if file does not exist
            if (iconPath == null || Files.notExists(iconPath)) {
                Logger.error("DockController - createButton - path for cached icon not found");
                return null;
            }

            Image icon = new Image(iconPath.toUri().toString());
            ImageView imageView = new ImageView(icon);
            imageView.setFitWidth(appServices.appearanceService().getIconsSize());
            imageView.setFitHeight(appServices.appearanceService().getIconsSize());

            Button button = new Button(item.getLabel());
            button.getStyleClass().add("dock-button");
            imageView.setFitWidth(appServices.appearanceService().getIconsSize());
            imageView.setFitHeight(appServices.appearanceService().getIconsSize());
            button.setGraphic(imageView);

            button.setOnAction(e -> appServices.itemActionService().execute(item, this::openSettingsWindow));

            // Show a window list preview when hovering this program icon.
            setupHoverPreview(button, (DockProgramItemModel) item, icon);

            return button;

        } else {
            return null;
        }
    }

    private void setupHoverPreview(Button button, DockProgramItemModel item, Image icon) {
        // Track hover state for the icon and popup to avoid flicker.
        button.setOnMouseEntered(e -> {
            currentHoverButton = button;
            hideDebounce.stop();
            // If another icon owns the popup, close it before showing new content.
            if (windowPreviewPopup.isShowing() && popupOwnerButton != button) {
                windowPreviewPopup.hide();
                popupOwnerButton = null;
            }
            showWindowPreview(button, item, icon);
        });

        button.setOnMouseExited(e -> {
            if (currentHoverButton == button) {
                currentHoverButton = null;
            }
            // Hide with a short debounce to allow moving into the popup.
            scheduleHide();
        });
    }

    private void showWindowPreview(Button button, DockProgramItemModel item, Image icon) {
        int requestId = ++hoverRequestId;
        // Query native windows on a background thread.
        Task<List<NativeWindowUtils.WindowInfo>> task = new Task<>() {
            @Override
            protected List<NativeWindowUtils.WindowInfo> call() throws Exception {
                return appServices.windowPreviewService().loadPreview(item);
            }
        };

        task.setOnSucceeded(e -> {
            // Ignore results from older hover requests.
            if (requestId != hoverRequestId) {
                return;
            }
            List<NativeWindowUtils.WindowInfo> windows = task.getValue();
            // If the mouse left the icon, do nothing.
            if (currentHoverButton != button || !button.isHover()) {
                return;
            }
            // Only show popup when there is at least one window.
            if (!windows.isEmpty()) {
                DockTheme dockTheme = appServices.appearanceService().getDockTheme();
                windowPreviewPopup.updateContent(
                        windows,
                        icon,
                        item.getLabel(),
                        dockTheme,
                        appServices.windowPreviewService()::activate
                );
                windowPreviewPopup.showAbove(button, hBoxContainer);
                popupOwnerButton = button;

            } else if (windowPreviewPopup.isShowing() && popupOwnerButton == button) {
                windowPreviewPopup.hide();
                popupOwnerButton = null;
            }
        });

        task.setOnFailed(e -> {
            Logger.error("Failed to fetch windows for " + item.getLabel() + ": " + task.getException().getMessage());
        });

        windowPreviewExecutor.execute(task);
    }

    private void scheduleHide() {
        hideDebounce.stop();
        hideDebounce.playFromStart();
    }

    private boolean shouldHidePreview() {
        if (isHoveringPopup) {
            return false;
        }
        if (currentHoverButton == null) {
            return true;
        }
        return !currentHoverButton.isHover();
    }

    private void openSettingsWindow() {
        try {
            FXMLLoader loader = new FXMLLoader(App.class.getResource("fxml/DockSettingsView.fxml"));
            Parent root = loader.load();

            SettingsController settingsController = loader.getController();
            settingsController.setAppServices(appServices);
            settingsController.setDockRefreshAction(this::updateDockUI);
            settingsController.setPositioningModeChangeAction(this::handlePositioningModeChange);
            settingsController.handleInitialization();

            Stage stage = new Stage();
            stage.setTitle("Settings Window");
            setStageIcon(stage);
            stage.setScene(new Scene(root));
            stage.show();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    public void setDockIconsSize(int iconsSize) {
        appServices.appearanceService().setIconsSize(iconsSize);
        updateDockUI();
    }

    public int getDockIconsSize() {
        return appServices.appearanceService().getIconsSize();
    }

    public void setDockIconsSpacing(int spacingValue) {
        appServices.appearanceService().setSpacingBetweenIcons(spacingValue);
        updateDockUI();
    }

    public int getDockIconsSpacing() {
        return appServices.appearanceService().getSpacingBetweenIcons();
    }

    public int getDockTransparency() {
        return appServices.appearanceService().getDockTransparencyPercentage();
    }

    public void setDockTransparency(int value) {
        appServices.appearanceService().setDockTransparencyPercentage(value);
        updateDockUI();
    }

    public void setDockBorderRounding(int value) {
        appServices.appearanceService().setDockBorderRounding(value);
        updateDockUI();

    }

    public int getDockBorderRounding() {
        return appServices.appearanceService().getDockBorderRounding();
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public String getDockColorRGB() {
        return appServices.appearanceService().getDockColorRGB();
    }

    public void setDockColorRGB(String value) {
        appServices.appearanceService().setDockColorRGB(value);
        updateDockUI();
    }

    public void saveChanges() {
        appServices.dockService().saveChanges();
    }

    public void setAppServices(AppServices appServices) {
        this.appServices = appServices;
    }

    private void handlePositioningModeChange(DockPositioningMode positioningMode) {
        DockPositioningMode currentMode = appServices.positioningService().getPositioningMode();
        if (currentMode == DockPositioningMode.STATIC && positioningMode == DockPositioningMode.DYNAMIC) {
            appServices.dockService().setDockPosition(stage.getX(), stage.getY());
        }
        appServices.positioningService().setPositioningMode(positioningMode);
    }
}
