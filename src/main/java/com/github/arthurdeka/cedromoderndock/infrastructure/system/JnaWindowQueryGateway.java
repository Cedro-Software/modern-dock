package com.github.arthurdeka.cedromoderndock.infrastructure.system;

import com.github.arthurdeka.cedromoderndock.domain.WindowQueryGateway;
import com.github.arthurdeka.cedromoderndock.util.NativeWindowUtils;

import java.util.List;

public class JnaWindowQueryGateway implements WindowQueryGateway {
    @Override
    public List<NativeWindowUtils.WindowInfo> findOpenWindows(String executablePath) {
        return NativeWindowUtils.getOpenWindows(executablePath);
    }

    @Override
    public void activate(NativeWindowUtils.WindowInfo windowInfo) {
        NativeWindowUtils.activateWindow(windowInfo.hwnd());
    }
}
