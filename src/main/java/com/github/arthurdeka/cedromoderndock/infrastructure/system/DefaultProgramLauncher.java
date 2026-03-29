package com.github.arthurdeka.cedromoderndock.infrastructure.system;

import com.github.arthurdeka.cedromoderndock.domain.ProgramLauncher;
import com.github.arthurdeka.cedromoderndock.util.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;

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
        LaunchCommand launchCommand = resolveLaunchCommand(path);
        try {
            new ProcessBuilder(launchCommand.toCommandLine()).start();
            Logger.info("Executing: " + label);
        } catch (IOException e) {
            if (e.getMessage() != null && e.getMessage().contains("error=740")) {
                Logger.info("Standard execution failed. Requesting elevation...");
                String command = buildElevationCommand(launchCommand);
                new ProcessBuilder("powershell.exe", "-Command", command).start();
                Logger.info("(Elevated) Executing: " + label);
            } else {
                Logger.error("Error trying to execute program");
                throw e;
            }
        }
    }

    static LaunchCommand resolveLaunchCommand(String executablePath) {
        Path normalizedPath = Path.of(executablePath).toAbsolutePath().normalize();
        if (isDiscordExecutable(normalizedPath)) {
            Path installDirectory = normalizedPath.getParent() == null ? null : normalizedPath.getParent().getParent();
            if (installDirectory != null) {
                Path updateExecutable = installDirectory.resolve("Update.exe");
                if (Files.isRegularFile(updateExecutable)) {
                    return new LaunchCommand(
                            updateExecutable.toString(),
                            List.of("--processStart", normalizedPath.getFileName().toString())
                    );
                }
            }
        }

        return new LaunchCommand(normalizedPath.toString(), List.of());
    }

    private static boolean isDiscordExecutable(Path executablePath) {
        Path fileName = executablePath.getFileName();
        Path versionDirectory = executablePath.getParent();
        Path installDirectory = versionDirectory == null ? null : versionDirectory.getParent();
        if (fileName == null || versionDirectory == null || installDirectory == null) {
            return false;
        }

        return fileName.toString().equalsIgnoreCase("Discord.exe")
                && versionDirectory.getFileName() != null
                && versionDirectory.getFileName().toString().toLowerCase(Locale.ROOT).startsWith("app-")
                && installDirectory.getFileName() != null
                && installDirectory.getFileName().toString().equalsIgnoreCase("Discord");
    }

    private static String buildElevationCommand(LaunchCommand launchCommand) {
        String escapedFilePath = escapePowerShellArgument(launchCommand.executablePath());
        if (launchCommand.arguments().isEmpty()) {
            return "Start-Process -FilePath '" + escapedFilePath + "' -Verb RunAs";
        }

        String escapedArguments = launchCommand.arguments().stream()
                .map(DefaultProgramLauncher::escapePowerShellArgument)
                .map(argument -> "'" + argument + "'")
                .reduce((first, second) -> first + ", " + second)
                .orElse("");

        return "Start-Process -FilePath '" + escapedFilePath + "' -ArgumentList " + escapedArguments + " -Verb RunAs";
    }

    private static String escapePowerShellArgument(String argument) {
        return argument.replace("'", "''");
    }

    record LaunchCommand(String executablePath, List<String> arguments) {
        List<String> toCommandLine() {
            if (arguments.isEmpty()) {
                return List.of(executablePath);
            }

            java.util.ArrayList<String> command = new java.util.ArrayList<>();
            command.add(executablePath);
            command.addAll(arguments);
            return command;
        }
    }
}
