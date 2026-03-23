package com.github.arthurdeka.cedromoderndock.application;

import com.github.arthurdeka.cedromoderndock.domain.IconGateway;

public record AppServices(
        DockService dockService,
        DockAppearanceService appearanceService,
        DockPositioningService positioningService,
        DockItemActionService itemActionService,
        WindowPreviewService windowPreviewService,
        IconGateway iconGateway
) {
}
