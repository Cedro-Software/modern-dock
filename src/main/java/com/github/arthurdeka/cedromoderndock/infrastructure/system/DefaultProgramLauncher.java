package com.github.arthurdeka.cedromoderndock.infrastructure.system;

import com.github.arthurdeka.cedromoderndock.domain.ProgramLauncher;
import com.github.arthurdeka.cedromoderndock.util.Logger;

import java.io.IOException;

public class DefaultProgramLauncher implements ProgramLauncher {
    @Override
    public void launch(String executablePath, String label) {
        Logger.info(label + " Clicked");

        if (executablePath == null || executablePath.trim().isEmpty()) {
            Logger.error("Executable path not defined for: " + label);
            return;
        }

        try {
            executeAndHandleElevation(executablePath, label);
        } catch (IOException e) {
            Logger.error("Failed to open: " + label);
            Logger.error("Path: " + executablePath);
            Logger.error("Error: " + e.getMessage());
        } catch (InterruptedException e) {
            Logger.error("Process interrupted: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private void executeAndHandleElevation(String path, String label) throws IOException, InterruptedException {
        try {
            new ProcessBuilder(path).start();
            Logger.info("Executing: " + label);
        } catch (IOException e) {
            if (e.getMessage() != null && e.getMessage().contains("error=740")) {
                Logger.info("Standard execution failed. Requesting elevation...");
                String command = "Start-Process -FilePath '" + path + "' -Verb RunAs";
                new ProcessBuilder("powershell.exe", "-Command", command).start();
                Logger.info("(Elevated) Executing: " + label);
            } else {
                Logger.error("Error trying to execute program");
                throw e;
            }
        }
    }
}
