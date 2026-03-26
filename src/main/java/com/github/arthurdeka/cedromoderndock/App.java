package com.github.arthurdeka.cedromoderndock;

import com.github.arthurdeka.cedromoderndock.application.AppServices;
import com.github.arthurdeka.cedromoderndock.application.DockAppearanceService;
import com.github.arthurdeka.cedromoderndock.application.DockItemActionService;
import com.github.arthurdeka.cedromoderndock.application.DockPositioningService;
import com.github.arthurdeka.cedromoderndock.application.DockService;
import com.github.arthurdeka.cedromoderndock.application.WindowPreviewService;
import com.github.arthurdeka.cedromoderndock.controller.DockController;
import com.github.arthurdeka.cedromoderndock.infrastructure.persistence.JsonDockRepository;
import com.github.arthurdeka.cedromoderndock.infrastructure.system.CachedWindowsIconGateway;
import com.github.arthurdeka.cedromoderndock.infrastructure.system.DefaultFolderLauncher;
import com.github.arthurdeka.cedromoderndock.infrastructure.system.DefaultProgramLauncher;
import com.github.arthurdeka.cedromoderndock.infrastructure.system.DefaultWindowsModuleLauncher;
import com.github.arthurdeka.cedromoderndock.infrastructure.system.JnaWindowQueryGateway;
import com.github.arthurdeka.cedromoderndock.util.SingleInstanceGuard;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import javax.swing.JOptionPane;
import java.io.IOException;

import static com.github.arthurdeka.cedromoderndock.util.UIUtils.setStageIcon;

public class App extends Application {
    private static SingleInstanceGuard singleInstanceGuard;

    @Override
    public void start(Stage primaryStage) throws IOException {
        AppServices appServices = createServices();

        // invisble primary stage to dont show the dock icon in the taskbar
        primaryStage.initStyle(StageStyle.UTILITY);
        primaryStage.setOpacity(0);
        primaryStage.show();

        // creates a new stage for the dock
        Stage dockStage = new Stage();

        // loading dock interface and controller.
        FXMLLoader loader = new FXMLLoader(App.class.getResource("fxml/DockView.fxml"));
        Scene scene = new Scene(loader.load());

        // configuring dock stage.
        dockStage.setTitle("Cedro Modern Dock");
        setStageIcon(dockStage);
        // defining the invisible window as the "owner" of the dock (this makes the dock invisible).
        dockStage.initOwner(primaryStage);
        dockStage.initStyle(StageStyle.TRANSPARENT);
        scene.setFill(Color.TRANSPARENT);
        dockStage.setScene(scene);

        DockController dockController = loader.getController();
        dockController.setStage(dockStage);
        dockController.setAppServices(appServices);
        dockController.handleInitialization();

        dockStage.show();
        appServices.positioningService().applyPosition(dockStage);
    }

    @Override
    public void stop() {
        if (singleInstanceGuard != null) {
            singleInstanceGuard.close();
            singleInstanceGuard = null;
        }
    }

    public static void main(String[] args) {
        singleInstanceGuard = new SingleInstanceGuard();
        if (!singleInstanceGuard.tryAcquire()) {
            JOptionPane.showMessageDialog(
                    null,
                    "There is already an instance of Cedro Modern Dock running. Check your desktop",
                    "Cedro Modern Dock",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        try {
            launch();
        } finally {
            if (singleInstanceGuard != null) {
                singleInstanceGuard.close();
                singleInstanceGuard = null;
            }
        }
    }

    private AppServices createServices() {
        DockService dockService = new DockService(new JsonDockRepository());
        DockAppearanceService appearanceService = new DockAppearanceService(dockService);
        DockPositioningService positioningService = new DockPositioningService(dockService);
        DockItemActionService itemActionService = new DockItemActionService(
                new DefaultProgramLauncher(),
                new DefaultFolderLauncher(),
                new DefaultWindowsModuleLauncher()
        );
        WindowPreviewService windowPreviewService = new WindowPreviewService(new JnaWindowQueryGateway());

        return new AppServices(
                dockService,
                appearanceService,
                positioningService,
                itemActionService,
                windowPreviewService,
                new CachedWindowsIconGateway()
        );
    }
}
