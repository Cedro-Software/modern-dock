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
    public void cacheProgramIcon(String executablePath) {
        WindowsIconHandler.extractAndCacheIcon(executablePath);
    }
}
