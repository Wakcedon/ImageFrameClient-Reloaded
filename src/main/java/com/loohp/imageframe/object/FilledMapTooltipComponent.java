package com.loohp.imageframe.object;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.gui.MapRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;

public class FilledMapTooltipComponent implements MapTooltipComponent {

    private static final ResourceLocation BACKGROUND = ResourceLocation.withDefaultNamespace("textures/map/map_background.png");

    private final int mapId;

    public FilledMapTooltipComponent(int mapId) {
        this.mapId = mapId;
    }

    @Override
    public int getHeight() {
        return 66;
    }

    @Override
    public int getWidth(Font font) {
        return 66;
    }

    @Override
    public void extractImage(Font font, int x, int y, int width, int height, GuiGraphics graphics) {
        graphics.blit(BACKGROUND, x, y, 0, 0, 64, 64, 64, 64);

        Minecraft client = Minecraft.getInstance();
        ClientLevel level = client.level;
        if (level == null) return;

        MapItemSavedData data = level.getMapData(new MapId(mapId));
        if (data == null) return;

        MapRenderer mapRenderer = client.gameRenderer.getMapRenderer();
        mapRenderer.render(
                graphics.pose(),
                graphics.bufferSource(),
                new MapId(mapId),
                data,
                false,
                0xF000F0
        );
    }

}
