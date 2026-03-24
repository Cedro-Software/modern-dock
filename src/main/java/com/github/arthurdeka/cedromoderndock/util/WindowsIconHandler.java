package com.github.arthurdeka.cedromoderndock.util;

import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.Structure;
import com.sun.jna.platform.win32.GDI32;
import com.sun.jna.platform.win32.Guid;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef.HBITMAP;
import com.sun.jna.platform.win32.WinDef.HDC;
import com.sun.jna.platform.win32.WinDef.HICON;
import com.sun.jna.platform.win32.WinGDI;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.win32.StdCallLibrary;
import com.sun.jna.win32.W32APIOptions;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

/**
 * Extracts the icon from executables (.exe) and saves it to a persistent cache folder in AppData.
 * The extraction uses Windows Shell API (JNA) to request a high-resolution icon.
 */
public final class WindowsIconHandler {

    private WindowsIconHandler() {
    } // static utility

    // Persistent cache directory in %APPDATA%/CedroModernDock/iconsCache
    private static final Path CACHE_DIR = getCacheDirectory();
    // Target extraction size to capture the highest-quality icon available.
    private static final int ICON_SIZE = 256;
    private static final int SHGFI_ICON = 0x000000100;
    private static final int SHGFI_SYSICONINDEX = 0x000004000;
    private static final int FILE_ATTRIBUTE_DIRECTORY = 0x00000010;
    private static final int SHIL_JUMBO = 0x4;
    private static final int ILD_TRANSPARENT = 0x00000001;

    private interface User32Ex extends StdCallLibrary {
        User32Ex INSTANCE = Native.load("user32", User32Ex.class, W32APIOptions.DEFAULT_OPTIONS);

        int PrivateExtractIconsW(String szFileName, int nIconIndex, int cxIcon, int cyIcon,
                                 HICON[] phicon, int[] piconid, int nIcons, int flags);
    }

    private interface Shell32Ex extends StdCallLibrary {
        Shell32Ex INSTANCE = Native.load("shell32", Shell32Ex.class, W32APIOptions.DEFAULT_OPTIONS);

        long SHGetFileInfoW(String pszPath, int dwFileAttributes, SHFILEINFO psfi, int cbFileInfo, int uFlags);
        int SHGetImageList(int iImageList, Guid.IID riid, com.sun.jna.ptr.PointerByReference ppv);
    }

    private interface Comctl32Ex extends StdCallLibrary {
        Comctl32Ex INSTANCE = Native.load("comctl32", Comctl32Ex.class, W32APIOptions.DEFAULT_OPTIONS);

        HICON ImageList_GetIcon(com.sun.jna.Pointer himl, int i, int flags);
    }

    @Structure.FieldOrder({"hIcon", "iIcon", "dwAttributes", "szDisplayName", "szTypeName"})
    public static class SHFILEINFO extends Structure {
        public HICON hIcon;
        public int iIcon;
        public int dwAttributes;
        public char[] szDisplayName = new char[260];
        public char[] szTypeName = new char[80];
    }

    /**
     * Get the cached icon path
     *
     * @param exePath complete path to the .exe. (take it from the value in the config.json file)
     * @return The path to the icon in the cache folder
     */
    public static Path getCachedIconPath(String exePath) {
        return getCachedPath(exePath, "program");
    }

    public static Path getCachedFolderIconPath(String folderPath) {
        return getCachedPath(folderPath, "folder_v3");
    }

    private static Path getCachedPath(String inputPath, String kind) {
        try {
            String fileName = kind + "_" + getHashedFileName(inputPath) + ".png";
            return CACHE_DIR.resolve(fileName);
        } catch (NoSuchAlgorithmException e) {
            Logger.error("getCachedIconPath - Failed to generate hashed file name for " + inputPath + e);
            return null;
        }
    }

    /**
     * Extracts the icon from executables
     *
     * @param exePath complete path to the .exe.
     * @return The path to the icon in the cache folder
     */
    public static Path extractAndCacheIcon(String exePath) {
        return extractAndCacheProgramIcon(exePath);
    }

