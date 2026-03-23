package com.github.arthurdeka.cedromoderndock.domain;

import com.github.arthurdeka.cedromoderndock.util.NativeWindowUtils;

import java.util.List;

public interface WindowQueryGateway {
    List<NativeWindowUtils.WindowInfo> findOpenWindows(String executablePath);

    void activate(NativeWindowUtils.WindowInfo windowInfo);
}
