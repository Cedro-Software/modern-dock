package com.github.arthurdeka.cedromoderndock.application;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

public final class ProgramSelectionResolver {

    private ProgramSelectionResolver() {
    }

    public static ResolvedProgramSelection resolve(Path selectedExecutable) {
        Path normalizedPath = selectedExecutable.toAbsolutePath().normalize();
        Path resolvedExecutable = resolveSquirrelExecutable(normalizedPath);
        return new ResolvedProgramSelection(
                resolvedExecutable.toString(),
                stripExtension(resolvedExecutable.getFileName().toString())
        );
    }

    private static Path resolveSquirrelExecutable(Path selectedExecutable) {
        if (!isUpdaterExecutable(selectedExecutable)) {
            return selectedExecutable;
        }

        Path installDir = selectedExecutable.getParent();
        if (installDir == null) {
            return selectedExecutable;
        }

        String installFolderName = installDir.getFileName() == null ? "" : installDir.getFileName().toString();
        List<Path> appDirectories = listAppDirectories(installDir);
        for (Path appDirectory : appDirectories) {
            Path preferredExecutable = appDirectory.resolve(installFolderName + ".exe");
            if (Files.isRegularFile(preferredExecutable)) {
                return preferredExecutable;
            }

            Path fallbackExecutable = findFirstExecutable(appDirectory);
            if (fallbackExecutable != null) {
                return fallbackExecutable;
            }
        }

        return selectedExecutable;
    }

    private static boolean isUpdaterExecutable(Path executablePath) {
        Path fileName = executablePath.getFileName();
        return fileName != null && fileName.toString().equalsIgnoreCase("Update.exe");
    }

    private static List<Path> listAppDirectories(Path installDir) {
        try (Stream<Path> paths = Files.list(installDir)) {
            return paths
                    .filter(Files::isDirectory)
                    .filter(path -> path.getFileName() != null)
                    .filter(path -> path.getFileName().toString().toLowerCase(Locale.ROOT).startsWith("app-"))
                    .sorted(Comparator.comparing(ProgramSelectionResolver::lastModifiedTime).reversed())
                    .toList();
        } catch (IOException e) {
            return List.of();
        }
    }

    private static long lastModifiedTime(Path path) {
        try {
            return Files.getLastModifiedTime(path).toMillis();
        } catch (IOException e) {
            return Long.MIN_VALUE;
        }
    }

    private static Path findFirstExecutable(Path appDirectory) {
        try (Stream<Path> files = Files.list(appDirectory)) {
            return files
                    .filter(Files::isRegularFile)
                    .filter(path -> path.getFileName() != null)
                    .filter(path -> path.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(".exe"))
                    .filter(path -> !path.getFileName().toString().equalsIgnoreCase("Update.exe"))
                    .sorted(Comparator.comparing(path -> path.getFileName().toString()))
                    .findFirst()
                    .orElse(null);
        } catch (IOException e) {
            return null;
        }
    }

    private static String stripExtension(String fileName) {
        int lastDot = fileName.lastIndexOf('.');
        return lastDot > 0 ? fileName.substring(0, lastDot) : fileName;
    }

    public record ResolvedProgramSelection(String executablePath, String label) {
    }
}
