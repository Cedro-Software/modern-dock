package com.github.arthurdeka.cedromoderndock.infrastructure.system;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DefaultProgramLauncherTest {

    @TempDir
    Path tempDir;

    @Test
    void launchesRegularProgramsDirectly() throws IOException {
        Path executable = Files.createFile(tempDir.resolve("Editor.exe"));

        DefaultProgramLauncher.LaunchCommand command =
                DefaultProgramLauncher.resolveLaunchCommand(executable.toString());

        assertEquals(executable.toString(), command.executablePath());
        assertEquals(List.of(), command.arguments());
    }

    @Test
    void launchesDiscordThroughUpdateExecutable() throws IOException {
        Path installDir = Files.createDirectories(tempDir.resolve("Discord"));
        Path updateExecutable = Files.createFile(installDir.resolve("Update.exe"));
        Path versionDirectory = Files.createDirectories(installDir.resolve("app-1.0.9230"));
        Path discordExecutable = Files.createFile(versionDirectory.resolve("Discord.exe"));

        DefaultProgramLauncher.LaunchCommand command =
                DefaultProgramLauncher.resolveLaunchCommand(discordExecutable.toString());

        assertEquals(updateExecutable.toString(), command.executablePath());
        assertEquals(List.of("--processStart", "Discord.exe"), command.arguments());
    }
}
