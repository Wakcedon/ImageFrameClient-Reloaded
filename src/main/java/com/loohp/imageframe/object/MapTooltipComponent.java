package com.loohp.imageframe.object;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.world.inventory.tooltip.TooltipComponent;

public interface MapTooltipComponent extends TooltipComponent, ClientTooltipComponent {

    @Override
    default void renderImage(Font font, int mouseX, int mouseY, GuiGraphics guiGraphics) {
        extractImage(font, mouseX, mouseY, getWidth(font), getHeight(), guiGraphics);
    }

    void extractImage(Font font, int x, int y, int width, int height, GuiGraphics guiGraphics);

}
