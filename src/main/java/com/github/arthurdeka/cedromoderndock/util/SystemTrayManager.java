package com.github.arthurdeka.cedromoderndock.util;

import com.github.arthurdeka.cedromoderndock.App;
import javafx.application.Platform;

import javax.imageio.ImageIO;
import java.awt.AWTException;
import java.awt.EventQueue;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.io.InputStream;

public final class SystemTrayManager {
    private final Runnable openSettingsAction;
    private final Runnable exitAction;
    private TrayIcon trayIcon;

    public SystemTrayManager(Runnable openSettingsAction, Runnable exitAction) {
        this.openSettingsAction = openSettingsAction;
        this.exitAction = exitAction;
    }

    public void install() {
        if (!SystemTray.isSupported()) {
            Logger.info("System tray is not supported on this system.");
            return;
        }

        EventQueue.invokeLater(() -> {
            if (trayIcon != null) {
                return;
            }

            try {
                TrayIcon createdTrayIcon = createTrayIcon();
                SystemTray.getSystemTray().add(createdTrayIcon);
                trayIcon = createdTrayIcon;
                Logger.info("System tray icon installed.");
            } catch (AWTException | IOException e) {
                Logger.error("Failed to install system tray icon: " + e.getMessage());
            }
        });
    }

    public void dispose() {
        EventQueue.invokeLater(() -> {
            if (trayIcon == null || !SystemTray.isSupported()) {
                return;
            }

            SystemTray.getSystemTray().remove(trayIcon);
            trayIcon = null;
            Logger.info("System tray icon removed.");
        });
    }

    private TrayIcon createTrayIcon() throws IOException {
        PopupMenu popupMenu = new PopupMenu();

        MenuItem openSettingsMenuItem = new MenuItem("Open Settings");
        openSettingsMenuItem.addActionListener(event -> Platform.runLater(openSettingsAction));
        popupMenu.add(openSettingsMenuItem);

        MenuItem exitMenuItem = new MenuItem("Exit");
        exitMenuItem.addActionListener(event -> Platform.runLater(exitAction));
        popupMenu.add(exitMenuItem);

        TrayIcon createdTrayIcon = new TrayIcon(loadTrayImage(), "Cedro Modern Dock", popupMenu);
        createdTrayIcon.setImageAutoSize(true);
        createdTrayIcon.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                if (event.getButton() == MouseEvent.BUTTON1 && event.getClickCount() == 1) {
                    Platform.runLater(openSettingsAction);
                }
            }
        });
        return createdTrayIcon;
    }

    private Image loadTrayImage() throws IOException {
        try (InputStream inputStream = App.class.getResourceAsStream(
                "/com/github/arthurdeka/cedromoderndock/icons/cedro/logo_32.png"
        )) {
            if (inputStream == null) {
                throw new IOException("Tray icon resource not found.");
            }
            return ImageIO.read(inputStream);
        }
    }
}
