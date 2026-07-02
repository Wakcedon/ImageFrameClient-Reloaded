package com.loohp.imageframe.mixin;

import com.loohp.imageframe.configuration.Configuration;
import com.loohp.imageframe.object.FilledMapTooltipComponent;
import com.loohp.imageframe.object.ImageMapTooltipComponent;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.saveddata.maps.MapId;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(Item.class)
public class ItemMixin {

    @Unique
    private static final String KEY = "CombinedImageMap";

    @Inject(at = @At("HEAD"), cancellable = true, method = "getTooltipImage")
    public void getTooltipImage(ItemStack stack, CallbackInfoReturnable<Optional<TooltipComponent>> cir) {
        Item item = stack.getItem();
        if (Configuration.PREVIEW_MAPS_IN_TOOLTIP.get() && Items.PAPER.equals(item)) {
            CustomData customDataComponent = stack.get(DataComponents.CUSTOM_DATA);
            if (customDataComponent != null) {
                CompoundTag tag = customDataComponent.copyTag();
                if (tag.contains(KEY)) {
                    int index = tag.getInt(KEY);
                    cir.setReturnValue(Optional.of(new ImageMapTooltipComponent(index)));
                    cir.cancel();
                }
            }
        } else if (Configuration.PREVIEW_MAPS_IN_TOOLTIP.get() && Items.FILLED_MAP.equals(item)) {
            MapId mapIdComponent = stack.get(DataComponents.MAP_ID);
            if (mapIdComponent != null) {
                cir.setReturnValue(Optional.of(new FilledMapTooltipComponent(mapIdComponent.id())));
                cir.cancel();
            }
        }
    }

}
