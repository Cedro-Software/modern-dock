package com.github.arthurdeka.cedromoderndock.controller;

import com.github.arthurdeka.cedromoderndock.App;
import com.github.arthurdeka.cedromoderndock.application.AppServices;
import com.github.arthurdeka.cedromoderndock.model.DockItem;
import com.github.arthurdeka.cedromoderndock.model.DockProgramItemModel;
import com.github.arthurdeka.cedromoderndock.model.DockSettingsItemModel;
import com.github.arthurdeka.cedromoderndock.util.ColorManipulation;
import com.github.arthurdeka.cedromoderndock.util.Logger;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ListView;
import javafx.scene.control.Slider;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;

import static com.github.arthurdeka.cedromoderndock.util.UIUtils.setStageIcon;

public class SettingsController {

    // Icons tab
    @FXML
    private ListView listView;

    @FXML
    private Button addProgramButton;
    @FXML
    private Button removeProgramButton;
    @FXML
    private Button moveItemUpButton;
    @FXML
    private Button moveItemDownButton;

    private ObservableList<String> listItems = FXCollections.observableArrayList();

    // Icons customization tab

    @FXML
    private Slider iconSizeSlider;
    @FXML
    private Slider spacingBetweenIconsSlider;

    // Dock Customization tab

    @FXML
    private Slider dockTransparencySlider;
    @FXML
    private Slider dockBorderRoundingSlider;
    @FXML
    private ColorPicker dockColorPicker;

    // misc
    private AppServices appServices;
    private Runnable dockRefreshAction = () -> {};


    // Run when FXML is loaded
    public void initialize() {
        Logger.info("[Initializing] SettingsController");

    }

    public void handleInitialization() {

        // add listener to listView
        addDockItemsToListView(appServices.dockService().getItems());
        listView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener() {
            @Override
            public void changed(ObservableValue observableValue, Object oldValue, Object newValue) {
                handleListViewItemSelection();
            }
        });


        // add listener to sliders
        iconSizeSlider.setValue(appServices.appearanceService().getIconsSize());
        iconSizeSlider.valueProperty().addListener(((observableValue, oldValue, newValue) -> {
            int value = (int) iconSizeSlider.getValue();
            handleSetIconSizeSlider(value);
        }));

        spacingBetweenIconsSlider.setValue(appServices.appearanceService().getSpacingBetweenIcons());
        spacingBetweenIconsSlider.valueProperty().addListener(((observableValue, oldValue, newValue) -> {
            int value = (int) spacingBetweenIconsSlider.getValue();
            handleSetIconsSpacingSlider(value);
        }));

        dockTransparencySlider.setValue(appServices.appearanceService().getDockTransparencyPercentage());
        dockTransparencySlider.valueProperty().addListener(((observableValue, oldValue, newValue) -> {
            int value = (int) dockTransparencySlider.getValue();
            handleSetDockTransparencySlider(value);
        }));

        dockBorderRoundingSlider.setValue(appServices.appearanceService().getDockBorderRounding());
        dockBorderRoundingSlider.valueProperty().addListener(((observableValue, oldValue, newValue) -> {
            int value = (int) dockBorderRoundingSlider.getValue();
            handleSetDockBorderRoundingSlider(value);
        }));

