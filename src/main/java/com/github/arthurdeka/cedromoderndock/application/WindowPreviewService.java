package com.github.arthurdeka.cedromoderndock.application;

import com.github.arthurdeka.cedromoderndock.domain.WindowQueryGateway;
import com.github.arthurdeka.cedromoderndock.model.DockProgramItemModel;
import com.github.arthurdeka.cedromoderndock.util.NativeWindowUtils;

import java.util.List;

public class WindowPreviewService {
    private final WindowQueryGateway windowQueryGateway;

    public WindowPreviewService(WindowQueryGateway windowQueryGateway) {
        this.windowQueryGateway = windowQueryGateway;
    }

    public List<NativeWindowUtils.WindowInfo> loadPreview(DockProgramItemModel item) {
        return windowQueryGateway.findOpenWindows(item.getExecutablePath());
    }

    public void activate(NativeWindowUtils.WindowInfo windowInfo) {
        windowQueryGateway.activate(windowInfo);
    }
}
