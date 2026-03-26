package com.github.arthurdeka.cedromoderndock.controller;

import com.github.arthurdeka.cedromoderndock.App;
import com.github.arthurdeka.cedromoderndock.application.AppServices;
import com.github.arthurdeka.cedromoderndock.model.DockHorizontalAnchor;
import com.github.arthurdeka.cedromoderndock.model.DockItem;
import com.github.arthurdeka.cedromoderndock.model.DockFolderItemModel;
import com.github.arthurdeka.cedromoderndock.model.DockPositioningMode;
import com.github.arthurdeka.cedromoderndock.model.DockProgramItemModel;
import com.github.arthurdeka.cedromoderndock.model.DockSettingsItemModel;
import com.github.arthurdeka.cedromoderndock.model.DockVerticalAnchor;
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
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Button;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ListView;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Slider;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import static com.github.arthurdeka.cedromoderndock.util.UIUtils.setStageIcon;

public class SettingsController {
    private static final double LIST_VIEW_CELL_HEIGHT = 40;
    private static final double LIST_VIEW_MAX_HEIGHT = 200;

    // Icons tab
    @FXML
    private ListView<String> listView;

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

    // Dock positioning tab
    @FXML
    private RadioButton staticPositioningRadio;
    @FXML
    private RadioButton dynamicPositioningRadio;
    @FXML
    private ChoiceBox<String> verticalPositionChoiceBox;
    @FXML
    private ChoiceBox<String> horizontalPositionChoiceBox;
    @FXML
    private Slider topSpacingSlider;
    @FXML
    private Slider leftSpacingSlider;
    @FXML
    private Slider rightSpacingSlider;
    @FXML
    private Slider bottomSpacingSlider;
    @FXML
    private VBox staticPositioningPane;
    @FXML
    private VBox dynamicPositioningPane;

    // misc
    private AppServices appServices;
    private Runnable dockRefreshAction = () -> {};
    private Consumer<DockPositioningMode> positioningModeChangeAction = positioningMode -> {};


    // Run when FXML is loaded
    public void initialize() {
        Logger.info("[Initializing] SettingsController");
        ToggleGroup positioningModeGroup = new ToggleGroup();
        staticPositioningRadio.setToggleGroup(positioningModeGroup);
        dynamicPositioningRadio.setToggleGroup(positioningModeGroup);
        listView.setFixedCellSize(LIST_VIEW_CELL_HEIGHT);
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

        initializePositioningControls();

    }

    private void initializePositioningControls() {
        verticalPositionChoiceBox.setItems(FXCollections.observableArrayList("Top", "Middle", "Down"));
        horizontalPositionChoiceBox.setItems(FXCollections.observableArrayList("Left", "Middle", "Right"));

        DockPositioningMode positioningMode = appServices.positioningService().getPositioningMode();
        if (positioningMode == DockPositioningMode.DYNAMIC) {
            dynamicPositioningRadio.setSelected(true);
        } else {
            staticPositioningRadio.setSelected(true);
        }

        verticalPositionChoiceBox.setValue(toVerticalLabel(appServices.positioningService().getVerticalAnchor()));
        horizontalPositionChoiceBox.setValue(toHorizontalLabel(appServices.positioningService().getHorizontalAnchor()));
        topSpacingSlider.setValue(appServices.positioningService().getTopSpacing());
        leftSpacingSlider.setValue(appServices.positioningService().getLeftSpacing());
        rightSpacingSlider.setValue(appServices.positioningService().getRightSpacing());
        bottomSpacingSlider.setValue(appServices.positioningService().getBottomSpacing());

        verticalPositionChoiceBox.getSelectionModel().selectedItemProperty().addListener((observableValue, oldValue, newValue) -> {
            if (newValue != null) {
                appServices.positioningService().setVerticalAnchor(toVerticalAnchor(newValue));
                dockRefreshAction.run();
            }
        });

        horizontalPositionChoiceBox.getSelectionModel().selectedItemProperty().addListener((observableValue, oldValue, newValue) -> {
            if (newValue != null) {
                appServices.positioningService().setHorizontalAnchor(toHorizontalAnchor(newValue));
                dockRefreshAction.run();
            }
        });

        topSpacingSlider.valueProperty().addListener((observableValue, oldValue, newValue) -> {
            appServices.positioningService().setTopSpacing((int) Math.round(newValue.doubleValue()));
            dockRefreshAction.run();
        });

        leftSpacingSlider.valueProperty().addListener((observableValue, oldValue, newValue) -> {
            appServices.positioningService().setLeftSpacing((int) Math.round(newValue.doubleValue()));
            dockRefreshAction.run();
        });

        rightSpacingSlider.valueProperty().addListener((observableValue, oldValue, newValue) -> {
            appServices.positioningService().setRightSpacing((int) Math.round(newValue.doubleValue()));
            dockRefreshAction.run();
        });

        bottomSpacingSlider.valueProperty().addListener((observableValue, oldValue, newValue) -> {
            appServices.positioningService().setBottomSpacing((int) Math.round(newValue.doubleValue()));
            dockRefreshAction.run();
        });

        updatePositioningModeUI();
    }