    public static Path extractAndCacheFolderIcon(String folderPath) {
        try {
            Path cachedIconPath = getCachedFolderIconPath(folderPath);
            if (cachedIconPath == null) {
                return null;
            }

            if (Files.exists(cachedIconPath)) {
                Logger.info("Icon for " + folderPath + " found in cache.");
                return cachedIconPath;
            }

            File folder = new File(folderPath);
            if (!folder.isDirectory()) {
                Logger.error("Folder icon extraction failed because folder is invalid: " + folderPath);
                return null;
            }

            BufferedImage image = extractFolderIconWithShellApi(folder);
            if (image == null) {
                return null;
            }

            ImageIO.write(image, "png", cachedIconPath.toFile());
            return Files.exists(cachedIconPath) && Files.size(cachedIconPath) > 0 ? cachedIconPath : null;
        } catch (IOException e) {
            Logger.error("WindowsIconExtractor error on extractAndCacheFolderIcon: " + e.getMessage() + e);
            return null;
        }
    }

    private static Path extractAndCacheProgramIcon(String exePath) {
        try {
            Path cachedIconPath = getCachedIconPath(exePath);
            if (cachedIconPath == null) {
                return null;
            }

            // If the icon already exists, return its path immediately.
            if (Files.exists(cachedIconPath)) {
                Logger.info("Icon for " + exePath + " found in cache.");
                return cachedIconPath;
            }

            if (!extractIconWithShellApi(exePath, cachedIconPath)) {
                Logger.error("Failed to extract icon for " + exePath + " using Shell API.");
                Files.deleteIfExists(cachedIconPath); // Clean up empty or corrupted file
                return null;
            }

            Logger.info("[extractAndCacheIcon] Icon for " + exePath + " successfully extracted and cached at iconsCache");
            return cachedIconPath; // Return the path of the newly saved icon.

        } catch (IOException e) {
            Logger.error("WindowsIconExtractor error on extractAndCacheIcon: " + e.getMessage() + e);
            return null;
        }
    }

    private static BufferedImage extractFolderIconWithShellApi(File folder) {
        SHFILEINFO shFileInfo = new SHFILEINFO();
        long result = Shell32Ex.INSTANCE.SHGetFileInfoW(
                folder.getAbsolutePath(),
                FILE_ATTRIBUTE_DIRECTORY,
                shFileInfo,
                shFileInfo.size(),
                SHGFI_SYSICONINDEX
        );

        if (result == 0) {
            return null;
        }

        com.sun.jna.ptr.PointerByReference imageListRef = new com.sun.jna.ptr.PointerByReference();
        Guid.IID iImageListId = new Guid.IID("{46EB5926-582E-4017-9FDF-E8998DAA0950}");
        int hr = Shell32Ex.INSTANCE.SHGetImageList(SHIL_JUMBO, iImageListId, imageListRef);
        if (hr != 0 || imageListRef.getValue() == null) {
            return null;
        }

        HICON hIcon = Comctl32Ex.INSTANCE.ImageList_GetIcon(imageListRef.getValue(), shFileInfo.iIcon, ILD_TRANSPARENT);
        if (hIcon == null) {
            return null;
        }

        try {
            return iconToBufferedImage(hIcon);
        } finally {
            User32.INSTANCE.DestroyIcon(hIcon);
        }
    }

    /**
     * Extracts the icon using the Windows Shell API and saves it as a PNG.
     */
    private static boolean extractIconWithShellApi(String exePath, Path outputPath) throws IOException {
        HICON[] icons = new HICON[1];
        int[] iconIds = new int[1];
        int extracted = User32Ex.INSTANCE.PrivateExtractIconsW(
                exePath,
                0,
                ICON_SIZE,
                ICON_SIZE,
                icons,
                iconIds,
                1,
                0
        );

        if (extracted <= 0 || icons[0] == null) {
            return false;
        }

        BufferedImage image;
        try {
            image = iconToBufferedImage(icons[0]);
        } finally {
            User32.INSTANCE.DestroyIcon(icons[0]);
        }

        if (image == null) {
            return false;
        }

        ImageIO.write(image, "png", outputPath.toFile());
        return Files.exists(outputPath) && Files.size(outputPath) > 0;
    }

