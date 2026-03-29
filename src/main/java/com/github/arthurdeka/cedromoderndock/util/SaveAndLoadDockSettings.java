package com.github.arthurdeka.cedromoderndock.util;

import com.github.arthurdeka.cedromoderndock.domain.DockRepository;
import com.github.arthurdeka.cedromoderndock.infrastructure.persistence.JsonDockRepository;
import com.github.arthurdeka.cedromoderndock.model.DockModel;

/**
 * Utility class used to save and load the Dock's settings
 */
public final class SaveAndLoadDockSettings {
    private static final DockRepository REPOSITORY = new JsonDockRepository();

    private SaveAndLoadDockSettings() {}

    /**
     * saves the DockModel object in the config file (config.json).
     * @param model DockModel object to be saved.
     */
    public static void save(DockModel model) {
        REPOSITORY.save(model);
    }

    /**
     * This method loads the currennt config.json file and returns a DockModel.
     * If the file is corrupted or does not exist, a new one with only a DockSettings item is created and returned
     *
     * @return A DockModel instance, always valid (loaded or default).
     */
    public static DockModel load() {
        return REPOSITORY.load();
    }
}
