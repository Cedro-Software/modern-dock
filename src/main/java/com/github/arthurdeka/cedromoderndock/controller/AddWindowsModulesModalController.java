package com.github.arthurdeka.cedromoderndock.controller;

import com.github.arthurdeka.cedromoderndock.App;
import com.github.arthurdeka.cedromoderndock.application.AppServices;
import com.github.arthurdeka.cedromoderndock.model.DockPositioningMode;
import com.github.arthurdeka.cedromoderndock.model.DockWindowsModuleItemModel;
import com.github.arthurdeka.cedromoderndock.util.Logger;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.util.List;
import java.util.function.Consumer;

import static com.github.arthurdeka.cedromoderndock.util.UIUtils.setStageIcon;

public class AddWindowsModulesModalController {
    private static final List<String> MODULE_IDS = List.of("mypc", "trash", "ctrlpnl", "pconfig");

    @FXML
    private Label titleLabel;
    @FXML
    private Label subtitleLabel;
    @FXML
    private Label availableModulesTitleLabel;
    @FXML
    private Label availableModulesHelperLabel;
    @FXML
    private ListView<String> listView;
    @FXML
    private Button addSelectedModuleButton;

    private AppServices appServices;
    private Runnable dockRefreshAction = () -> {};
    private Consumer<DockPositioningMode> positioningModeChangeAction = positioningMode -> {};
    private final Runnable localizationListener = this::applyLocalizedTexts;
    private boolean localizationRegistered;
    private boolean cleanupRegistered;

    public void initialize() {
        Logger.info("[Initializing] AddWindowsModulesModalController");
        listView.sceneProperty().addListener((observableValue, oldScene, newScene) -> {
            if (newScene == null) {
                return;
            }

            newScene.windowProperty().addListener((windowObservable, oldWindow, newWindow) -> {
                if (newWindow == null) {
                    return;
                }
                updateWindowTitle();
                if (!cleanupRegistered) {
                    cleanupRegistered = true;
                    newWindow.addEventHandler(WindowEvent.WINDOW_HIDDEN, event -> unregisterLocalizationListener());
                }
            });
        });
    }

    public void handleInitialization() {
        if (!localizationRegistered) {
            appServices.localizationService().addListener(localizationListener);
            localizationRegistered = true;
        }
        applyLocalizedTexts();
    }

    @FXML
    private void handleAddSelectedModule() {
        int selectedIdx = listView.getSelectionModel().getSelectedIndex();
        if (selectedIdx < 0 || selectedIdx >= MODULE_IDS.size()) {
            return;
        }

        String moduleId = MODULE_IDS.get(selectedIdx);
        appServices.dockService().addItem(new DockWindowsModuleItemModel(defaultModuleLabel(moduleId), moduleId));
        dockRefreshAction.run();
        openSettingsWindow();
    }

    private void openSettingsWindow() {
        try {
            Stage currentStage = (Stage) listView.getScene().getWindow();

            FXMLLoader loader = new FXMLLoader(App.class.getResource("fxml/DockSettingsView.fxml"));
            Parent root = loader.load();

            SettingsController settingsController = loader.getController();
            settingsController.setAppServices(appServices);
            settingsController.setDockRefreshAction(dockRefreshAction);
            settingsController.setPositioningModeChangeAction(positioningModeChangeAction);
            settingsController.handleInitialization();

            Stage stage = new Stage();
            stage.setTitle(text("settings.window.title"));
            setStageIcon(stage);
            stage.setScene(new Scene(root));
            stage.show();
            stage.setMinHeight(stage.getHeight());
            stage.setMaxHeight(stage.getHeight());

            currentStage.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void setAppServices(AppServices appServices) {
        this.appServices = appServices;
    }

    public void setDockRefreshAction(Runnable dockRefreshAction) {
        this.dockRefreshAction = dockRefreshAction;
    }

    public void setPositioningModeChangeAction(Consumer<DockPositioningMode> positioningModeChangeAction) {
        this.positioningModeChangeAction = positioningModeChangeAction;
    }

    private void applyLocalizedTexts() {
        if (appServices == null) {
            return;
        }

        titleLabel.setText(text("windowsModule.modal.title"));
        subtitleLabel.setText(text("windowsModule.modal.subtitle"));
        availableModulesTitleLabel.setText(text("windowsModule.modal.available.title"));
        availableModulesHelperLabel.setText(text("windowsModule.modal.available.helper"));
        addSelectedModuleButton.setText(text("windowsModule.modal.addSelected"));
        listView.getItems().setAll(MODULE_IDS.stream().map(this::resolveModuleLabel).toList());
        updateWindowTitle();
    }

    private String resolveModuleLabel(String moduleId) {
        return switch (moduleId) {
            case "mypc" -> text("windowsModule.myComputer");
            case "trash" -> text("windowsModule.recycleBin");
            case "ctrlpnl" -> text("windowsModule.controlPanel");
            case "pconfig" -> text("windowsModule.settings");
            default -> moduleId;
        };
    }

    private String defaultModuleLabel(String moduleId) {
        return switch (moduleId) {
            case "mypc" -> "My Computer";
            case "trash" -> "Recycle Bin";
            case "ctrlpnl" -> "Control Panel";
            case "pconfig" -> "Settings";
            default -> moduleId;
        };
    }

    private void updateWindowTitle() {
        if (listView.getScene() != null && listView.getScene().getWindow() instanceof Stage stage && appServices != null) {
            stage.setTitle(text("windowsModule.modal.title"));
        }
    }

    private void unregisterLocalizationListener() {
        if (appServices != null && localizationRegistered) {
            appServices.localizationService().removeListener(localizationListener);
            localizationRegistered = false;
        }
    }

    private String text(String key) {
        return appServices.localizationService().text(key);
    }
}
