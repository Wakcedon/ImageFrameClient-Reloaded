package com.loohp.imageframe.mixin;

import com.loohp.imageframe.object.FilledMapTooltipData;
import com.loohp.imageframe.object.ImageMapTooltipData;
import net.minecraft.component.Component;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.MapIdComponent;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.tooltip.TooltipData;
import net.minecraft.nbt.NbtCompound;
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

    @Inject(at = @At("HEAD"), cancellable = true, method = "getTooltipData")
    public void getTooltipData(ItemStack stack, CallbackInfoReturnable<Optional<TooltipData>> cir) {
        Item item = stack.getItem();
        if (Items.PAPER.equals(item)) {
            Component<NbtComponent> customDataComponent = stack.getTyped(DataComponentTypes.CUSTOM_DATA);
            if (customDataComponent != null) {
                NbtCompound tag = customDataComponent.value().copyNbt();
                Optional<Integer> optIndex = tag.getInt(KEY);
                if (optIndex.isPresent()) {
                    cir.setReturnValue(Optional.of(new ImageMapTooltipData(optIndex.get())));
                    cir.cancel();
                }
            }
        } else if (Items.FILLED_MAP.equals(item)) {
            Component<MapIdComponent> mapIdComponent = stack.getTyped(DataComponentTypes.MAP_ID);
            if (mapIdComponent != null) {
                cir.setReturnValue(Optional.of(new FilledMapTooltipData(mapIdComponent.value().id())));
                cir.cancel();
            }
        }
    }

}
