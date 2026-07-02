package com.loohp.imageframe.mixin;

import com.loohp.imageframe.ImageFrameClient;
import com.loohp.imageframe.configuration.Configuration;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.gui.MapRenderer;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MapRenderer.class)
public class MapRendererMixin {

    @Inject(at = @At("HEAD"), method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/world/level/saveddata/maps/MapId;Lnet/minecraft/world/level/saveddata/maps/MapItemSavedData;ZI)V")
    private void beforeRender(PoseStack poseStack, MultiBufferSource buffer, MapId mapId, MapItemSavedData mapData, boolean isFrame, int packedLight, CallbackInfo ci) {
        if (!Configuration.USE_NATIVE_RES_MAP_IMAGES.get()) return;

        DynamicTexture hdTexture = ImageFrameClient.MOD.getHdTexture(mapId.id());
        if (hdTexture == null) {
            ImageFrameClient.MOD.requestHdMap(mapId.id());
            return;
        }

        ResourceLocation mapLocation = ResourceLocation.withDefaultNamespace("map/" + mapId.id());
        Minecraft.getInstance().getTextureManager().register(mapLocation, hdTexture);
    }

}
