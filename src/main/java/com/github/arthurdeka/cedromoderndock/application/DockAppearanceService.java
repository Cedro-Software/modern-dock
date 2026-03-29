package com.github.arthurdeka.cedromoderndock.application;

import com.github.arthurdeka.cedromoderndock.model.DockModel;

public class DockAppearanceService {
    private final DockService dockService;

    public DockAppearanceService(DockService dockService) {
        this.dockService = dockService;
    }

    public DockModel getDock() {
        return dockService.getDock();
    }

    public int getIconsSize() {
        return getDock().getIconsSize();
    }

    public void setIconsSize(int iconsSize) {
        getDock().setIconsSize(iconsSize);
        dockService.saveChanges();
    }

    public int getSpacingBetweenIcons() {
        return getDock().getSpacingBetweenIcons();
    }

    public void setSpacingBetweenIcons(int spacingValue) {
        getDock().setSpacingBetweenIcons(spacingValue);
        dockService.saveChanges();
    }

    public int getDockTransparencyPercentage() {
        return (int) (getDock().getDockTransparency() * 100);
    }

    public void setDockTransparencyPercentage(int value) {
        getDock().setDockTransparency((double) value / 100);
        dockService.saveChanges();
    }

    public int getDockBorderRounding() {
        return getDock().getDockBorderRounding();
    }

    public void setDockBorderRounding(int value) {
        getDock().setDockBorderRounding(value);
        dockService.saveChanges();
    }

    public String getDockColorRGB() {
        return getDock().getDockColorRGB();
    }

    public void setDockColorRGB(String value) {
        getDock().setDockColorRGB(value);
        dockService.saveChanges();
    }

    public DockTheme getDockTheme() {
        DockModel dock = getDock();
        return new DockTheme(dock.getDockColorRGB(), dock.getDockTransparency(), dock.getDockBorderRounding());
    }
}
