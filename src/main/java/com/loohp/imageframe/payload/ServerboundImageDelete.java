package com.loohp.imageframe.payload;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record ServerboundImageDelete(String name) implements CustomPacketPayload {

    public static final Type<ServerboundImageDelete> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("imageframe", "serverbound_image_delete"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ServerboundImageDelete> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, ServerboundImageDelete::name,
            ServerboundImageDelete::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

}
