package com.loohp.imageframe.object;

import com.loohp.imageframe.ImageFrameClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.gui.MapRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;

public class ImageMapTooltipComponent implements MapTooltipComponent {

    private static final ResourceLocation BACKGROUND = ResourceLocation.withDefaultNamespace("textures/map/map_background.png");

    private static final float BG_INSET = 1F;
    private static final float PAD_PX = 2F;

    private final int index;

    public ImageMapTooltipComponent(int index) {
        this.index = index;
    }

    @Override
    public int getHeight() {
        return ImageFrameClient.MOD.getOrRequestImageMapData(index) == null ? 0 : 66;
    }

    @Override
    public int getWidth(Font font) {
        ImageMapData data = ImageFrameClient.MOD.getOrRequestImageMapData(index);
        if (data == null) return 0;
        return getWidth(data);
    }

    private int getWidth(ImageMapData data) {
        int cols = data.width();
        int rows = data.height();
        float availableH = 64f - BG_INSET * 2f;
        float usableH = Math.max(0f, availableH - PAD_PX * 2f);
        int tileSizePxInt = Math.max(1, (int) Math.floor(usableH / rows));
        float totalWidth = cols * tileSizePxInt + PAD_PX * 2f + BG_INSET * 2f;
        return (int) Math.ceil(totalWidth);
    }

    @Override
    public void extractImage(Font font, int x, int y, int width, int height, GuiGraphics graphics) {
        Minecraft client = Minecraft.getInstance();
        ClientLevel level = client.level;
        if (level == null) return;

        ImageMapData imageMapData = ImageFrameClient.MOD.getOrRequestImageMapData(index);
        if (imageMapData == null) return;

        int gridCols = imageMapData.width();
        int gridRows = imageMapData.height();
        float availableH = 64f - BG_INSET * 2f;
        float usableH = Math.max(0f, availableH - PAD_PX * 2f);
        int tileSizePxInt = Math.max(1, (int) Math.floor(usableH / gridRows));

        int mapWidth = getWidth(imageMapData);
        graphics.blit(BACKGROUND, x, y, 0, 0, mapWidth, 64, mapWidth, 64);

        float gridW = gridCols * tileSizePxInt + PAD_PX * 2f;
        float gridH = gridRows * tileSizePxInt + PAD_PX * 2f;

        float originX = x + Math.round((mapWidth - gridW) * 0.5f);
        float originY = y + Math.round((64f - gridH) * 0.5f);

        float overlap = 0.25f;
        float tileScale = (tileSizePxInt + overlap) / 128f;

        MapRenderer mapRenderer = client.gameRenderer.getMapRenderer();
        for (int i = 0; i < imageMapData.mapIds().size(); i++) {
            int id = imageMapData.mapIds().getInt(i);
            int col = i % gridCols;
            int row = i / gridCols;

            MapItemSavedData data = level.getMapData(new MapId(id));
            if (data == null) continue;

            float drawX = originX + PAD_PX + col * tileSizePxInt;
            float drawY = originY + PAD_PX + row * tileSizePxInt;

            graphics.pose().pushPose();
            graphics.pose().translate(drawX, drawY, 0.0);
            graphics.pose().translate(-overlap * 0.5f, -overlap * 0.5f, 0.0);
            graphics.pose().scale(tileScale, tileScale, 1.0f);

            mapRenderer.render(
                    graphics.pose(),
                    graphics.bufferSource(),
                    new MapId(id),
                    data,
                    false,
                    0xF000F0
            );
            graphics.pose().popPose();
        }
    }

}