        // set current color in colorpicker
        Color RGBAcolor = ColorManipulation.fromRGBtoRGBA(appServices.appearanceService().getDockColorRGB());
        dockColorPicker.setValue(RGBAcolor);

    }


    private void handleListViewItemSelection() {
        int selectedIdx = listView.getSelectionModel().getSelectedIndex();
        if (selectedIdx < 0) {
            removeProgramButton.setDisable(true);
            moveItemUpButton.setDisable(true);
            moveItemDownButton.setDisable(true);
            return;
        }
        DockItem item = appServices.dockService().getItems().get(selectedIdx);

        // disables removeProgramButton if the selected item is the Settings item
        if (item instanceof DockSettingsItemModel) {
            removeProgramButton.setDisable(true);
        } else {
            removeProgramButton.setDisable(false);

        }


        // disables moveItemUpButton if item is already at top or bottom of the lsit.
        if (selectedIdx == 0) {
            moveItemUpButton.setDisable(true);
        } else {
            moveItemUpButton.setDisable(false);
        }

        // disables moveItemDownButton if item is already at top or bottom of the lsit.
        if (selectedIdx == listItems.size() - 1) {
            moveItemDownButton.setDisable(true);

        } else {
            moveItemDownButton.setDisable(false);
        }


    }

    private void addDockItemsToListView(List<DockItem> DockItems) {

        listItems.clear();

        for (DockItem item : DockItems) {
            listItems.add(item.getLabel());
            Logger.info("[Initializing][listView] Adding item to ListView: " + item.getLabel());
        }

        // list view will always follow ObservableList<String> listItems
        listView.setItems(listItems);

    }

    // Icons tab

    @FXML
    private void openAddWindowsModuleWindow() {
        // opens AddWindowsModulesWindowView and creates a new instance of AddWindowsModulesModalController
        try {

            // Get reference to the current window/stage
            Stage currentStage = (Stage) listView.getScene().getWindow();

            FXMLLoader loader = new FXMLLoader(App.class.getResource("fxml/AddWindowsModulesModalView.fxml"));
            Parent root = loader.load();

            AddWindowsModulesModalController addWindowsModulesModalController = loader.getController();
            addWindowsModulesModalController.setAppServices(appServices);
            addWindowsModulesModalController.setDockRefreshAction(dockRefreshAction);

            Stage stage = new Stage();
            stage.setTitle("Add Windows Module");
            setStageIcon(stage);
            stage.setScene(new Scene(root));
            stage.show();

            // Close the current window
            currentStage.close();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    private void handleAddProgram() {
        // opens a file chooser and creates a new DockItem using file path and file icon info
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose .exe");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Executable", "*.exe"));

        File file = fileChooser.showOpenDialog(null);

        if (file != null) {
            // obs: the .exe file path will be used to extract the icon path as well.
            String selectedExePath = file.getAbsolutePath();
            String selectedExeName = Paths.get(file.getAbsolutePath()).getFileName().toString().replace(".exe", "");

            //extracting icon
            appServices.iconGateway().cacheProgramIcon(selectedExePath);

            // saving to dock
            DockItem newItem = new DockProgramItemModel(selectedExeName, selectedExePath);
            appServices.dockService().addItem(newItem);
            Logger.info("[listView] Program added: " + selectedExeName);

        }

        addDockItemsToListView(appServices.dockService().getItems());
        dockRefreshAction.run();

    }

    @FXML
    private void handleRemoveProgram() {
        int selectedIdx = listView.getSelectionModel().getSelectedIndex();

        // deletes selected option
        Logger.info("[listView] Removing item on index: " + selectedIdx);

        appServices.dockService().removeItem(selectedIdx);
        listItems.remove(selectedIdx);

        dockRefreshAction.run();

    }

    @FXML
    private void handleMoveItem(ActionEvent event) {
        int selectedIdx = listView.getSelectionModel().getSelectedIndex();

        if (event.getSource() == moveItemUpButton) {
            Logger.info("[listView] moving item up");
            Collections.swap(listItems, selectedIdx, selectedIdx - 1);
            appServices.dockService().swapItems(selectedIdx, selectedIdx - 1);

            // set new position as selected
            listView.getSelectionModel().select(selectedIdx - 1);

        } else {
            Logger.info("[listView] moving item down");
            Collections.swap(listItems, selectedIdx, selectedIdx + 1);
            appServices.dockService().swapItems(selectedIdx, selectedIdx + 1);

            // set new position as selected
            listView.getSelectionModel().select(selectedIdx + 1);

        }

        handleListViewItemSelection();


    }

    // Icons customization tab

    private void handleSetIconSizeSlider(int value) {
        appServices.appearanceService().setIconsSize(value);
        dockRefreshAction.run();
    }

    private void handleSetIconsSpacingSlider(int value) {
        appServices.appearanceService().setSpacingBetweenIcons(value);
        dockRefreshAction.run();
    }

    // dock customization tab

    private void handleSetDockTransparencySlider(int value) {
        appServices.appearanceService().setDockTransparencyPercentage(value);
        dockRefreshAction.run();
    }

    private void handleSetDockBorderRoundingSlider(int value) {
        appServices.appearanceService().setDockBorderRounding(value);
        dockRefreshAction.run();
    }

    @FXML
    private void handleSetDockColor() {
        String RGBAColor = String.valueOf(dockColorPicker.getValue());
        String RGBColor = ColorManipulation.fromRGBAtoRGB(RGBAColor);

        appServices.appearanceService().setDockColorRGB(RGBColor);
        dockRefreshAction.run();

    }

    // misc ======

    public void setAppServices(AppServices appServices) {
        this.appServices = appServices;
    }

    public void setDockRefreshAction(Runnable dockRefreshAction) {
        this.dockRefreshAction = dockRefreshAction;
    }

    @FXML
    private void openAknowledgementsWindow() {
        try {
            FXMLLoader loader = new FXMLLoader(App.class.getResource("fxml/AcknowledgementsModalView.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Acknowledgements");
            stage.setScene(new Scene(root));
            stage.show();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}