    @FXML
    private void handlePositioningModeChange() {
        DockPositioningMode positioningMode = staticPositioningRadio.isSelected()
                ? DockPositioningMode.STATIC
                : DockPositioningMode.DYNAMIC;
        positioningModeChangeAction.accept(positioningMode);
        updatePositioningModeUI();
        dockRefreshAction.run();
    }

    private void updatePositioningModeUI() {
        boolean isStaticMode = staticPositioningRadio.isSelected();
        staticPositioningPane.setVisible(isStaticMode);
        staticPositioningPane.setManaged(isStaticMode);
        dynamicPositioningPane.setVisible(!isStaticMode);
        dynamicPositioningPane.setManaged(!isStaticMode);
    }

    private String toVerticalLabel(DockVerticalAnchor verticalAnchor) {
        return switch (verticalAnchor) {
            case TOP -> "Top";
            case MIDDLE -> "Middle";
            case DOWN -> "Down";
        };
    }

    private DockVerticalAnchor toVerticalAnchor(String value) {
        return switch (value) {
            case "Middle" -> DockVerticalAnchor.MIDDLE;
            case "Down" -> DockVerticalAnchor.DOWN;
            default -> DockVerticalAnchor.TOP;
        };
    }

    private String toHorizontalLabel(DockHorizontalAnchor horizontalAnchor) {
        return switch (horizontalAnchor) {
            case LEFT -> "Left";
            case MIDDLE -> "Middle";
            case RIGHT -> "Right";
        };
    }

    private DockHorizontalAnchor toHorizontalAnchor(String value) {
        return switch (value) {
            case "Middle" -> DockHorizontalAnchor.MIDDLE;
            case "Right" -> DockHorizontalAnchor.RIGHT;
            default -> DockHorizontalAnchor.LEFT;
        };
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
        updateListViewHeight();

    }

    private void updateListViewHeight() {
        int visibleRows = Math.max(1, listItems.size());
        double contentHeight = visibleRows * listView.getFixedCellSize() + 2;
        double boundedHeight = Math.min(contentHeight, LIST_VIEW_MAX_HEIGHT);

        listView.setMinHeight(boundedHeight);
        listView.setPrefHeight(boundedHeight);
        listView.setMaxHeight(LIST_VIEW_MAX_HEIGHT);
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
            addWindowsModulesModalController.setPositioningModeChangeAction(positioningModeChangeAction);

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
    private void handleAddFolder() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Choose folder");

        Stage owner = (Stage) listView.getScene().getWindow();
        File selectedFolder = directoryChooser.showDialog(owner);

        if (selectedFolder == null || !selectedFolder.isDirectory()) {
            return;
        }

        String folderPath = selectedFolder.getAbsolutePath();
        String folderName = selectedFolder.getName();
        String label = (folderName == null || folderName.isBlank()) ? folderPath : folderName;

        appServices.iconGateway().cacheFolderIcon(folderPath);
        DockItem newItem = new DockFolderItemModel(label, folderPath);
        appServices.dockService().addItem(newItem);
        Logger.info("[listView] Folder added: " + label);

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
        if (selectedIdx < 0) {
            return;
        }

        if (event.getSource() == moveItemUpButton) {
            if (selectedIdx == 0) {
                return;
            }
            Logger.info("[listView] moving item up");
            Collections.swap(listItems, selectedIdx, selectedIdx - 1);
            appServices.dockService().swapItems(selectedIdx, selectedIdx - 1);

            // set new position as selected
            listView.getSelectionModel().select(selectedIdx - 1);

        } else {
            if (selectedIdx >= listItems.size() - 1) {
                return;
            }
            Logger.info("[listView] moving item down");
            Collections.swap(listItems, selectedIdx, selectedIdx + 1);
            appServices.dockService().swapItems(selectedIdx, selectedIdx + 1);

            // set new position as selected
            listView.getSelectionModel().select(selectedIdx + 1);

        }

        handleListViewItemSelection();
        dockRefreshAction.run();


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

    public void setPositioningModeChangeAction(Consumer<DockPositioningMode> positioningModeChangeAction) {
        this.positioningModeChangeAction = positioningModeChangeAction;
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


