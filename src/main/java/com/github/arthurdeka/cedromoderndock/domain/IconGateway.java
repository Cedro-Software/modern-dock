package com.github.arthurdeka.cedromoderndock.domain;

import java.nio.file.Path;

public interface IconGateway {
    Path resolveProgramIcon(String executablePath);

    void cacheProgramIcon(String executablePath);
}
