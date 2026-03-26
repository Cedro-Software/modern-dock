package com.github.arthurdeka.cedromoderndock.util;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public final class SingleInstanceGuard implements AutoCloseable {
    private static final String LOCK_FILE_NAME = "app.lock";

    private final Path lockFilePath;
    private FileChannel channel;
    private FileLock lock;

    public SingleInstanceGuard() {
        this(getDefaultLockPath());
    }

    public SingleInstanceGuard(Path lockFilePath) {
        this.lockFilePath = lockFilePath;
    }

    public boolean tryAcquire() {
        try {
            Path parent = lockFilePath.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }

            channel = FileChannel.open(
                    lockFilePath,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.WRITE
            );
            lock = channel.tryLock();
            return lock != null;
        } catch (IOException e) {
            Logger.error("Failed to acquire single-instance lock: " + e.getMessage());
            closeQuietly();
            return false;
        } catch (OverlappingFileLockException e) {
            closeQuietly();
            return false;
        }
    }

    @Override
    public void close() {
        closeQuietly();
    }

    private void closeQuietly() {
        try {
            if (lock != null && lock.isValid()) {
                lock.release();
            }
        } catch (IOException e) {
            Logger.error("Failed to release single-instance lock: " + e.getMessage());
        } finally {
            lock = null;
        }

        try {
            if (channel != null && channel.isOpen()) {
                channel.close();
            }
        } catch (IOException e) {
            Logger.error("Failed to close single-instance lock channel: " + e.getMessage());
        } finally {
            channel = null;
        }
    }

    private static Path getDefaultLockPath() {
        String appDataPath = System.getenv("APPDATA");
        if (appDataPath == null || appDataPath.isEmpty()) {
            appDataPath = System.getProperty("user.home");
        }

        return Paths.get(appDataPath, "CedroModernDock", LOCK_FILE_NAME);
    }
}
