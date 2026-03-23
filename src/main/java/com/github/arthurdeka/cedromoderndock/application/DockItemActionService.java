package com.github.arthurdeka.cedromoderndock.application;

import com.github.arthurdeka.cedromoderndock.domain.ProgramLauncher;
import com.github.arthurdeka.cedromoderndock.domain.WindowsModuleLauncher;
import com.github.arthurdeka.cedromoderndock.model.DockItem;
import com.github.arthurdeka.cedromoderndock.model.DockItemType;
import com.github.arthurdeka.cedromoderndock.model.DockProgramItemModel;
import com.github.arthurdeka.cedromoderndock.model.DockWindowsModuleItemModel;

public class DockItemActionService {
    private final ProgramLauncher programLauncher;
    private final WindowsModuleLauncher windowsModuleLauncher;

    public DockItemActionService(ProgramLauncher programLauncher, WindowsModuleLauncher windowsModuleLauncher) {
        this.programLauncher = programLauncher;
        this.windowsModuleLauncher = windowsModuleLauncher;
    }

    public void execute(DockItem item, Runnable openSettingsAction) {
        if (item.getType() == DockItemType.PROGRAM && item instanceof DockProgramItemModel programItem) {
            programLauncher.launch(programItem.getExecutablePath(), programItem.getLabel());
            return;
        }

        if (item.getType() == DockItemType.WINDOWS_MODULE && item instanceof DockWindowsModuleItemModel windowsModuleItem) {
            windowsModuleLauncher.launch(windowsModuleItem.getModule(), windowsModuleItem.getLabel());
            return;
        }

        if (item.getType() == DockItemType.SETTINGS) {
            openSettingsAction.run();
        }
    }
}
