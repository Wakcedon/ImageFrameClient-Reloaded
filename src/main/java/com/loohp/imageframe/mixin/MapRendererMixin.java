package com.loohp.imageframe.mixin;

import com.loohp.imageframe.ImageFrameClient;
import com.loohp.imageframe.configuration.Configuration;
import net.minecraft.client.renderer.MapRenderer;
import net.minecraft.client.renderer.state.MapRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MapRenderer.class)
public class MapRendererMixin {

    @Inject(at = @At("TAIL"), method = "extractRenderState")
    public void extractRenderState(MapId mapId, MapItemSavedData mapData, MapRenderState mapRenderState, CallbackInfo ci) {
        if (!Configuration.useNativeResMapImages) {
            return;
        }
        if (mapRenderState.texture == null) {
            return;
        }
        Identifier hdMapId = ImageFrameClient.MOD.getOrRequestLoadedHdMap(mapId.id());
        if (hdMapId == null) {
            return;
        }
        mapRenderState.texture = hdMapId;
    }

}
