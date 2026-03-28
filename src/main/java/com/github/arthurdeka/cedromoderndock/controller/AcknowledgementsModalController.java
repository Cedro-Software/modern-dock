package com.github.arthurdeka.cedromoderndock.controller;

import com.github.arthurdeka.cedromoderndock.application.AppServices;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class AcknowledgementsModalController {
    @FXML
    private Label titleLabel;
    @FXML
    private Label subtitleLabel;
    @FXML
    private Label sectionTitleLabel;

    private AppServices appServices;
    private final Runnable localizationListener = this::applyLocalizedTexts;
    private boolean localizationRegistered;
    private boolean cleanupRegistered;

    public void initialize() {
        titleLabel.sceneProperty().addListener((observableValue, oldScene, newScene) -> {
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

    public void setAppServices(AppServices appServices) {
        this.appServices = appServices;
    }

    private void applyLocalizedTexts() {
        if (appServices == null) {
            return;
        }

        titleLabel.setText(text("acknowledgements.title"));
        subtitleLabel.setText(text("acknowledgements.subtitle"));
        sectionTitleLabel.setText(text("acknowledgements.sectionTitle"));
        updateWindowTitle();
    }

    private void updateWindowTitle() {
        if (titleLabel.getScene() != null && titleLabel.getScene().getWindow() instanceof Stage stage && appServices != null) {
            stage.setTitle(text("acknowledgements.window.title"));
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
