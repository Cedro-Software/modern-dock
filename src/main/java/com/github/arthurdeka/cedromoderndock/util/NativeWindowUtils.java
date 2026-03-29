package com.github.arthurdeka.cedromoderndock.util;

import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.ptr.IntByReference;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class NativeWindowUtils {

    // Minimal info required by the popup to activate and label a window.
    public record WindowInfo(HWND hwnd, String title) {}

    public static List<WindowInfo> getOpenWindows(String executablePath) {
        List<WindowInfo> windows = new ArrayList<>();
        if (executablePath == null || executablePath.isEmpty()) {
            return windows;
        }

        // Normalize the target executable path so comparisons are stable.
        final Path targetPath = Paths.get(executablePath).toAbsolutePath().normalize();

        User32.INSTANCE.EnumWindows((hWnd, arg1) -> {
            if (User32.INSTANCE.IsWindowVisible(hWnd)) {
                char[] buffer = new char[1024];
                User32.INSTANCE.GetWindowText(hWnd, buffer, 1024);
                String title = new String(buffer).trim();

                // Skip windows without title or hidden ones (some invisible windows report visible but have empty title/rect).
                if (title.isEmpty()) {
                    return true;
                }

                // Keep only windows belonging to the executable path requested.
                if (isWindowFromExecutable(hWnd, targetPath)) {
                    windows.add(new WindowInfo(hWnd, title));
                }
            }
            return true;
        }, null);

        return windows;
    }

    private static boolean isWindowFromExecutable(HWND hWnd, Path targetPath) {
        IntByReference pidRef = new IntByReference();
        User32.INSTANCE.GetWindowThreadProcessId(hWnd, pidRef);
        int pid = pidRef.getValue();

        // Query the process image path to match it against the target executable.
        WinNT.HANDLE process = Kernel32.INSTANCE.OpenProcess(
                WinNT.PROCESS_QUERY_LIMITED_INFORMATION,
                false,
                pid
        );

        if (process != null) {
            try {
                char[] pathBuffer = new char[1024];
                IntByReference size = new IntByReference(pathBuffer.length);
                if (Kernel32.INSTANCE.QueryFullProcessImageName(process, 0, pathBuffer, size)) {
                    String processPathStr = new String(pathBuffer, 0, size.getValue());
                    Path processPath = Paths.get(processPathStr).toAbsolutePath().normalize();
                    // Prefer full path match, but fall back to filename match for edge cases.
                    if (isSameExecutable(processPath, targetPath)) {
                        return true;
                    }
                }
            } finally {
                Kernel32.INSTANCE.CloseHandle(process);
            }
        }
        return false;
    }

    private static boolean isSameExecutable(Path processPath, Path targetPath) {
        if (processPath == null || targetPath == null) {
            return false;
        }

        // Exact path match.
        if (processPath.equals(targetPath)) {
            return true;
        }

        // Case-insensitive path match (Windows path comparisons).
        String processStr = normalizePathString(processPath);
        String targetStr = normalizePathString(targetPath);
        if (processStr.equalsIgnoreCase(targetStr)) {
            return true;
        }

        // Fallback: match only the filename when the full path is not comparable.
        Path processFile = processPath.getFileName();
        Path targetFile = targetPath.getFileName();
        if (processFile != null && targetFile != null) {
            return processFile.toString().equalsIgnoreCase(targetFile.toString());
        }

        return false;
    }

    private static String normalizePathString(Path path) {
        String value = path.toString();
        // Strip Windows extended-length path prefix if present.
        if (value.startsWith("\\\\?\\")) {
            value = value.substring(4);
        }
        return value;
    }

    public static void activateWindow(HWND hwnd) {
        if (hwnd == null) return;

        // Restore window if minimized
        User32.INSTANCE.ShowWindow(hwnd, User32.SW_RESTORE);

        // Bring to front
        User32.INSTANCE.SetForegroundWindow(hwnd);
    }
}
