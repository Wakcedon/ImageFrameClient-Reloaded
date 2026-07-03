package com.loohp.imageframe.configuration;

import com.loohp.imageframe.object.Resolution;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.ModConfigSpec;

public class Configuration {

    public static ModConfigSpec.ConfigValue<Boolean> USE_NATIVE_RES_MAP_IMAGES;
    public static ModConfigSpec.ConfigValue<Resolution> MAX_IMAGE_SIZE;
    public static ModConfigSpec.ConfigValue<Boolean> PREVIEW_MAPS_IN_TOOLTIP;
    public static ModConfigSpec.ConfigValue<Boolean> PREVIEW_PAINTINGS_IN_TOOLTIP;
    public static ModConfigSpec.ConfigValue<Boolean> NOTIFY_WHEN_SERVER_SUPPORTS;
    public static ModConfigSpec.ConfigValue<Integer> CACHE_TTL_DAYS;
    public static ModConfigSpec.ConfigValue<Integer> CACHE_MAX_MB;

    private static ModConfigSpec CONFIG_SPEC;

    public static void init(ModContainer container) {
        var builder = new ModConfigSpec.Builder();

        builder.push("general");
        USE_NATIVE_RES_MAP_IMAGES = builder
                .comment("Use native resolution and full color map images sent from the server.")
                .translation("imageframeclient.config.useNativeResMapImages")
                .define("useNativeResMapImages", true);

        MAX_IMAGE_SIZE = builder
                .comment("The maximum pixels a map can have on each side.")
                .translation("imageframeclient.config.maxImageSize")
                .defineEnum("maxImageSize", Resolution.NATIVE);

        PREVIEW_MAPS_IN_TOOLTIP = builder
                .comment("Preview maps when you hover over them in your inventory.")
                .translation("imageframeclient.config.previewMapsInTooltip")
                .define("previewMapsInTooltip", true);

        PREVIEW_PAINTINGS_IN_TOOLTIP = builder
                .comment("Preview paintings when you hover over them in your inventory.")
                .translation("imageframeclient.config.previewPaintingsInTooltip")
                .define("previewPaintingsInTooltip", true);

        NOTIFY_WHEN_SERVER_SUPPORTS = builder
                .comment("Show a toast message when you join a server if they support ImageFrame client.")
                .translation("imageframeclient.config.notifyWhenServerSupports")
                .define("notifyWhenServerSupports", true);
        builder.pop();

        builder.push("cache");
        CACHE_TTL_DAYS = builder
                .comment("Maximum number of days to keep cached HD textures (0 = forever).")
                .translation("imageframeclient.config.cacheTtlDays")
                .defineInRange("cacheTtlDays", 7, 0, 365);

        CACHE_MAX_MB = builder
                .comment("Maximum cache size in megabytes (0 = unlimited). Oldest files are deleted first.")
                .translation("imageframeclient.config.cacheMaxMb")
                .defineInRange("cacheMaxMb", 100, 0, 10000);
        builder.pop();

        CONFIG_SPEC = builder.build();
        container.registerConfig(ModConfig.Type.CLIENT, CONFIG_SPEC);
    }

}
