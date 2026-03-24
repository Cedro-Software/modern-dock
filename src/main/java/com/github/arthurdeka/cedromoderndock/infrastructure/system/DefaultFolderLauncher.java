package com.github.arthurdeka.cedromoderndock.infrastructure.system;

import com.github.arthurdeka.cedromoderndock.domain.FolderLauncher;
import com.github.arthurdeka.cedromoderndock.util.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class DefaultFolderLauncher implements FolderLauncher {
    @Override
    public void launch(String folderPath, String label) {
        Logger.info(label + " Clicked");

        if (folderPath == null || folderPath.trim().isEmpty()) {
            Logger.error("Folder path not defined for: " + label);
            return;
        }

        Path path = Path.of(folderPath);
        if (!Files.isDirectory(path)) {
            Logger.error("Folder path is invalid: " + folderPath);
            return;
        }

        try {
            new ProcessBuilder("explorer.exe", folderPath).start();
            Logger.info("Opening folder: " + label);
        } catch (IOException e) {
            Logger.error("Failed to open folder: " + label);
            Logger.error("Path: " + folderPath);
            Logger.error("Error: " + e.getMessage());
        }
    }
}