    /**
     * Converts a Windows HICON into a BufferedImage.
     */
    private static BufferedImage iconToBufferedImage(HICON hIcon) {
        WinGDI.ICONINFO iconInfo = new WinGDI.ICONINFO();
        if (!User32.INSTANCE.GetIconInfo(hIcon, iconInfo)) {
            return null;
        }

        try {
            HBITMAP hBitmap = iconInfo.hbmColor != null ? iconInfo.hbmColor : iconInfo.hbmMask;
            if (hBitmap == null) {
                return null;
            }

            WinGDI.BITMAP bitmap = new WinGDI.BITMAP();
            GDI32.INSTANCE.GetObject(hBitmap, bitmap.size(), bitmap.getPointer());
            bitmap.read();

            int width = bitmap.bmWidth.intValue();
            int height = bitmap.bmHeight.intValue();
            if (width <= 0 || height <= 0) {
                return null;
            }

            WinGDI.BITMAPINFO bmi = new WinGDI.BITMAPINFO();
            bmi.bmiHeader.biSize = bmi.bmiHeader.size();
            bmi.bmiHeader.biWidth = width;
            // Negative height creates a top-down DIB (no vertical flip).
            bmi.bmiHeader.biHeight = -height;
            bmi.bmiHeader.biPlanes = 1;
            bmi.bmiHeader.biBitCount = 32;
            bmi.bmiHeader.biCompression = WinGDI.BI_RGB;

            int bufferSize = width * height * 4;
            Memory buffer = new Memory(bufferSize);
            HDC hdc = GDI32.INSTANCE.CreateCompatibleDC(null);
            if (hdc == null) {
                return null;
            }
            WinNT.HANDLE oldBitmap = GDI32.INSTANCE.SelectObject(hdc, hBitmap);

            int lines = GDI32.INSTANCE.GetDIBits(
                    hdc,
                    hBitmap,
                    0,
                    height,
                    buffer,
                    bmi,
                    WinGDI.DIB_RGB_COLORS
            );

            GDI32.INSTANCE.SelectObject(hdc, oldBitmap);
            GDI32.INSTANCE.DeleteDC(hdc);

            if (lines == 0) {
                return null;
            }

            byte[] data = buffer.getByteArray(0, bufferSize);
            int[] pixels = new int[width * height];
            for (int i = 0, p = 0; i < pixels.length; i++) {
                int b = data[p++] & 0xFF;
                int g = data[p++] & 0xFF;
                int r = data[p++] & 0xFF;
                int a = data[p++] & 0xFF;
                pixels[i] = (a << 24) | (r << 16) | (g << 8) | b;
            }

            BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            image.setRGB(0, 0, width, height, pixels, 0, width);
            return image;
        } finally {
            cleanupIconInfo(iconInfo);
        }
    }

    private static void cleanupIconInfo(WinGDI.ICONINFO iconInfo) {
        if (iconInfo.hbmColor != null) {
            GDI32.INSTANCE.DeleteObject(iconInfo.hbmColor);
        }
        if (iconInfo.hbmMask != null) {
            GDI32.INSTANCE.DeleteObject(iconInfo.hbmMask);
        }
    }

    /**
     * Returns the icons cache directory (creating AppData/CedroModernDock/iconsCache if it does not exist)
     */
    private static Path getCacheDirectory() {
        try {
            String appData = System.getenv("APPDATA");
            if (appData == null || appData.isEmpty()) {
                Logger.error("AppData Not found.");
            }
            Path cacheDir = Paths.get(appData, "CedroModernDock", "iconsCache");
            Files.createDirectories(cacheDir); // Creates the folder structure if it doesn't exist
            return cacheDir;
        } catch (IOException e) {
            Logger.error("Failed to create icon cache directory." + e);
            throw new RuntimeException("Could not create cache directory.", e);
        }
    }

    /**
     * Generates a safe filename using a SHA-256 hash of the executable path.
     */
    private static String getHashedFileName(String input) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
        return new BigInteger(1, hash).toString(16);
    }

}
