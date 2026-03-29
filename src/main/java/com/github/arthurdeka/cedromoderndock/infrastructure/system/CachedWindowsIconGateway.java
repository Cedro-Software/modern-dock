package com.github.arthurdeka.cedromoderndock.infrastructure.system;

import com.github.arthurdeka.cedromoderndock.domain.IconGateway;
import com.github.arthurdeka.cedromoderndock.util.WindowsIconHandler;

import java.nio.file.Path;

public class CachedWindowsIconGateway implements IconGateway {
    @Override
    public Path resolveProgramIcon(String executablePath) {
        return WindowsIconHandler.getCachedIconPath(executablePath);
    }

    @Override
    public Path resolveFolderIcon(String folderPath) {
        return WindowsIconHandler.getCachedFolderIconPath(folderPath);
    }

    @Override
    public void cacheProgramIcon(String executablePath) {
        WindowsIconHandler.extractAndCacheIcon(executablePath);
    }

    @Override
    public void cacheFolderIcon(String folderPath) {
        WindowsIconHandler.extractAndCacheFolderIcon(folderPath);
    }
}
