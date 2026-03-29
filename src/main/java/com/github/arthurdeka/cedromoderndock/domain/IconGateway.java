package com.github.arthurdeka.cedromoderndock.domain;

import java.nio.file.Path;

public interface IconGateway {
    Path resolveProgramIcon(String executablePath);
    Path resolveFolderIcon(String folderPath);

    void cacheProgramIcon(String executablePath);
    void cacheFolderIcon(String folderPath);
}
