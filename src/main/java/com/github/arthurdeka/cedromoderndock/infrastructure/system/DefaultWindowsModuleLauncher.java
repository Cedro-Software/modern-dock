package com.github.arthurdeka.cedromoderndock.infrastructure.system;

import com.github.arthurdeka.cedromoderndock.domain.WindowsModuleLauncher;
import com.github.arthurdeka.cedromoderndock.util.Logger;

import java.io.IOException;
import java.util.Objects;

public class DefaultWindowsModuleLauncher implements WindowsModuleLauncher {
    @Override
    public void launch(String module, String label) {
        try {
            if (Objects.equals(module, "mypc")) {
                new ProcessBuilder("explorer.exe", "::{20D04FE0-3AEA-1069-A2D8-08002B30309D}").start();
            } else if (Objects.equals(module, "trash")) {
                new ProcessBuilder("explorer.exe", "::{645FF040-5081-101B-9F08-00AA002F954E}").start();
            } else if (Objects.equals(module, "ctrlpnl")) {
                new ProcessBuilder("control.exe").start();
            } else if (Objects.equals(module, "pconfig")) {
                new ProcessBuilder("cmd", "/c", "start", "ms-settings:").start();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        Logger.info(label + " Clicked");
    }
}
