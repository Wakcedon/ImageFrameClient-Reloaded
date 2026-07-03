package com.loohp.imageframe.configuration;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.loohp.imageframe.ImageFrameClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public final class ServerPerConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger("imageframe-perconfig");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String DIR_NAME = "imageframeclient";
    private static final String FILE_NAME = "server_overrides.json";

    private static Path configFile;
    private static Map<String, Map<String, Object>> overrides = new HashMap<>();
    private static String currentServer;

    private ServerPerConfig() {}

    public static void init() {
        Path gameDir = ImageFrameClient.getGameDirectory();
        configFile = gameDir.resolve(DIR_NAME).resolve(FILE_NAME);
        load();
    }

    private static void load() {
        if (Files.isRegularFile(configFile)) {
            try (Reader r = Files.newBufferedReader(configFile)) {
                Map<String, Map<String, Object>> data = GSON.fromJson(r,
                        new TypeToken<Map<String, Map<String, Object>>>() {}.getType());
                if (data != null) overrides = data;
            } catch (IOException e) {
                LOGGER.warn("Failed to load server overrides", e);
            }
        }
    }

    private static void save() {
        try {
            Files.createDirectories(configFile.getParent());
            try (Writer w = Files.newBufferedWriter(configFile)) {
                GSON.toJson(overrides, w);
            }
        } catch (IOException e) {
            LOGGER.warn("Failed to save server overrides", e);
        }
    }

    public static void setCurrentServer(String serverIp) {
        currentServer = serverIp;
    }

    public static Optional<String> getCurrentServer() {
        return Optional.ofNullable(currentServer);
    }

    public static <T> T getOverride(String key, T defaultValue) {
        if (currentServer == null) return defaultValue;
        Map<String, Object> serverOpts = overrides.get(currentServer);
        if (serverOpts == null) return defaultValue;
        Object val = serverOpts.get(key);
        if (val == null) return defaultValue;
        try {
            if (defaultValue instanceof Boolean) return (T) Boolean.valueOf(val.toString());
            if (defaultValue instanceof Enum) {
                for (var constant : defaultValue.getClass().getEnumConstants()) {
                    if (constant.toString().equalsIgnoreCase(val.toString())) return (T) constant;
                }
                return defaultValue;
            }
            return (T) val;
        } catch (Exception e) {
            return defaultValue;
        }
    }

    public static <T> void setOverride(String key, T value) {
        if (currentServer == null) return;
        overrides.computeIfAbsent(currentServer, k -> new HashMap<>()).put(key, value);
        save();
    }
}
