package com.loohp.imageframe.util;

import com.mojang.blaze3d.platform.NativeImage;
import com.loohp.imageframe.ImageFrameClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class ImageCache {

    private static final Logger LOGGER = LoggerFactory.getLogger("imageframe-cache");
    private static final String CACHE_DIR_NAME = "imageframeclient";
    private static final String TEXTURES_SUBDIR = "cache";

    public static Path getCacheDir() {
        Path gameDir = ImageFrameClient.getGameDirectory();
        return gameDir.resolve(CACHE_DIR_NAME).resolve(TEXTURES_SUBDIR);
    }

    public static NativeImage loadCachedTexture(int mapId) {
        Path file = getCacheDir().resolve("map_" + mapId + ".png");
        if (!Files.isRegularFile(file)) return null;
        try {
            byte[] data = Files.readAllBytes(file);
            return ImageUtil.pngToNativeImage(data);
        } catch (IOException e) {
            LOGGER.warn("Failed to load cached texture for map {}", mapId, e);
            return null;
        }
    }

    public static void saveTexture(int mapId, NativeImage image) {
        try {
            Files.createDirectories(getCacheDir());
            byte[] png = ImageUtil.nativeImageToPng(image);
            Files.write(getCacheDir().resolve("map_" + mapId + ".png"), png);
        } catch (IOException e) {
            LOGGER.warn("Failed to cache texture for map {}", mapId, e);
        }
    }

    public static void removeTexture(int mapId) {
        Path file = getCacheDir().resolve("map_" + mapId + ".png");
        try {
            Files.deleteIfExists(file);
        } catch (IOException e) {
            LOGGER.warn("Failed to remove cached texture for map {}", mapId, e);
        }
    }

    public static void clearCache() {
        Path dir = getCacheDir();
        if (!Files.isDirectory(dir)) return;
        try (var files = Files.list(dir)) {
            files.filter(p -> p.getFileName().toString().endsWith(".png"))
                 .forEach(p -> {
                     try { Files.deleteIfExists(p); }
                     catch (IOException e) { LOGGER.warn("Failed to delete cache file {}", p, e); }
                 });
            LOGGER.info("ImageFrame cache cleared");
        } catch (IOException e) {
            LOGGER.warn("Failed to clear cache", e);
        }
    }

    private ImageCache() {}
}
