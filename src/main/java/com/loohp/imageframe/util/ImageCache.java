package com.loohp.imageframe.util;

import com.mojang.blaze3d.platform.NativeImage;
import com.loohp.imageframe.ImageFrameClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.stream.Stream;

public final class ImageCache {

    private static final Logger LOGGER = LoggerFactory.getLogger("imageframe-cache");
    private static final String CACHE_DIR_NAME = "imageframeclient";
    private static final String TEXTURES_SUBDIR = "cache";
    private static final Duration MAX_AGE = Duration.ofDays(7);
    private static final long MAX_SIZE_BYTES = 100 * 1024 * 1024;

    private static volatile long lastCleanup = 0;

    public static Path getCacheDir() {
        Path gameDir = ImageFrameClient.getGameDirectory();
        return gameDir.resolve(CACHE_DIR_NAME).resolve(TEXTURES_SUBDIR);
    }

    public static NativeImage loadCachedTexture(int mapId) {
        cleanupIfNeeded();
        Path file = getCacheDir().resolve("map_" + mapId + ".png");
        if (!Files.isRegularFile(file)) return null;
        if (isExpired(file)) {
            try { Files.deleteIfExists(file); } catch (IOException ignored) {}
            return null;
        }
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

    public static boolean hasCachedTexture(int mapId) {
        Path file = getCacheDir().resolve("map_" + mapId + ".png");
        return Files.isRegularFile(file) && !isExpired(file);
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

    private static boolean isExpired(Path file) {
        try {
            BasicFileAttributes attrs = Files.readAttributes(file, BasicFileAttributes.class);
            return attrs.lastModifiedTime().toInstant().plus(MAX_AGE).isBefore(Instant.now());
        } catch (IOException e) {
            return false;
        }
    }

    private static synchronized void cleanupIfNeeded() {
        long now = System.currentTimeMillis();
        if (now - lastCleanup < 300_000) return;
        lastCleanup = now;
        Path dir = getCacheDir();
        if (!Files.isDirectory(dir)) return;
        try {
            long totalSize;
            try (Stream<Path> list = Files.list(dir)) {
                totalSize = list.filter(p -> p.getFileName().toString().endsWith(".png"))
                        .mapToLong(p -> {
                            try { return Files.size(p); } catch (IOException e) { return 0; }
                        }).sum();
            }
            if (totalSize <= MAX_SIZE_BYTES) return;

            final long[] remaining = {totalSize};
            try (Stream<Path> list = Files.list(dir)) {
                list.filter(p -> p.getFileName().toString().endsWith(".png"))
                    .sorted(Comparator.comparingLong(p -> {
                        try { return Files.getLastModifiedTime(p).toMillis(); } catch (IOException e) { return 0; }
                    }))
                    .takeWhile(p -> remaining[0] > MAX_SIZE_BYTES)
                    .forEachOrdered(p -> {
                        try {
                            long sz = Files.size(p);
                            Files.deleteIfExists(p);
                            remaining[0] -= sz;
                        } catch (IOException ignored) {}
                    });
            }
            LOGGER.info("Cache cleaned to {} MB", remaining[0] / (1024.0 * 1024.0));
        } catch (IOException e) {
            LOGGER.warn("Cache cleanup failed", e);
        }
    }

    private ImageCache() {}
}
