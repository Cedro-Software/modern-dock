package com.github.arthurdeka.cedromoderndock.application;

import com.github.arthurdeka.cedromoderndock.model.DockFolderItemModel;
import com.github.arthurdeka.cedromoderndock.model.DockProgramItemModel;
import com.github.arthurdeka.cedromoderndock.model.DockSettingsItemModel;
import com.github.arthurdeka.cedromoderndock.model.DockWindowsModuleItemModel;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DockItemActionServiceTest {

    @Test
    void executesProgramItemsThroughProgramLauncher() {
        InvocationCapture capture = new InvocationCapture();
        DockItemActionService service = new DockItemActionService(
                (path, label) -> capture.record("program", path, label),
                (path, label) -> capture.record("folder", path, label),
                (module, label) -> capture.record("module", module, label)
        );

        service.execute(new DockProgramItemModel("Editor", "C:\\tools\\editor.exe"), capture::openSettings);

        assertEquals("program", capture.kind);
        assertEquals("C:\\tools\\editor.exe", capture.value);
        assertEquals("Editor", capture.label);
    }

    @Test
    void executesFolderItemsThroughFolderLauncher() {
        InvocationCapture capture = new InvocationCapture();
        DockItemActionService service = new DockItemActionService(
                (path, label) -> capture.record("program", path, label),
                (path, label) -> capture.record("folder", path, label),
                (module, label) -> capture.record("module", module, label)
        );

        service.execute(new DockFolderItemModel("Projects", "C:\\Users\\Arthur Rodrigues\\Projects"), capture::openSettings);

        assertEquals("folder", capture.kind);
        assertEquals("C:\\Users\\Arthur Rodrigues\\Projects", capture.value);
        assertEquals("Projects", capture.label);
    }

    @Test
    void executesWindowsModuleItemsThroughWindowsModuleLauncher() {
        InvocationCapture capture = new InvocationCapture();
        DockItemActionService service = new DockItemActionService(
                (path, label) -> capture.record("program", path, label),
                (path, label) -> capture.record("folder", path, label),
                (module, label) -> capture.record("module", module, label)
        );

        service.execute(new DockWindowsModuleItemModel("Control Panel", "ctrlpnl"), capture::openSettings);

        assertEquals("module", capture.kind);
        assertEquals("ctrlpnl", capture.value);
        assertEquals("Control Panel", capture.label);
    }

    @Test
    void executesSettingsItemsThroughSettingsAction() {
        InvocationCapture capture = new InvocationCapture();
        DockItemActionService service = new DockItemActionService(
                (path, label) -> capture.record("program", path, label),
                (path, label) -> capture.record("folder", path, label),
                (module, label) -> capture.record("module", module, label)
        );

        service.execute(new DockSettingsItemModel(), capture::openSettings);

        assertEquals("settings", capture.kind);
    }

    private static final class InvocationCapture {
        private String kind = "";
        private String value = "";
        private String label = "";

        private void record(String kind, String value, String label) {
            this.kind = kind;
            this.value = value;
            this.label = label;
        }

        private void openSettings() {
            this.kind = "settings";
        }
    }
}
