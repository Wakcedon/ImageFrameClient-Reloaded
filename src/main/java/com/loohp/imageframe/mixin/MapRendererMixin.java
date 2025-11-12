package com.loohp.imageframe.mixin;

import com.loohp.imageframe.ImageFrameClient;
import com.loohp.imageframe.configuration.Configuration;
import net.minecraft.client.render.MapRenderState;
import net.minecraft.client.render.MapRenderer;
import net.minecraft.component.type.MapIdComponent;
import net.minecraft.item.map.MapState;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MapRenderer.class)
public class MapRendererMixin {

    @Inject(at = @At("TAIL"), method = "update")
    public void update(MapIdComponent mapId, MapState mapState, MapRenderState renderState, CallbackInfo ci) {
        if (!Configuration.useNativeResMapImages) {
            return;
        }
        if (renderState.texture == null) {
            return;
        }
        Identifier hdMapId = ImageFrameClient.MOD.getOrRequestLoadedHdMap(mapId.id());
        if (hdMapId == null) {
            return;
        }
        renderState.texture = hdMapId;
    }

}
