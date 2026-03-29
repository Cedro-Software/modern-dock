package com.github.arthurdeka.cedromoderndock.infrastructure.persistence;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.github.arthurdeka.cedromoderndock.domain.DockRepository;
import com.github.arthurdeka.cedromoderndock.model.DockModel;
import com.github.arthurdeka.cedromoderndock.util.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public final class JsonDockRepository implements DockRepository {
    private static final String CONFIG_FILE_NAME = "config.json";
    private final Path configFilePath;
    private final ObjectMapper mapper;

    public JsonDockRepository() {
        this(getDefaultConfigPath());
    }

    public JsonDockRepository(Path configFilePath) {
        this.configFilePath = configFilePath;
        this.mapper = createObjectMapper();
    }

    private static Path getDefaultConfigPath() {
        String appDataPath = System.getenv("APPDATA");
        if (appDataPath == null || appDataPath.isEmpty()) {
            appDataPath = System.getProperty("user.home");
        }

        Path configDir = Paths.get(appDataPath, "CedroModernDock");
        File dir = configDir.toFile();
        if (!dir.exists()) {
            dir.mkdirs();
        }

        return configDir.resolve(CONFIG_FILE_NAME);
    }

    private static ObjectMapper createObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        return objectMapper;
    }

    @Override
    public void save(DockModel model) {
        try {
            mapper.writeValue(configFilePath.toFile(), model);
        } catch (IOException e) {
            Logger.error("Error saving the DockModel: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public DockModel load() {
        File configFile = configFilePath.toFile();
        if (configFile.exists() && configFile.length() > 0) {
            try {
                DockModel model = mapper.readValue(configFile, DockModel.class);
                Logger.info("[JsonDockRepository] Dock config.json loaded successfully from: " + configFilePath);
                return model;
            } catch (IOException e) {
                Logger.error("Error reading config.json, creating a new default config file: " + e.getMessage());
                return createAndSaveDefault();
            }
        }

        Logger.error("config.json not found or is empty. Creating a new default config file at: " + configFilePath);
        return createAndSaveDefault();
    }

    private DockModel createAndSaveDefault() {
        DockModel model = new DockModel();
        model.loadDefaultItems();
        save(model);
        return model;
    }
}
