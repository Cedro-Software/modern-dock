package com.github.arthurdeka.cedromoderndock.application;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ProgramSelectionResolverTest {

    @TempDir
    Path tempDir;

    @Test
    void keepsRegularExecutablesUntouched() throws IOException {
        Path executable = Files.createFile(tempDir.resolve("Editor.exe"));

        ProgramSelectionResolver.ResolvedProgramSelection selection = ProgramSelectionResolver.resolve(executable);

        assertEquals(executable.toString(), selection.executablePath());
        assertEquals("Editor", selection.label());
    }

    @Test
    void resolvesSquirrelUpdaterToRealApplicationExecutable() throws IOException {
        Path installDir = Files.createDirectories(tempDir.resolve("Discord"));
        Files.createFile(installDir.resolve("Update.exe"));
        Path appDirectory = Files.createDirectories(installDir.resolve("app-1.0.0"));
        Path discordExecutable = Files.createFile(appDirectory.resolve("Discord.exe"));

        ProgramSelectionResolver.ResolvedProgramSelection selection =
                ProgramSelectionResolver.resolve(installDir.resolve("Update.exe"));

        assertEquals(discordExecutable.toString(), selection.executablePath());
        assertEquals("Discord", selection.label());
    }

    @Test
    void fallsBackToFirstExecutableWhenFolderNameDoesNotMatch() throws IOException {
        Path installDir = Files.createDirectories(tempDir.resolve("SomeWrapper"));
        Files.createFile(installDir.resolve("Update.exe"));
        Path appDirectory = Files.createDirectories(installDir.resolve("app-2.0.0"));
        Path realExecutable = Files.createFile(appDirectory.resolve("RealApp.exe"));

        ProgramSelectionResolver.ResolvedProgramSelection selection =
                ProgramSelectionResolver.resolve(installDir.resolve("Update.exe"));

        assertEquals(realExecutable.toString(), selection.executablePath());
        assertEquals("RealApp", selection.label());
    }
}
